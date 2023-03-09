/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.ujaen.ssmmaa.agentes;

import jade.core.AID;
import es.ujaen.ssmmaa.agentes.Constantes.Plato;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.SequentialBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import java.util.ArrayList;

/**
 *
 * @author danie
 */
public class AgenteCliente extends Agent {
    //Variables del agente

    private ArrayList<Plato> servicios;
    private int serviciosCompletados = 0;
    private int servicioActual;

    @Override
    protected void setup() {
        //Incialización de variables
        servicios = new ArrayList<>();
        //obtengo el argumento
        Object[] args = getArguments();

        if (args != null && args.length > 0) {
            servicios = (ArrayList<Constantes.Plato>) args[0];
            System.out.println(getAID().getName() + ": Mi lista de servicios es " + servicios);
        }
        //inicializacion agente monitor

        // Se añaden las tareas principales
        addBehaviour(new ServicioBehaviour());
    }

    @Override
    protected void takeDown() {
        //Eliminar registro del agente en las Páginas Amarillas

        //Liberación de recursos, incluido el GUI
        //Despedida
        System.out.println("Finaliza la ejecución del agente: " + this.getName());
    }

    private void serviceFinished() {
        servicioActual++;
        serviciosCompletados = 0;
        if (servicioActual < servicios.size()) {
            addBehaviour(new ComandaBehaviour(servicios.get(servicioActual)));
        } else {
            addBehaviour(new ServicioBehaviour());
        }
    }

    private class ServicioBehaviour extends SequentialBehaviour {

        private static final long serialVersionUID = 1L;

        public ServicioBehaviour() {
            for (Plato servicio : servicios) {
                addSubBehaviour(new ComandaBehaviour(servicio));
            }
        }

        @Override
        public int onEnd() {
            System.out.println(getAID().getName() + ": Servicios completados");
            doDelete();
            return super.onEnd();
        }
    }

    private class ComandaBehaviour extends Behaviour {

        private Plato servicio;
        private boolean platoPreparado = false;

        public ComandaBehaviour(Plato servicio) {
            this.servicio = servicio;
        }

        @Override
        public void action() {
            if (!platoPreparado) {
                ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
                msg.addReceiver(new AID("restaurante", AID.ISLOCALNAME));
                msg.setContent(servicio.toString());
                send(msg);

                MessageTemplate mt = MessageTemplate.and(
                        MessageTemplate.MatchPerformative(ACLMessage.INFORM),
                        MessageTemplate.MatchSender(new AID("cocina", AID.ISLOCALNAME))
                );
                ACLMessage respuesta = blockingReceive(mt);
                if (respuesta != null) {
                    System.out.println(getAID().getName() + ": " + servicio + " listo en " + respuesta.getSender().getName());
                    platoPreparado = true;
                } else {
                    block();
                }
            } else {
                serviciosCompletados++;
                System.out.println(getAID().getName() + ": Servicio " + servicio + " completado");
                if (serviciosCompletados == servicios.size()) {
                    myAgent.addBehaviour(new ServicioBehaviour());
                }
                serviceFinished();
            }
        }

        @Override
        public boolean done() {
            return platoPreparado;
        }

    }

    //Métodos de trabajo del agente
    //Clases internas que representan las tareas del agente
}
