/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.ujaen.ssmmaa.agentes;

import es.ujaen.ssmmaa.gui.AgenteRestauranteJFrame;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

/**
 *
 * @author danie
 */
public class AgenteRestaurante extends Agent {

    // Constantes
    public static final long TIEMPO_CICLO = 5000; // 10 seg.

    // Variables del agente
    private AgenteRestauranteJFrame myGui;

    private int capacidadMaxima;
    private int numServiciosRestantes;
    //private AgenteMonitor agMonitor;

    /**
     * Se ejecuta cuando se inicia el agente
     */
    @Override
    protected void setup() {
        //Configuración del GUI y presentación
        System.getProperty("java.classpath");
        myGui = new AgenteRestauranteJFrame(this);
        myGui.setVisible(true);
        myGui.presentarSalida("Se inicializa la ejecución de " + this.getName() + "\n");

        //Incialización de variables
        //obtengo el argumento
        Object[] args = getArguments();

        if (args != null && args.length > 0) {
            String argumento = (String) args[0];
            capacidadMaxima = Integer.parseInt(argumento);
            argumento = (String) args[1];
            numServiciosRestantes = Integer.parseInt(argumento);
        }
        //inicializacion agente monitor
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType("Restaurante");
        sd.setName("Restaurante");
        dfd.addServices(sd);
        try {
            DFService.register(this, dfd);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }

        // Se añaden las tareas principales
        addBehaviour(new Tarea());
    }

    /**
     * Se ejecuta al finalizar el agente
     */
    @Override
    protected void takeDown() {
        numServiciosRestantes--;
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

    //Métodos del agente
    //Clases que representan las tareas del agente
    public class Tarea extends CyclicBehaviour {

        public Tarea() {
        }

        @Override
        public void action() {
            // Recibir mensajes de los clientes
            MessageTemplate template = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
            ACLMessage msg = myAgent.receive(template);
            if (msg != null) {
                // Comprobar si hay disponibilidad de espacio en el restaurante
                if (capacidadMaxima > 0) {
                    // Atender la petición del cliente y reservar el espacio en el restaurante
                    ACLMessage reply = msg.createReply();
                    reply.setPerformative(ACLMessage.INFORM);
                    reply.setContent("Bienvenido al restaurante. Por favor, tome asiento.");
                    send(reply);
                    capacidadMaxima--;
                } else {
                    // Informar al cliente de que no hay disponibilidad de espacio en el restaurante
                    ACLMessage reply = msg.createReply();
                    reply.setPerformative(ACLMessage.REFUSE);
                    reply.setContent("Lo siento, el restaurante está lleno. Vuelva a intentarlo más tarde.");
                    send(reply);
                }
            } else {
                // Si no se recibe ningún mensaje, el comportamiento se bloquea
                block();
            }
        }

    }

}
