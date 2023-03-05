/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.ujaen.ssmmaa.agentes;

import jade.core.Agent;
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
    private int capacidadPreparacion;

    @Override
    protected void setup() {

        //Incialización de variables
        //obtengo el argumento
        Object[] args = getArguments();

        if (args != null && args.length > 0) {
            String argumento = (String) args[0];
            capacidadPreparacion = Integer.parseInt(argumento);
        }
        //inicializacion agente monitor
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType("Cocina");
        sd.setName("Cocina");
        dfd.addServices(sd);
        try {
            DFService.register(this, dfd);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }

        // Se añaden las tareas principales
        addBehaviour(new AgenteCocina.Tarea(this));
    }

    @Override
    protected void takeDown() {
        //Eliminar registro del agente en las Páginas Amarillas

        //Liberación de recursos, incluido el GUI
        //Despedida
        System.out.println("Finaliza la ejecución del agente: " + this.getName());
    }

    //Métodos de trabajo del agente
    public class Tarea extends CyclicBehaviour {

        public Tarea(Agent a) {
            super(a);
        }

        @Override
        public void action() {
            // Esperar mensajes
            ACLMessage msg = receive();
            if (msg != null) {
                // Verificar si es una solicitud de preparación
                if (msg.getPerformative() == ACLMessage.REQUEST) {
                    // Obtener el plato solicitado
                    String plato = msg.getContent();

                    // Verificar si hay disponibilidad para preparar el plato
                    if (capacidadPreparacion > 0) {
                        // Preparar el plato
                        capacidadPreparacion--;

                        // Enviar mensaje de confirmación al restaurante
                        ACLMessage reply = msg.createReply();
                        reply.setPerformative(ACLMessage.INFORM);
                        reply.setContent("Plato " + plato + " preparado.");
                        send(reply);
                    } else {
                        // Enviar mensaje de rechazo al restaurante
                        ACLMessage reply = msg.createReply();
                        reply.setPerformative(ACLMessage.FAILURE);
                        reply.setContent("No hay disponibilidad para preparar el plato " + plato);
                        send(reply);
                    }
                } else {
                    // Mensaje no válido
                    System.out.println("Mensaje no válido recibido por el agente Cocina.");
                }
            } else {
                // No se recibió mensaje
                block();
            }
        }

    }

    //Clases internas que representan las tareas del agente
}
