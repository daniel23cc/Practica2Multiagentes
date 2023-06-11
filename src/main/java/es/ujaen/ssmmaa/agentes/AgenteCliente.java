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
import jade.domain.FIPAAgentManagement.FailureException;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPANames;
import jade.lang.acl.MessageTemplate;
import jade.proto.ContractNetInitiator;
import java.util.Date;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author danie
 */
public class AgenteCliente extends Agent {
    //Variables del agente

    private ArrayList<ArrayList<String>> servicios;
    private AgenteClienteJFrame myGui;
    private ArrayList<AID>[] listaAgentes;
    private int restAenviar;

    private Resultado resultado;
    private String claveMapa;

    private int contRest = 0;

    private float dineroDisponible = 60;

    private static int numPlatosRestantes;

    @Override
    protected void setup() {

        myGui = new AgenteClienteJFrame(this);
        myGui.setVisible(true);
        myGui.presentarSalida("Se inicializa la ejecución de " + this.getName() + "\n");
        //creamos lista agentes
        listaAgentes = new ArrayList[CATEGORIAS.length];
        for (NombreServicio categoria : CATEGORIAS) {
            listaAgentes[categoria.ordinal()] = new ArrayList<>();
        }

        //Incialización de variables
        servicios = new ArrayList<ArrayList<String>>();
        servicios.add(new ArrayList<>());
        numPlatosRestantes = 0;
        String[] parts = getName().split("@");
        claveMapa = parts[0].substring(0, parts[0].length() - 1);

        //obtengo el argumento
        Object[] args = getArguments();

        if (args != null && args.length > 0) {
            for (int i = 0; i < args.length - 1; i++) {
                if ((String) args[i] == ";") {
                    servicios.add(new ArrayList<>());
                } else {
                    servicios.get(servicios.size() - 1).add((String) args[i]);
                }
            }
            resultado = (Resultado) args[args.length - 1];
        }


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

        addBehaviour(new TareaInicio(this, aleatorio.nextInt(MAX_TIEMPO_PEDIR_PLATOS - MIN_TIEMPO_PEDIR_PLATOS) + MIN_TIEMPO_PEDIR_PLATOS));

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

    public class TareaSolicitarServicioIniciador extends ContractNetInitiator {

        public TareaSolicitarServicioIniciador(Agent a, ACLMessage cfp) {

            super(a, cfp);
            myGui.presentarSalida("Solicitando entrar para perdir servicio: " + cfp.getContent());

        }

        @Override
        protected void handlePropose(ACLMessage propose, Vector acceptances) {
           
            float costePropuesto = Float.parseFloat(propose.getContent());
            if (costePropuesto < dineroDisponible) {
                dineroDisponible -= costePropuesto;
                myGui.presentarSalida("Cliente acepta el presupuesto de " + costePropuesto + " € del Restaurante (Le quedan: " + dineroDisponible + " €)");
                ACLMessage accept = propose.createReply();
                accept.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
                accept.setContent("OK");
                acceptances.add(accept);

            } else {
                myGui.presentarSalida("Cliente no puede entrar porque es mas pobre que un Venezolano");
                ACLMessage accept = propose.createReply();
                accept.setPerformative(ACLMessage.REJECT_PROPOSAL);
                accept.setContent("NO");
                acceptances.add(accept);
            }
        }

        @Override
        protected void handleRefuse(ACLMessage refuse) {
            // rechazo del restaurante, si no puedo con un servicio intento con otro
            myGui.presentarSalida("Cliente rechazado, esta seguramente cocinando otros servicios");
            contRest++;
        }

        @Override
        protected void handleInform(ACLMessage inform) {

            //myGui.presentarSalida(inform.getContent());
            String[] contenido = inform.getContent().split(",");
            //myGui.presentarSalida("ELES: " + contenido.length / 3);

            for (int i = 0; i < contenido.length / 3; i++) {
                numPlatosRestantes--;

                int tiempoEspera = aleatorio.nextInt(MAX_TIEMPO_PEDIR_PLATOS - MIN_TIEMPO_PEDIR_PLATOS) + MIN_TIEMPO_PEDIR_PLATOS;
                myGui.presentarSalida("***************************");
                myGui.presentarSalida("Comiendo: " + contenido[i * 3] + ", quedan " + numPlatosRestantes + " platos del servicio actual");
                myGui.presentarSalida("Esperando... " + tiempoEspera + " ms");

                try {
                    Thread.sleep(tiempoEspera);
                } catch (InterruptedException ex) {
                    Logger.getLogger(AgenteCliente.class.getName()).log(Level.SEVERE, null, ex);
                }
                myGui.presentarSalida("ÑAM ÑAM");
                myGui.presentarSalida("***************************");
                // Limpiar el contenido del último mensaje procesado  

            }
            inform.clearAllReceiver();
            myGui.presentarSalida("Agente ya HA TERMINADO todos los platos de este servicio");
            servicios.remove(PRIMERO);

        }


        @Override
        protected void handleFailure(ACLMessage failure) {
            myGui.presentarSalida("Cliente rechazado, esta seguramente cocinando otros servicios");
            contRest++;
        }
    }

    public class TareaInicio extends TickerBehaviour {

        public TareaInicio(Agent a, long period) {
            super(a, period);
        }

        @Override
        protected void onTick() {
            if (!servicios.isEmpty()) {
                if (numPlatosRestantes == 0) {
                    myGui.presentarSalida("----------VUELTA--------- de"+myAgent.getName());

                    ACLMessage msg = new ACLMessage(ACLMessage.CFP);
                    msg.setProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET);
                    msg.setSender(getAID());
                    String contenido = "";
                    for (int i = 0; i < servicios.get(PRIMERO).size(); i++) {
                        contenido += servicios.get(PRIMERO).get(i);
                        if (i != servicios.get(PRIMERO).size() - 1) {
                            contenido += ":";
                        }
                    }
                    msg.setContent(contenido);
                    numPlatosRestantes = contenido.split(":").length;

                    if (listaAgentes[RESTAURANTE.ordinal()].size() > 0) {
                        int numAgentes = listaAgentes[RESTAURANTE.ordinal()].size();
                        if (numAgentes <= contRest) {
                            contRest = 0;
                        }
                        //java.util.Iterator<AID> it = listaAgentes[RESTAURANTE.ordinal()].iterator();
                        //aqui aplico un algoritmo donde reparto la presion selectiva, priorizaré el 90% de las veces repartir aleatoriamente los clientes entre los restaurantes
                        //sin embargo, dare oportunidad a cambiar la politica de reparto en caso de que los restaurantes esten sobresaturados
                        if (aleatorio.nextInt(D100) > 99) {
                            restAenviar = contRest;
                        } else {
                            restAenviar = aleatorio.nextInt(numAgentes);
                        }

                        msg.addReceiver(listaAgentes[RESTAURANTE.ordinal()].get(restAenviar));
                        myGui.presentarSalida("--> Solicitud entrar a Restaurante nº: " + restAenviar);
                        addBehaviour(new TareaSolicitarServicioIniciador(this.myAgent, msg));
                    }
                }
            } else {
                //borrar el cliente
                myGui.presentarSalida("AGENTE YA HA ACABADO DE COMER TODOS SUS PLATOS");

                myAgent.doDelete();
            }
        }

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

}
