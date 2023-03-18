/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.ujaen.ssmmaa.agentes;

import static es.ujaen.ssmmaa.agentes.Constantes.CATEGORIAS;
import es.ujaen.ssmmaa.agentes.Constantes.NombreServicio;
import static es.ujaen.ssmmaa.agentes.Constantes.NombreServicio.CLIENTE;
import static es.ujaen.ssmmaa.agentes.Constantes.NombreServicio.RESTAURANTE;
import jade.core.AID;
import es.ujaen.ssmmaa.agentes.Constantes.Plato;
import static es.ujaen.ssmmaa.agentes.Constantes.Plato.pedirPlato;
import static es.ujaen.ssmmaa.agentes.Constantes.TIPO_SERVICIO;
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
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.MessageTemplate;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author danie
 */
public class AgenteCliente extends Agent {
    //Variables del agente

    private ArrayList<String> servicios;
    private static boolean heEntrado;
    private AgenteClienteJFrame myGui;
    private AID agenteRestaurante;
    private ArrayList<AID>[] listaAgentes;
    private AID agenteDF;

    private int cont = 0;

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
        servicios = new ArrayList<>();
        heEntrado = false;
  
        //obtengo el argumento
        Object[] args = getArguments();
        if (args != null && args.length > 0) {
            //System.out.println("HAY: " + args.length);
            for (int i = 0; i < args.length; i++) {
                servicios.add((String) args[i]);
            }
            //System.out.println(getAID().getName() + ": Mi lista de servicios es " + servicios);
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
        addBehaviour(new TareaEnvioSolicitudEntradaRestaurante(this, 2000));
        addBehaviour(new TareaRecibirContestacionRestaurante(this));
        addBehaviour(new TareaPedirPlatos(this, 12000));
        addBehaviour(new TareaRecibirPlato(this));

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
        myGui.dispose();
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
                    System.out.println("El agente: " + agente.getName()
                            + " ha sido eliminado de la lista de "
                            + myAgent.getName());
                    myGui.presentarSalida("El agente: " + agente.getName()
                            + " ha sido eliminado de la lista de "
                            + myAgent.getName());
                }
            }
        }
    }

    public class TareaEnvioSolicitudEntradaRestaurante extends TickerBehaviour {

        public TareaEnvioSolicitudEntradaRestaurante(Agent a, long period) {
            super(a, period);
        }

        @Override
        public void onTick() {

            //Se envía la operación a todos los agentes restaurante
            if (!heEntrado) {
                if (listaAgentes[RESTAURANTE.ordinal()].size() > 0) {

                    //creo estructura mensaje
                    ACLMessage mensaje = new ACLMessage(ACLMessage.REQUEST);
                    //digo quien lo envia
                    mensaje.setSender(myAgent.getAID());
                    //Se añaden todos los agentes operación
                    int numAgentes = listaAgentes[RESTAURANTE.ordinal()].size();
                    myGui.presentarSalida("Agentes restaurante encontrados:" + numAgentes + "\n");
                    if (listaAgentes[RESTAURANTE.ordinal()].size() < cont) {
                        cont = 0;
                    }
                    mensaje.addReceiver(listaAgentes[RESTAURANTE.ordinal()].get(cont));

                    //solicito entrar
                    mensaje.setContent("Cliente," + getAID().toString() + ",solicita entrar");

                    myGui.presentarSalida("--> Solicitud entrar a Restaurante nº: "+cont);
                    send(mensaje);

                }
            }

        }
    }

    public class TareaRecibirContestacionRestaurante extends CyclicBehaviour {

        public TareaRecibirContestacionRestaurante(AgenteCliente aThis) {
            super(aThis);
        }

        @Override
        public void action() {
            if (!heEntrado) {
                MessageTemplate plantilla = MessageTemplate.and(
                        MessageTemplate.MatchPerformative(ACLMessage.INFORM),
                        MessageTemplate.not(MessageTemplate.MatchSender(agenteDF)));
                ACLMessage mensaje = myAgent.receive(plantilla);

                if (mensaje != null) {
                    String[] contenido = mensaje.getContent().split(",");
                    //myGui.presentarSalida("CONTENIDO: " + contenido[0] + "\n");
                    if (contenido[0].equals("OK")) {
                        myGui.presentarSalida("<-- He entrado al restaurante nº: "+cont);
                        heEntrado = true;
                        agenteRestaurante=mensaje.getSender();

                    } else {
                        myGui.presentarSalida("<-- NO He entrado al restaurante nº: "+cont);
                        cont++;
                    }
                }
            }
        }

    }

    public class TareaPedirPlatos extends TickerBehaviour {

        public TareaPedirPlatos(Agent a, long period) {
            super(a, period);
        }

        @Override
        public void onTick() {
            if (heEntrado) {
                //Plato platoPedido = Plato.pedirPlato();
                if (!servicios.isEmpty()) {
                    String platoPedido = servicios.remove(0);
                    System.out.println("Cliente: " + getAID() + " pide: " + platoPedido);

                    //creo estructura mensaje
                    ACLMessage mensaje = new ACLMessage(ACLMessage.INFORM);
                    //digo quien lo envia
                    mensaje.setSender(myAgent.getAID());
                    //Se añade el agenteRestaurante al cual entro el cliente

                    mensaje.addReceiver(agenteRestaurante);

                    //solicito el plato
                    mensaje.setContent(platoPedido);

                    myGui.presentarSalida("--> Solicitud del plato: " + mensaje.getContent() + "\n");
                    send(mensaje);
                } else {
                    //borrar el cliente
                    myGui.presentarSalida("AGENTE YA HA ACABADO DE COMER TODOS SUS PLATOS");
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(AgenteCliente.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    myAgent.doDelete();
                }
            }

        }
    }

    public class TareaRecibirPlato extends CyclicBehaviour {

        public TareaRecibirPlato(AgenteCliente aThis) {
            super(aThis);
        }

        @Override
        public void action() {
            if (heEntrado) {
                MessageTemplate plantilla = MessageTemplate.and(
                        MessageTemplate.MatchPerformative(ACLMessage.CONFIRM),
                        MessageTemplate.not(MessageTemplate.MatchSender(agenteDF)));
                ACLMessage mensaje = myAgent.receive(plantilla);

                if (mensaje != null) {
                    String[] contenido = mensaje.getContent().split(",");
                    myGui.presentarSalida("***************************");
                    myGui.presentarSalida("Comiendo: "+contenido[0]);
                    myGui.presentarSalida("***************************");
                }
            }
        }
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

