/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.ujaen.ssmmaa.agentes;

import static es.ujaen.ssmmaa.agentes.Constantes.CATEGORIAS;
import es.ujaen.ssmmaa.agentes.Constantes.NombreServicio;
import static es.ujaen.ssmmaa.agentes.Constantes.NombreServicio.RESTAURANTE;
import jade.core.AID;
import es.ujaen.ssmmaa.agentes.Constantes.Plato;
import static es.ujaen.ssmmaa.agentes.Constantes.TIPO_SERVICIO;
import es.ujaen.ssmmaa.gui.AgenteClienteJFrame;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
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

/**
 *
 * @author danie
 */
public class AgenteCliente extends Agent {
    //Variables del agente

    private ArrayList<Plato> servicios;
    private AgenteClienteJFrame myGui;
    private AID agenteRestaurante;
    private ArrayList<AID>[] listaAgentes;
    private AID agenteDF;
    private ArrayList<String> mensajesPendientes;

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
        mensajesPendientes = new ArrayList<>();
        //servicios = new ArrayList<>();
        //obtengo el argumento
        //Object[] args = getArguments();
//        if (args != null && args.length > 0) {
//            servicios = (ArrayList<Constantes.Plato>) args[0];
//            System.out.println(getAID().getName() + ": Mi lista de servicios es " + servicios);
//        }
        //Busco agentes restaurante
        // Se añaden las tareas principales
        DFAgentDescription template = new DFAgentDescription();
        ServiceDescription templateSd = new ServiceDescription();
        templateSd.setType(TIPO_SERVICIO);
        template.addServices(templateSd);

        addBehaviour(new TareaSuscripcionDF(this, template));
        addBehaviour(new TareaEnvio());
        //addBehaviour(new TareaMostrar(this, 10000));

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
        System.out.println("Finaliza la ejecución del agente: " + this.getName());
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
                    //myGui.presentarSalida("CATEGORIAS "+sd.getName());
                    if (sd.getName().equals(nombreServicio.name())) {
                        listaAgentes[nombreServicio.ordinal()].add(dfad.getName());
                    }
                }
            }

            // Si hemos localizado agentes de operación activamos el botón de
            // envío
//            if (!listaAgentes[OPERACION.ordinal()].isEmpty()) {
//                myGui.activarEnviar(true);
//            }
//            myGui.presentarSalida("\nEl agente: " + myAgent.getName()
//                    + "ha encontrado a:\n\t" + dfad.getName() + "\n");
            myGui.presentarSalida("El agente: " + myAgent.getName()
                    + "ha encontrado a:\n\t" + Arrays.toString(listaAgentes));
            System.out.println("El agente: " + myAgent.getName()
                    + "ha encontrado a:\n\t" + Arrays.toString(listaAgentes));
        }

        @Override
        public void onDeregister(DFAgentDescription dfad) {
            AID agente = dfad.getName();

            for (NombreServicio servicio : CATEGORIAS) {
                if (listaAgentes[servicio.ordinal()].remove(agente)) {
                    System.out.println("El agente: " + agente.getName()
                            + " ha sido eliminado de la lista de "
                            + myAgent.getName());
                    myGui.presentarSalida("El agente: " + agente.getName()
                            + " ha sido eliminado de la lista de "
                            + myAgent.getName());
                }
            }

            // Si no hay agentes de operación inactivamos el botón de envío
//            if (!listaAgentes[OPERACION.ordinal()].isEmpty()) {
//                myGui.activarEnviar(false);
//            }
        }
    }

    public class TareaEnvio extends OneShotBehaviour {

        @Override
        public void action() {
            //Se envía la operación a todos los agentes restaurante
            
            ACLMessage mensaje = new ACLMessage(ACLMessage.INFORM);
            mensaje.setSender(myAgent.getAID());
            //Se añaden todos los agentes operación
            int numAgentes = listaAgentes[RESTAURANTE.ordinal()].size();
            myGui.presentarSalida("--->  Agentes restaurantee ncontrados:"+numAgentes+"\n");
            for (int i = 0; i < numAgentes; i++) {
                mensaje.addReceiver(listaAgentes[RESTAURANTE.ordinal()].get(i));
            }
            mensaje.setContent("Primer mensaje de prueba");
            myGui.presentarSalida("--->  ENVIANDO: "+mensaje.getContent()+"\n");
            send(mensaje);

            //Se añade el mensaje para la consola
            mensajesPendientes.add("Enviado a: " + numAgentes
                    + " agentes el mensaje: " + mensaje.getContent());

        }

    }

    

    //addBehaviour(new TareaSuscripcionDF(this, template));
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
//        addBehaviour(new CyclicBehaviour(this) {
//            public void action() {
//                if (servicios.size() > 0) {
//                    // Enviar solicitud de pedido al agente Restaurante
//                    ACLMessage solicitudPedido = new ACLMessage(ACLMessage.REQUEST);
//                    solicitudPedido.addReceiver(agenteRestaurante);
//                    solicitudPedido.setContent(servicios.get(0).toString());
//                    send(solicitudPedido);
//                    servicios.remove(0);
//                } else {
//                    // Ya se han pedido todos los platos, terminar el comportamiento
//                    stop();
//                }
//            }
//        });
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
