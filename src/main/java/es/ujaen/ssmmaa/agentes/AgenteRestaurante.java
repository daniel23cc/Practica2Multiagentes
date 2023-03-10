/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.ujaen.ssmmaa.agentes;

import es.ujaen.ssmmaa.gui.AgenteRestauranteJFrame;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import java.util.ArrayList;
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
    //private AgenteRestauranteJFrame myGui;
    // Capacidad máxima de comandas que podemos recibir simultáneamente
    private int capacidad;
    private int numServicios;
    private int numServiciosActuales = 0;
    
    private List<Mesa> mesas = new ArrayList<>();
    private List<Comanda> comandasPendientes = new ArrayList<>();
    private List<Comanda> comandasEnProceso = new ArrayList<>();

    /**
     * Se ejecuta cuando se inicia el agente
     */
    @Override
    protected void setup() {
        //Configuración del GUI y presentación
        System.getProperty("java.classpath");

        //myGui = new AgenteRestauranteJFrame(this);
        //myGui.setVisible(true);
        //myGui.presentarSalida("Se inicializa la ejecución de " + this.getName() + "\n");
        //Incialización de variables
        //obtengo el argumento
        Object[] args = getArguments();

        if (args != null && args.length > 0) {
            String argumento = (String) args[0];
            serviciosAPreparar = Integer.parseInt(argumento);
            argumento = (String) args[1];
            capacidadMaximaClientes = Integer.parseInt(argumento);
        } else {
            // Si no se han proporcionado argumentos, se usa un valor por defecto
            serviciosAPreparar = 5;
            System.out.println("AgenteRestaurante - Cantidad de comandas a preparar por defecto: " + serviciosAPreparar);
        }
        // Inicializamos
        usuariosEnRestaurante = 0;
        comandasPreparando = 0;
        //inicializacion agente monitor
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType("restaurante");
        sd.setName("restaurante");
        dfd.addServices(sd);
        try {
            DFService.register(this, dfd);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }

        // Se añaden las tareas principales
        addBehaviour(new AtenderClientes());
    }

    /**
     * Se ejecuta al finalizar el agente
     */
    @Override
    protected void takeDown() {
        //Desregistro de las Páginas Amarillas
        try {
            DFService.deregister(this);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }

        //Se liberan los recuros y se despide
        //myGui.dispose();
        System.out.println("Finaliza la ejecución de " + this.getName());
    }

    private class AtenderClientes extends CyclicBehaviour {
        private int serviciosRestantes = serviciosAPreparar;
        private int usuariosEnRestaurante = 0;
        private final Random random = new Random();

        public AtenderClientes() {
        }

        @Override
        public void action() {
            // Esperamos a recibir un mensaje
            ACLMessage msg = receive();
            if (msg != null) {
                String contenido = msg.getContent();
                if (contenido.equals("NuevaComanda")) {
                    // Recibimos el mensaje de una nueva comanda
                    if (usuariosEnRestaurante < capacidadMaximaClientes) {
                        // Aceptar solicitud y enviar mensaje de confirmación
                        ACLMessage reply = msg.createReply();
                        reply.setPerformative(ACLMessage.AGREE);
                        reply.setContent("Solicitud aceptada");
                        send(reply);

                        // Incrementar usuarios en el restaurante
                        usuariosEnRestaurante++;

                        try {
                            // Esperar un tiempo aleatorio para simular atención al cliente
                            Thread.sleep(random.nextInt(5000) + 3000);
                        } catch (InterruptedException ex) {
                            Logger.getLogger(AgenteRestaurante.class.getName()).log(Level.SEVERE, null, ex);
                        }

                        // Enviar mensaje al agente Cocina indicando que se preparó el plato
                        ACLMessage msgCocina = new ACLMessage(ACLMessage.INFORM);
                        msgCocina.addReceiver(new AID("Cocina", AID.ISLOCALNAME));
                        msgCocina.setContent("Plato preparado");
                        send(msgCocina);

                        // Decrementar usuarios en el restaurante
                        usuariosEnRestaurante--;

                        // Verificar si se han completado todos los servicios
                        serviciosRestantes--;
                        if (serviciosRestantes == 0) {
                            System.out.println("Restaurante ha finalizado sus servicios.");
                            doDelete();
                        }

                    } else {
                        // Rechazar solicitud y enviar mensaje de error
                        ACLMessage reply = msg.createReply();
                        reply.setPerformative(ACLMessage.REFUSE);
                        reply.setContent("Restaurante lleno");
                        send(reply);
                    }
                } else {
                    block();
                }
            }
        }
    }
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
