/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.ujaen.ssmmaa.agentes;

import clasesAux.Mesa;
import static es.ujaen.ssmmaa.agentes.Constantes.CATEGORIAS;
import es.ujaen.ssmmaa.agentes.Constantes.Comanda;
import static es.ujaen.ssmmaa.agentes.Constantes.NombreServicio.CLIENTE;
import static es.ujaen.ssmmaa.agentes.Constantes.NombreServicio.RESTAURANTE;
import es.ujaen.ssmmaa.agentes.Constantes.OrdenComanda;
import static es.ujaen.ssmmaa.agentes.Constantes.TIPO_SERVICIO;
import es.ujaen.ssmmaa.gui.AgenteRestauranteJFrame;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
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
    private int capacidad;
    private int numServicios;
    private int numServiciosActuales = 0;
    private AID agenteDF;
    private List<Mesa> mesas = new ArrayList<>();
    private List<Comanda> comandasPendientes = new ArrayList<>();
    private List<Comanda> comandasEnProceso = new ArrayList<>();

    /**
     * Se ejecuta cuando se inicia el agente
     */
    @Override
    protected void setup() {
        //Configuración del GUI y presentación
        // System.getProperty("java.classpath");

        //obtengo el argumento
        Object[] args = getArguments();

        if (args != null && args.length > 0) {
            String argumento = (String) args[0];
            capacidad = Integer.parseInt(argumento);
            argumento = (String) args[1];
            numServicios = Integer.parseInt(argumento);
        } else {
            System.out.println("Error: el agente Restaurante necesita argumentos para su funcionamiento");
            doDelete();
        }

        agenteDF = new AID("df", AID.ISLOCALNAME); //evitar recibir mensajes de DF
        myGui = new AgenteRestauranteJFrame(this);
        myGui.setVisible(true);
        myGui.presentarSalida("Se inicializa la ejecución de " + this.getName() + "\n");

        // Inicializamos
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

        addBehaviour(new TareaRecepcion());

        //addBehaviour(new TareaRecepcionOperacion(this, dfd));
//            try {
//                DFService.register(this, dfd);
//            } catch (FIPAException fe) {
//                fe.printStackTrace();
//            }
        // Se añaden las tareas principales
//            addBehaviour(new ServicioEntradaBehaviour());
//            addBehaviour(new ServicioPrincipalBehaviour());
//            addBehaviour(new ServicioPostreBehaviour());
//            addBehaviour(new ServicioCuentaBehaviour());
//            // Comportamiento periódico que simula el tiempo de servicio del restaurante
//            addBehaviour(new TickerBehaviour(this, 5000) {
//                public void onTick() {
//                    if (numServiciosActuales < numServicios) {
//                        numServiciosActuales++;
//                        resetMesas();
//                    }
//                }
//            });
    }

//    private void resetMesas() {
//        mesas.clear();
//        for (int i = 0; i < capacidad; i++) {
//            mesas.add(new Mesa(i + 1));
//        }
//    }
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

    public class TareaRecepcion extends CyclicBehaviour {

        @Override
        public void action() {
            //Recepción de la información para realizar la operación
            MessageTemplate plantilla = MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.INFORM),
                    MessageTemplate.not(MessageTemplate.MatchSender(agenteDF)));
            ACLMessage mensaje = myAgent.receive(plantilla);
            if (mensaje != null) {
                //procesamos el mensaje
                String[] contenido = mensaje.getContent().split(",");

                try {
                    System.out.println("Contenido:");
                    myGui.presentarSalida("Contenido:\n");
                    for (int i = 0; i < contenido.length; i++) {
                        System.out.println(contenido[i]);
                        myGui.presentarSalida(contenido[i] + "\n");
                    }
                } catch (NumberFormatException ex) {
                    // No sabemos tratar el mensaje y los presentamos por consola
                    System.out.println("El agente: " + myAgent.getName()
                            + " no entiende el contenido del mensaje: \n\t"
                            + mensaje.getContent() + " enviado por: \n\t"
                            + mensaje.getSender());
                    myGui.presentarSalida("El agente: " + myAgent.getName()
                            + " no entiende el contenido del mensaje: \n\t"
                            + mensaje.getContent() + " enviado por: \n\t"
                            + mensaje.getSender());
                }
            } else {
                block();
            }

        }
    }

//
//    private class ServicioEntradaBehaviour extends Behaviour {
//
//        private MessageTemplate mt;
//        private int step = 0;
//        private int cantidadPlatoEntrante;
//
//        public ServicioEntradaBehaviour(int cantidadPlatoEntrante) {
//            this.cantidadPlatoEntrante = cantidadPlatoEntrante;
//        }
//
//        @Override
//        public void action() {
//            switch (step) {
//                case 0:
//                    // Buscar cocinas disponibles para el plato entrante
//                    DFAgentDescription template = new DFAgentDescription();
//                    ServiceDescription sd = new ServiceDescription();
//                    sd.setType("cocina");
//                    sd.setName(OrdenComanda.ENTRANTE.toString());
//                    template.addServices(sd);
//                    try {
//                        DFAgentDescription[] result = DFService.search(myAgent, template);
//                        if (result.length > 0) {
//                            // Se encontró al menos una cocina disponible
//                            ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
//                            msg.addReceiver(result[0].getName());
//                            msg.setContent(cantidadPlatoEntrante + "");
//                            msg.setConversationId("preparar-plato");
//                            myAgent.send(msg);
//                            mt = MessageTemplate.and(MessageTemplate.MatchConversationId("preparar-plato"),
//                                    MessageTemplate.MatchInReplyTo(msg.getReplyWith()));
//                            step = 1;
//                        } else {
//                            // No se encontró ninguna cocina disponible
//                            step = 2;
//                        }
//                    } catch (FIPAException fe) {
//                        fe.printStackTrace();
//                    }
//                    break;
//                case 1:
//                    // Esperar respuesta de la cocina
//                    ACLMessage respuesta = myAgent.receive(mt);
//                    if (respuesta != null) {
//                        if (respuesta.getPerformative() == ACLMessage.CONFIRM) {
//                            // El plato fue preparado correctamente
//                            cantidadPlatoEntrante--;
//                            if (cantidadPlatoEntrante == 0) {
//                                step = 2;
//                            } else {
//                                step = 0;
//                            }
//                        } else if (respuesta.getPerformative() == ACLMessage.DISCONFIRM) {
//                            // La cocina no pudo preparar el plato
//                            step = 2;
//                        }
//                    } else {
//                        block();
//                    }
//                    break;
//                case 2:
//                    // Liberar mesas que no fueron utilizadas
//                    if (numClientesEsperandoEntrada > 0) {
//                        for (int i = 0; i < mesas.length; i++) {
//                            if (mesas[i] == null) {
//                                numClientesEsperandoEntrada--;
//                                if (numClientesEsperandoEntrada == 0) {
//                                    break;
//                                }
//                            }
//                        }
//                    }
//                    // Finalizar comportamiento
//                    finished = true;
//                    break;
//            }
//        }
//
//        public boolean done() {
//            return finished;
//        }
//    }
//    private class AtenderClientes extends CyclicBehaviour {
//
//        private int serviciosRestantes = serviciosAPreparar;
//        private int usuariosEnRestaurante = 0;
//        private final Random random = new Random();
//
//        public AtenderClientes() {
//        }
//
//        @Override
//        public void action() {
//            // Esperamos a recibir un mensaje
//            ACLMessage msg = receive();
//            if (msg != null) {
//                String contenido = msg.getContent();
//                if (contenido.equals("NuevaComanda")) {
//                    // Recibimos el mensaje de una nueva comanda
//                    if (usuariosEnRestaurante < capacidadMaximaClientes) {
//                        // Aceptar solicitud y enviar mensaje de confirmación
//                        ACLMessage reply = msg.createReply();
//                        reply.setPerformative(ACLMessage.AGREE);
//                        reply.setContent("Solicitud aceptada");
//                        send(reply);
//
//                        // Incrementar usuarios en el restaurante
//                        usuariosEnRestaurante++;
//
//                        try {
//                            // Esperar un tiempo aleatorio para simular atención al cliente
//                            Thread.sleep(random.nextInt(5000) + 3000);
//                        } catch (InterruptedException ex) {
//                            Logger.getLogger(AgenteRestaurante.class.getName()).log(Level.SEVERE, null, ex);
//                        }
//
//                        // Enviar mensaje al agente Cocina indicando que se preparó el plato
//                        ACLMessage msgCocina = new ACLMessage(ACLMessage.INFORM);
//                        msgCocina.addReceiver(new AID("Cocina", AID.ISLOCALNAME));
//                        msgCocina.setContent("Plato preparado");
//                        send(msgCocina);
//
//                        // Decrementar usuarios en el restaurante
//                        usuariosEnRestaurante--;
//
//                        // Verificar si se han completado todos los servicios
//                        serviciosRestantes--;
//                        if (serviciosRestantes == 0) {
//                            System.out.println("Restaurante ha finalizado sus servicios.");
//                            doDelete();
//                        }
//
//                    } else {
//                        // Rechazar solicitud y enviar mensaje de error
//                        ACLMessage reply = msg.createReply();
//                        reply.setPerformative(ACLMessage.REFUSE);
//                        reply.setContent("Restaurante lleno");
//                        send(reply);
//                    }
//                } else {
//                    block();
//                }
//            }
//        }
//    }
//Métodos del agente
//Clases que representan las tareas del agente
//  public class AtenderClientes extends CyclicBehaviour {
//
//        //private int serviciosRestantes = numServicios;
//        //private int usuariosEnRestaurante = 0;
//        //private Random random = new Random();
//
//        public AtenderClientes() {
//        }
//
//        @Override
//        public void action() {
//            // Esperar mensaje de solicitud de cliente
//            ACLMessage msg = receive();
//            if (msg != null) {
//                // Verificar si hay capacidad de atención
//                if (usuariosEnRestaurante < capacidadAtencion) {
//                    // Aceptar solicitud y enviar mensaje de confirmación
//                    ACLMessage reply = msg.createReply();
//                    reply.setPerformative(ACLMessage.AGREE);
//                    reply.setContent("Solicitud aceptada");
//                    send(reply);
//
//                    // Incrementar usuarios en el restaurante
//                    usuariosEnRestaurante++;
//
//                    // Esperar un tiempo aleatorio para simular atención al cliente
//                    try {
//                        Thread.sleep(random.nextInt(5000) + 3000);
//                    } catch (InterruptedException ex) {
//                    }
//
//                    // Enviar mensaje al agente Cocina indicando que se preparó el plato
//                    ACLMessage msgCocina = new ACLMessage(ACLMessage.INFORM);
//                    msgCocina.addReceiver(new AID("Cocina", AID.ISLOCALNAME));
//                    msgCocina.setContent("Plato preparado");
//                    send(msgCocina);
//
//                    // Decrementar usuarios en el restaurante
//                    usuariosEnRestaurante--;
//
//                    // Verificar si se han completado todos los servicios
//                    serviciosRestantes--;
//                    if (serviciosRestantes == 0) {
//                        System.out.println("Restaurante ha finalizado sus servicios.");
//                        doDelete();
//                    }
//
//                } else {
//                    // Rechazar solicitud y enviar mensaje de error
//                    ACLMessage reply = msg.createReply();
//                    reply.setPerformative(ACLMessage.REFUSE);
//                    reply.setContent("Restaurante lleno");
//                    send(reply);
//                }
//            } else {
//                block();
//            }
//        }
//
//    }
}
