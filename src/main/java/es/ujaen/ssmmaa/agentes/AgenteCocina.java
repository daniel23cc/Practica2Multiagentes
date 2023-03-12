/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.ujaen.ssmmaa.agentes;

import static es.ujaen.ssmmaa.agentes.Constantes.TIPO_SERVICIO;
import es.ujaen.ssmmaa.gui.AgenteCocinaJFrame;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;

/**
 *
 * @author danie
 */
public class AgenteCocina extends Agent {

    //Variables del agente
    // Parámetros del agente
    private int capacidadPlatos;

    // Contador de platos preparados
    private int platosPreparados;
    private AgenteCocinaJFrame myGui;

    @Override
    protected void setup() {
        myGui = new AgenteCocinaJFrame(this);
        myGui.setVisible(true);
        myGui.presentarSalida("Se inicializa la ejecución de " + this.getName() + "\n");
        //obtengo el argumento
        Object[] args = getArguments();

        if (args != null && args.length > 0) {
            String argumento = (String) args[0];
            capacidadPlatos = Integer.parseInt(argumento);
            myGui.presentarSalida("Capacidad " + capacidadPlatos + "\n");
        }
        
        
        // Inicializamos el contador de platos preparados
        platosPreparados = 0;
        //inicializacion agente cocina
        DFAgentDescription template = new DFAgentDescription();
        ServiceDescription templateSd = new ServiceDescription();
        templateSd.setType(TIPO_SERVICIO);
        template.addServices(templateSd);
        try {
            DFService.register(this, template);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }

        // Se añaden las tareas principales
        addBehaviour(new AgenteCocina.Tarea(this));
    }

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

//    protected void recibirSolicitud() {
//        ACLMessage msg = receive();
//        if (msg != null) {
//            String content = msg.getContent();
//            String senderName = msg.getSender().getName();
//            System.out.println("AgenteCocina " + getAID().getName() + " ha recibido una solicitud de " + senderName + " para preparar el plato " + content);
//            if (cantidadPlatos > 0) {
//                ACLMessage reply = msg.createReply();
//                reply.setPerformative(ACLMessage.INFORM);
//                reply.setContent("El plato " + content + " ha sido preparado.");
//                send(reply);
//                cantidadPlatos--;
//            } else {
//                ACLMessage reply = msg.createReply();
//                reply.setPerformative(ACLMessage.REFUSE);
//                reply.setContent("Lo siento, no hay suficientes ingredientes para preparar el plato " + content + ".");
//                send(reply);
//            }
//        } 
//    }
//    //Métodos de trabajo del agente
//    public class Tarea extends CyclicBehaviour {
//
//        public Tarea(Agent a) {
//            super(a);
//        }
//
//        @Override
//        public void action() {
//            // Esperar mensajes
//            ACLMessage msg = receive();
//            if (msg != null) {
//                // Verificar si es una solicitud de preparación
//                if (msg.getPerformative() == ACLMessage.REQUEST) {
//                    // Obtener el plato solicitado
//                    String plato = msg.getContent();
//
//                    // Verificar si hay disponibilidad para preparar el plato
//                    if (capacidadPreparacion > 0) {
//                        // Preparar el plato
//                        capacidadPreparacion--;
//
//                        // Enviar mensaje de confirmación al restaurante
//                        ACLMessage reply = msg.createReply();
//                        reply.setPerformative(ACLMessage.INFORM);
//                        reply.setContent("Plato " + plato + " preparado.");
//                        send(reply);
//                    } else {
//                        // Enviar mensaje de rechazo al restaurante
//                        ACLMessage reply = msg.createReply();
//                        reply.setPerformative(ACLMessage.FAILURE);
//                        reply.setContent("No hay disponibilidad para preparar el plato " + plato);
//                        send(reply);
//                    }
//                } else {
//                    // Mensaje no válido
//                    System.out.println("Mensaje no válido recibido por el agente Cocina.");
//                }
//            } else {
//                // No se recibió mensaje
//                block();
//            }
//        }
//
//    }
    //Clases internas que representan las tareas del agente
    public class Tarea extends Behaviour {

        public Tarea(AgenteCocina aThis) {
            super(aThis);
        }

        @Override
        public void action() {
            // Esperamos a recibir un mensaje
            ACLMessage msg = receive();
            if (msg != null) {
                String contenido = msg.getContent();
                if (contenido.equals("NuevaComanda")) {
                    // Recibimos el mensaje de una nueva comanda
                    if (platosPreparados < capacidadPlatos) {
                        // Si tenemos capacidad para preparar más platos, enviamos un mensaje al AgenteRestaurante para preparar un plato
                        System.out.println("AgenteCocina - Preparando plato para la comanda.");
                        ACLMessage mensaje = new ACLMessage(ACLMessage.REQUEST);
                        mensaje.addReceiver(new AID("restaurante", AID.ISLOCALNAME));
                        mensaje.setContent("Preparar:Plato" + (platosPreparados + 1));
                        send(mensaje);
                        // Incrementamos el contador de platos preparados
                        platosPreparados++;
                    } else {
                        // Si no tenemos capacidad para preparar más platos, respondemos al AgenteRestaurante que no podemos preparar más platos
                        System.out.println("AgenteCocina - No podemos preparar más platos.");
                        ACLMessage respuesta = new ACLMessage(ACLMessage.REFUSE);
                        respuesta.addReceiver(new AID("restaurante", AID.ISLOCALNAME));
                        respuesta.setContent("CapacidadExcedida");
                        send(respuesta);
                    }
                } else if (contenido.startsWith("PlatoPreparado")) {
                    // Recibimos el mensaje de que un plato ha sido preparado
                    String plato = contenido.substring(15);
                    System.out.println("AgenteCocina - " + plato + " preparado.");
                } else {
                    // Si no es ninguna de las opciones anteriores, responder que no se entiende el mensaje
                    System.out.println("AgenteCocina - No entiendo el mensaje recibido.");
                    ACLMessage respuesta = new ACLMessage(ACLMessage.NOT_UNDERSTOOD);
                    respuesta.addReceiver(msg.getSender());
                    send(respuesta);
                }
            } else {
                // Si no hemos recibido ningún mensaje, bloqueamos el comportamiento hasta que llegue uno nuevo
                block();
            }
        }

        @Override
        public boolean done() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }
    }
}
