/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.ujaen.ssmmaa.agentes;

import jade.core.AID;
import es.ujaen.ssmmaa.agentes.Constantes.Plato;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.DFSubscriber;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.util.leap.Iterator;
import java.util.ArrayList;

/**
 *
 * @author danie
 */
public class AgenteCliente extends Agent {
    //Variables del agente

    private ArrayList<Plato> servicios;
    private AID agenteRestaurante;

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

        //Busco agentes restaurante
        // Se añaden las tareas principales
        DFAgentDescription dfd = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();
        sd.setType("restaurante");
        dfd.addServices(sd);
        addBehaviour(new TareaSuscripcionDF(this, template));
//        addBehaviour(new CyclicBehaviour(this) {
//            @Override
//            public void action() {
//                //registro paginas amarillas
//
//                try {
//                    DFAgentDescription[] result = DFService.search(myAgent, dfd);
//                    if (result.length > 0) {
//                        agenteRestaurante = result[0].getName();
//                    } else {
//                        System.out.println("Error: no se ha encontrado ningún agente Restaurante.");
//                        doDelete();
//                    }
//                } catch (FIPAException fe) {
//                    System.out.println("Error: " + fe.getMessage());
//                    doDelete();
//                }
//            }
//
//        });
        addBehaviour(new CyclicBehaviour(this) {
            public void action() {
                if (servicios.size() > 0) {
                    // Enviar solicitud de pedido al agente Restaurante
                    ACLMessage solicitudPedido = new ACLMessage(ACLMessage.REQUEST);
                    solicitudPedido.addReceiver(agenteRestaurante);
                    solicitudPedido.setContent(servicios.get(0).toString());
                    send(solicitudPedido);
                    servicios.remove(0);
                } else {
                    // Ya se han pedido todos los platos, terminar el comportamiento
                    stop();
                }
            }
        });
    }

    @Override
    protected void takeDown() {
        //Eliminar registro del agente en las Páginas Amarillas

        //Liberación de recursos, incluido el GUI
        //Despedida
        System.out.println("Finaliza la ejecución del agente: " + this.getName());
    }
    
    

//    private void serviceFinished() {
//        servicioActual++;
//        serviciosCompletados = 0;
//        if (servicioActual < servicios.size()) {
//            addBehaviour(new ComandaBehaviour(servicios.get(servicioActual)));
//        } else {
//            addBehaviour(new ServicioBehaviour());
//        }
//    }
//
//    private class ServicioBehaviour extends SequentialBehaviour {
//
//        private static final long serialVersionUID = 1L;
//
//        public ServicioBehaviour() {
//            for (Plato servicio : servicios) {
//                addSubBehaviour(new ComandaBehaviour(servicio));
//            }
//        }
//
//        @Override
//        public int onEnd() {
//            System.out.println(getAID().getName() + ": Servicios completados");
//            doDelete();
//            return super.onEnd();
//        }
//    }
//
//    private class ComandaBehaviour extends Behaviour {
//
//        private Plato servicio;
//        private boolean platoPreparado = false;
//
//        public ComandaBehaviour(Plato servicio) {
//            this.servicio = servicio;
//        }
//
//        @Override
//        public void action() {
//            if (!platoPreparado) {
//                ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
//                msg.addReceiver(new AID("restaurante", AID.ISLOCALNAME));
//                msg.setContent(servicio.toString());
//                send(msg);
//
//                MessageTemplate mt = MessageTemplate.and(
//                        MessageTemplate.MatchPerformative(ACLMessage.INFORM),
//                        MessageTemplate.MatchSender(new AID("cocina", AID.ISLOCALNAME))
//                );
//                ACLMessage respuesta = blockingReceive(mt);
//                if (respuesta != null) {
//                    System.out.println(getAID().getName() + ": " + servicio + " listo en " + respuesta.getSender().getName());
//                    platoPreparado = true;
//                } else {
//                    block();
//                }
//            } else {
//                serviciosCompletados++;
//                System.out.println(getAID().getName() + ": Servicio " + servicio + " completado");
//                if (serviciosCompletados == servicios.size()) {
//                    myAgent.addBehaviour(new ServicioBehaviour());
//                }
//                serviceFinished();
//            }
//        }
//
//        @Override
//        public boolean done() {
//            return platoPreparado;
//        }
//
//    }
    //Métodos de trabajo del agente
    //Clases internas que representan las tareas del agente
//    public class PedirPlatos extends CyclicBehaviour {
//
//        public PedirPlatos() {
//        }
//
//        @Override
//        public void action() {
//            if (platosPendientes > 0) {
//                // Enviar solicitud de pedido al agente Restaurante
//                ACLMessage solicitudPedido = new ACLMessage(ACLMessage.REQUEST);
//                solicitudPedido.addReceiver(agenteRestaurante);
//                solicitudPedido.setContent(listaPlatos.get(listaPlatos.size() - platosPendientes));
//                send(solicitudPedido);
//                platosPendientes--;
//            } else {
//                // Ya se han pedido todos los platos, terminar el comportamiento
//                stop();
//            }
//        }
//    }
}
