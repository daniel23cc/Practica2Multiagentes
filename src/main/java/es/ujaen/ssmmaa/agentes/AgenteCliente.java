/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.ujaen.ssmmaa.agentes;

import auxiliares.Resultado;
import static es.ujaen.ssmmaa.agentes.Constantes.CATEGORIAS;
import static es.ujaen.ssmmaa.agentes.Constantes.D100;
import static es.ujaen.ssmmaa.agentes.Constantes.MAXIMOS_INTENTOS_COMER;
import static es.ujaen.ssmmaa.agentes.Constantes.MAX_TIEMPO_PEDIR_PLATOS;
import static es.ujaen.ssmmaa.agentes.Constantes.MIN_TIEMPO_PEDIR_PLATOS;
import es.ujaen.ssmmaa.agentes.Constantes.NombreServicio;
import static es.ujaen.ssmmaa.agentes.Constantes.NombreServicio.CLIENTE;
import static es.ujaen.ssmmaa.agentes.Constantes.NombreServicio.RESTAURANTE;
import static es.ujaen.ssmmaa.agentes.Constantes.PRIMERO;
import jade.core.AID;
import es.ujaen.ssmmaa.agentes.Constantes.Plato;
import static es.ujaen.ssmmaa.agentes.Constantes.TIPO_SERVICIO;
import static es.ujaen.ssmmaa.agentes.Constantes.aleatorio;
import es.ujaen.ssmmaa.gui.AgenteClienteJFrame;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFSubscriber;
import jade.lang.acl.ACLMessage;
import jade.util.leap.Iterator;
import java.util.ArrayList;
import java.util.Arrays;
import jade.domain.FIPAException;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.MessageTemplate;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author danie
 */
public class AgenteCliente extends Agent {
    //Variables del agente

    private ArrayList<String> servicios;
    private boolean heEntrado;
    private AgenteClienteJFrame myGui;
    private AID agenteRestaurante;
    private ArrayList<AID>[] listaAgentes;
    private AID agenteDF;
    private int restAenviar;

    private Resultado resultado;
    private String claveMapa;

    private int cont = 0;

    @Override
    protected void setup() {

        myGui = new AgenteClienteJFrame(this);
        myGui.setVisible(true);
        myGui.presentarSalida("Se inicializa la ejecución de " + this.getName() + "\n");
        //creamos lista agentes
        listaAgentes = new ArrayList[CATEGORIAS.length];
        agenteDF = new AID("df", AID.ISLOCALNAME); //evitar recibir mensajes de DF
        for (NombreServicio categoria : CATEGORIAS) {
            listaAgentes[categoria.ordinal()] = new ArrayList<>();
        }

        //Incialización de variables
        servicios = new ArrayList<>();
        heEntrado = false;
        String[] parts = getName().split("@");
        claveMapa= parts[0].substring(0, parts[0].length() - 1);

        //obtengo el argumento
        Object[] args = getArguments();
        if (args != null && args.length > 0) {
            System.out.println("HAY: " + args.length);
            for (int i = 0; i < args.length - 1; i++) {
                servicios.add((String) args[i]);
            }
            resultado = (Resultado) args[args.length - 1];
        }

        //resultado.addfechasLlegada(claveMapa, new Date());
        //Registro del agente en las Páginas Amarrillas
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType(TIPO_SERVICIO);
        sd.setName(CLIENTE.name());
        dfd.addServices(sd);
        try {
            DFService.register(this, dfd);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }

        //Busco agentes restaurante
        // Se añaden las tareas principales
        DFAgentDescription template = new DFAgentDescription();
        ServiceDescription templateSd = new ServiceDescription();
        templateSd.setType(TIPO_SERVICIO);
        templateSd.setName(RESTAURANTE.name());
        template.addServices(templateSd);

        addBehaviour(new TareaSuscripcionDF(this, template));
        addBehaviour(new TareaEnvioSolicitudEntradaRestaurante(this, 2000));
        addBehaviour(new TareaRecibirContestacionRestaurante(this));
        addBehaviour(new TareaPedirPlatos(this, aleatorio.nextInt(MAX_TIEMPO_PEDIR_PLATOS - MIN_TIEMPO_PEDIR_PLATOS) + MIN_TIEMPO_PEDIR_PLATOS));
        addBehaviour(new TareaRecibirPlato(this));
    }

    @Override
    protected void takeDown() {
        //Eliminar registro del agente en las Páginas Amarillas
        try {
            DFService.deregister(this);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }
        //Liberación de recursos, incluido el GUI
        //Despedida
        myGui.dispose();
        System.out.println("Finaliza la ejecución del agente: " + claveMapa);
    }

    public class TareaSuscripcionDF extends DFSubscriber {

        public TareaSuscripcionDF(Agent a, DFAgentDescription template) {
            super(a, template);
        }

        @Override
        public void onRegister(DFAgentDescription dfad) {
            Iterator it = dfad.getAllServices();
            while (it.hasNext()) {
                ServiceDescription sd = (ServiceDescription) it.next();

                for (NombreServicio nombreServicio : CATEGORIAS) {
                    if (sd.getName().equals(nombreServicio.name())) {
                        listaAgentes[nombreServicio.ordinal()].add(dfad.getName());
                    }
                }
            }

            myGui.presentarSalida("El agente: " + myAgent.getName()
                    + "ha encontrado a:\n\t" + dfad.getName());
        }

        @Override
        public void onDeregister(DFAgentDescription dfad) {
            AID agente = dfad.getName();

            for (NombreServicio servicio : CATEGORIAS) {
                if (listaAgentes[servicio.ordinal()].remove(agente)) {
                    myGui.presentarSalida("El agente: " + agente.getName()
                            + " ha sido eliminado de la lista de "
                            + myAgent.getName());
                }
            }
        }
    }

    public class TareaEnvioSolicitudEntradaRestaurante extends TickerBehaviour {

        public TareaEnvioSolicitudEntradaRestaurante(Agent a, long period) {
            super(a, period);
        }

        @Override
        public void onTick() {

            //Se envía la operación a todos los agentes restaurante
            if (!heEntrado) {
                if (listaAgentes[RESTAURANTE.ordinal()].size() > 0) {

                    //creo estructura mensaje
                    ACLMessage mensaje = new ACLMessage(ACLMessage.REQUEST);
                    //digo quien lo envia
                    mensaje.setSender(myAgent.getAID());
                    //Se añaden todos los agentes operación
                    int numAgentes = listaAgentes[RESTAURANTE.ordinal()].size();
                    myGui.presentarSalida("Agentes restaurante encontrados:" + numAgentes + "\n");
                    if (numAgentes <= cont) {
                        cont = 0;
                    }

                    //aqui aplico un algoritmo donde reparto la presion selectiva, priorizaré el 90% de las veces repartir aleatoriamente los clientes entre los restaurantes
                    //sin embargo, dare oportunidad a cambiar la politica de reparto en caso de que los restaurantes esten sobresaturados
                    if (aleatorio.nextInt(D100) > 99) {
                        restAenviar = cont;
                    } else {
                        restAenviar = aleatorio.nextInt(numAgentes);
                    }

                    mensaje.addReceiver(listaAgentes[RESTAURANTE.ordinal()].get(restAenviar));

                    //solicito entrar
                    mensaje.setContent("Cliente," + getAID().toString() + ",solicita entrar");

                    myGui.presentarSalida("--> Solicitud entrar a Restaurante nº: " + restAenviar);
                    send(mensaje);

                }
            }

        }
    }

    public class TareaRecibirContestacionRestaurante extends CyclicBehaviour {

        public TareaRecibirContestacionRestaurante(AgenteCliente aThis) {
            super(aThis);
        }

        @Override
        public void action() {
            if (!heEntrado) {
                MessageTemplate plantilla = MessageTemplate.and(
                        MessageTemplate.MatchPerformative(ACLMessage.INFORM),
                        MessageTemplate.not(MessageTemplate.MatchSender(agenteDF)));
                ACLMessage mensaje = myAgent.receive(plantilla);

                if (mensaje != null) {
                    String[] contenido = mensaje.getContent().split(",");
                    if (contenido[0].equals("OK")) {
                        myGui.presentarSalida("<-- He entrado al restaurante nº: " + restAenviar);
                        heEntrado = true;
                        agenteRestaurante = mensaje.getSender();
                        //resultado.addfechasEntrada(claveMapa, new Date());

                    } else {
                        myGui.presentarSalida("<-- NO He entrado al restaurante nº: " + restAenviar);
                        cont++;
                    }
                }
            }
        }

    }

    public class TareaPedirPlatos extends TickerBehaviour {

        public TareaPedirPlatos(Agent a, long period) {
            super(a, period);
        }

        @Override
        public void onTick() {
            if (heEntrado) {
                //Plato platoPedido = Plato.pedirPlato();
                if (!servicios.isEmpty()) {
                    String platoPedido = servicios.get(PRIMERO);
                    System.out.println("Cliente: " + getAID() + " pide: " + platoPedido);

                    //creo estructura mensaje
                    ACLMessage mensaje = new ACLMessage(ACLMessage.INFORM);
                    //digo quien lo envia
                    mensaje.setSender(myAgent.getAID());
                    //Se añade el agenteRestaurante al cual entro el cliente

                    mensaje.addReceiver(agenteRestaurante);

                    //solicito el plato
                    mensaje.setContent(platoPedido + "," + myAgent.getAID());

                    myGui.presentarSalida("--> Solicitud del plato: " + mensaje.getContent() + "\n");
                    send(mensaje);
                } else {
                    //borrar el cliente
                    myGui.presentarSalida("AGENTE YA HA ACABADO DE COMER TODOS SUS PLATOS");
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(AgenteCliente.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    myAgent.doDelete();
                }
            }

        }
    }

    public class TareaRecibirPlato extends CyclicBehaviour {

        public TareaRecibirPlato(AgenteCliente aThis) {
            super(aThis);
        }

        @Override
        public void action() {
            if (heEntrado) {
                MessageTemplate plantilla = MessageTemplate.and(
                        MessageTemplate.MatchPerformative(ACLMessage.CONFIRM),
                        MessageTemplate.not(MessageTemplate.MatchSender(agenteDF)));
                ACLMessage mensaje = myAgent.receive(plantilla);

                if (mensaje != null) {
                    servicios.remove(PRIMERO);
                    String[] contenido = mensaje.getContent().split(",");
                    myGui.presentarSalida("***************************");
                    myGui.presentarSalida("Comiendo: " + contenido[0]);
                    myGui.presentarSalida("***************************");
                    //resultado.addfechasSalida(claveMapa, new Date());
                }
            }
        }
    }
}
