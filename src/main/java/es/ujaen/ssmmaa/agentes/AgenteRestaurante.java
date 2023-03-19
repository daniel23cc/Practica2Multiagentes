/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.ujaen.ssmmaa.agentes;

import static es.ujaen.ssmmaa.agentes.Constantes.CATEGORIAS;
import es.ujaen.ssmmaa.agentes.Constantes.NombreServicio;
import static es.ujaen.ssmmaa.agentes.Constantes.NombreServicio.CLIENTE;
import static es.ujaen.ssmmaa.agentes.Constantes.NombreServicio.COCINA;
import static es.ujaen.ssmmaa.agentes.Constantes.NombreServicio.RESTAURANTE;
import es.ujaen.ssmmaa.agentes.Constantes.OrdenComanda;
import static es.ujaen.ssmmaa.agentes.Constantes.PLATOS;
import es.ujaen.ssmmaa.agentes.Constantes.Plato;
import static es.ujaen.ssmmaa.agentes.Constantes.TIPO_SERVICIO;
import es.ujaen.ssmmaa.gui.AgenteRestauranteJFrame;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.DFSubscriber;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.util.leap.Iterator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author danie
 */
public class AgenteRestaurante extends Agent {

    // Variables del agente
    private AgenteRestauranteJFrame myGui;
    // Capacidad máxima de comandas que podemos recibir simultáneamente
    private static int capacidadComensales;
    private static int numComensales;
    private static int capacidadServicios;
    private static int numServicios;

    private ArrayList<AID>[] listaAgentes;
    private ArrayList<Plato> platosPedidos;
    private ArrayList<Plato> platosCocinados;
    private AID agenteDF;
    //private AID cliente;
    private int contCocinas = 0;

    /**
     * Se ejecuta cuando se inicia el agente
     */
    @Override
    protected void setup() {
        //Configuración del GUI y presentación
        // System.getProperty("java.classpath");

        listaAgentes = new ArrayList[CATEGORIAS.length];
        agenteDF = new AID("df", AID.ISLOCALNAME); //evitar recibir mensajes de DF
        for (NombreServicio categoria : CATEGORIAS) {
            listaAgentes[categoria.ordinal()] = new ArrayList<>();
        }

        //obtengo el argumento
        Object[] args = getArguments();

        if (args != null && args.length > 0) {
            String argumento = (String) args[0];
            capacidadComensales = Integer.parseInt(argumento);
            argumento = (String) args[1];
            capacidadServicios = Integer.parseInt(argumento);
        } else {
            System.out.println("Error: el agente Restaurante necesita argumentos para su funcionamiento");
            doDelete();
        }

        myGui = new AgenteRestauranteJFrame(this);
        myGui.setVisible(true);
        myGui.presentarSalida("Se inicializa la ejecución de " + this.getName() + "\n");

        // Inicializamos
        numComensales = 0;
        numServicios = 0;
        platosPedidos = new ArrayList<>();
        platosCocinados = new ArrayList<>();
        //registro del agente en las paginas amarillas
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType(TIPO_SERVICIO);
        sd.setName(RESTAURANTE.name());
        dfd.addServices(sd);

        try {
            DFService.register(this, dfd);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }

        //busco agentes cliente y cocina
        DFAgentDescription template = new DFAgentDescription();

        ServiceDescription templateSd = new ServiceDescription();
        templateSd.setType(TIPO_SERVICIO);
        template.addServices(templateSd);

        addBehaviour(new TareaEntradaClientes(this));
        addBehaviour(new TareaSuscripcionDF(this, template));
        addBehaviour(new TareaEnvioCocina(this,1000));
        //addBehaviour(new TareaRecibirContestacionCocina(this));
        addBehaviour(new TareaEnvioCliente(this));

    }

//
//    /**
//     * Se ejecuta al finalizar el agente
//     */
    @Override
    protected void takeDown() {
        //Desregistro de las Páginas Amarillas
        try {
            DFService.deregister(this);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }

        //Se liberan los recuros y se despide
        myGui.dispose();
        System.out.println("Finaliza la ejecución de " + this.getName());

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
//            System.out.println("El agente: " + myAgent.getName()
//                    + "ha encontrado a:\n\t" + dfad.getName());
        }

        @Override
        public void onDeregister(DFAgentDescription dfad) {
            AID agente = dfad.getName();

            for (NombreServicio servicio : CATEGORIAS) {
                if (listaAgentes[servicio.ordinal()].remove(agente)) {
//                    System.out.println("El agente: " + agente.getName()
//                            + " ha sido eliminado de la lista de "
//                            + myAgent.getName());
                    myGui.presentarSalida("El agente: " + agente.getName()
                            + " ha sido eliminado de la lista de "
                            + myAgent.getName());
                }
            }
        }
    }

    public class TareaEntradaClientes extends CyclicBehaviour {

        public TareaEntradaClientes(Agent a) {
            super(a);
        }

        @Override
        public void action() {
            if (numServicios < capacidadServicios) {
                MessageTemplate plantilla = MessageTemplate.and(
                        MessageTemplate.MatchPerformative(ACLMessage.REQUEST),
                        MessageTemplate.not(MessageTemplate.MatchSender(agenteDF)));

                ACLMessage mensaje = myAgent.receive(plantilla);

                if (mensaje != null) {
                    String[] contenido = mensaje.getContent().split(",");
                    //myGui.presentarSalida("CONTENIDO: " + contenido[1] + "\n");
                    if (numComensales < capacidadComensales) {
                        //System.out.println("SE METE");
                        //cliente = mensaje.getSender();

                        ACLMessage respuestaEntrada = new ACLMessage(ACLMessage.INFORM);
                        //respuestaEntrada.addReceiver(cliente);
                        respuestaEntrada.addReceiver(mensaje.getSender());
                        respuestaEntrada.setContent("OK");
                        send(respuestaEntrada);
                        numComensales++;
                        addBehaviour(new TareaRecepcion());
                        myGui.presentarSalida("--> Aceptacion de entrada al cliente");
                    }// si no hay capacidad, responder que no se puede entrar
                    else {
                        ACLMessage respuestaEntrada = new ACLMessage(ACLMessage.INFORM);
                        respuestaEntrada.addReceiver(mensaje.getSender());
                        respuestaEntrada.setContent("NO");
                        send(respuestaEntrada);
                        myGui.presentarSalida("--> Rechazo de entrada al cliente");
                    }
                }
            } else {
                myAgent.doDelete();
            }
        }
    }

    public class TareaRecepcion extends CyclicBehaviour {

        @Override
        public void action() {
            if (numServicios < capacidadServicios) {
                //Recepción de los platos del cliente
                MessageTemplate plantilla = MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.INFORM),
                        MessageTemplate.not(MessageTemplate.MatchSender(agenteDF)));
                ACLMessage mensaje = myAgent.receive(plantilla);
                if (mensaje != null) {
                    //procesamos el mensaje
                    String[] contenido = mensaje.getContent().split(",");
                    if (contenido[0].equals("ENVIADO")) {
                        myGui.presentarSalida("<--- Restaurante ha recibido cocinado el plato: " + contenido[1]);
                        platosCocinados.add(Plato.valueOf(contenido[1]));
                    } else if (contenido[0].equals("SORRY")) {
                        myGui.presentarSalida("<--- Restaurante NO ha recibido cocinado el plato: " + contenido[1]);
                        contCocinas++;

                        //si hay fallo, vuelvo a añadirlo a la lista para que intente cocinarlo en otra cocina
                        int posplatoAPedir = Plato.valueOf(contenido[1]).ordinal();
                        Plato plato = PLATOS[posplatoAPedir];
                        plato.setAIDcliente(mensaje.getSender());
                        platosPedidos.add(plato);
                        System.out.println("--------HE añadido: " + plato.name());
                    } else {
                        try {
                            myGui.presentarSalida("El cliente: " + mensaje.getSender() + " ha pedido " + contenido[0]);
                            //guardo el plato para proceder a solicitarlo mas tarde
                            int posplatoAPedir = Plato.valueOf(contenido[0]).ordinal();
                            Plato plato = PLATOS[posplatoAPedir];
                            plato.setAIDcliente(mensaje.getSender());
                            platosPedidos.add(plato);
                        } catch (NumberFormatException ex) {
                            // No sabemos tratar el mensaje y los presentamos por consola
                            System.out.println("El agente: " + myAgent.getName()
                                    + " no entiende el contenido del mensaje: \n\t"
                                    + mensaje.getContent() + " enviado por: \n\t"
                                    + mensaje.getSender());
                            myGui.presentarSalida("El agente: " + myAgent.getName()
                                    + " no entiende el contenido del mensaje:"
                                    + mensaje.getContent() + " enviado por:"
                                    + mensaje.getSender());
                        }
                    }
                } else {
                    block();
                }
            } else {
                myAgent.doDelete();
            }
        }
    }

    public class TareaEnvioCocina extends TickerBehaviour {

        public TareaEnvioCocina(AgenteRestaurante aThis,long period) {
            super(aThis,period);
        }

        @Override
        public void onTick() {

            if (!platosPedidos.isEmpty()) {
                //creo estructura mensaje
                ACLMessage mensaje = new ACLMessage(ACLMessage.INFORM);
                //digo quien lo envia
                mensaje.setSender(myAgent.getAID());
                //Se añaden todos los agentes operación
                int numAgentes = listaAgentes[COCINA.ordinal()].size();
                myGui.presentarSalida("Agentes cocina encontrados:" + numAgentes);
                if (numAgentes <= contCocinas) {
                    contCocinas = 0;
                }
                mensaje.addReceiver(listaAgentes[COCINA.ordinal()].get(contCocinas));

                //solicito entrar
                mensaje.setContent(platosPedidos.remove(0).toString());

                myGui.presentarSalida("---> ENVIANDO solicitud a la cocina nº:" + contCocinas + ": " + mensaje.getContent());

                send(mensaje);
            }

        }

    }

//    public class TareaRecibirContestacionCocina extends CyclicBehaviour {
//
//        public TareaRecibirContestacionCocina(AgenteRestaurante aThis) {
//            super(aThis);
//        }
//
//        @Override
//        public void action() {
//
//            MessageTemplate plantilla = MessageTemplate.and(
//                    MessageTemplate.MatchPerformative(ACLMessage.CONFIRM),
//                    MessageTemplate.not(MessageTemplate.MatchSender(agenteDF)));
//            ACLMessage mensaje = myAgent.receive(plantilla);
//
//            if (mensaje != null) {
//                String[] contenido = mensaje.getContent().split(",");
//
//                if (contenido[0].equals("ENVIADO")) {
//                    myGui.presentarSalida("<--- Restaurante ha recibido cocinado el plato: " + contenido[1]);
//                    platosCocinados.add(Plato.valueOf(contenido[1]));
//                } else {
//                    myGui.presentarSalida("<--- Restaurante NO ha recibido cocinado el plato: " + contenido[1]);
//                    contCocinas++;
//                    
//                    //si hay fallo, vuelvo a añadirlo a la lista para que intente cocinarlo en otra cocina
//                    int posplatoAPedir = Plato.valueOf(contenido[1]).ordinal();
//                    Plato plato = PLATOS[posplatoAPedir];
//                    plato.setAIDcliente(mensaje.getSender());
//                    platosPedidos.add(plato);
//                    System.out.println("--------HE añadido: "+plato.name());
//                }
//            }
//
//        }
//
//    }
    public class TareaEnvioCliente extends CyclicBehaviour {

        public TareaEnvioCliente(AgenteRestaurante aThis) {
            super(aThis);
        }

        @Override
        public void action() {
            if (!platosCocinados.isEmpty()) {
                Plato platoAentregar = platosCocinados.remove(0);

                //creo estructura mensaje
                ACLMessage mensaje = new ACLMessage(ACLMessage.CONFIRM);
                //digo quien lo envia
                mensaje.setSender(myAgent.getAID());
                //Se envia el cliente previo
                mensaje.addReceiver(platoAentregar.getAIDcliente());

                //envio el plato
                mensaje.setContent(platoAentregar.toString());

                myGui.presentarSalida("--->  ENVIANDO al cliente: " + platoAentregar.getAIDcliente() + " plato: " + platoAentregar.toString());
                send(mensaje);

                numServicios++;
            }
        }

    }

}
