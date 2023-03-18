/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.ujaen.ssmmaa.agentes;

import clasesAux.Mesa;
import static es.ujaen.ssmmaa.agentes.Constantes.CATEGORIAS;
import es.ujaen.ssmmaa.agentes.Constantes.Comanda;
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
    private AID cliente;
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
        addBehaviour(new TareaEnvioCocina(this));
        addBehaviour(new TareaRecibirContestacionCocina(this));
        addBehaviour(new TareaEnvioCliente(this));

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
                        cliente = mensaje.getSender();

                        ACLMessage respuestaEntrada = new ACLMessage(ACLMessage.INFORM);
                        respuestaEntrada.addReceiver(cliente);
                        respuestaEntrada.setContent("OK");
                        send(respuestaEntrada);
                        addBehaviour(new TareaRecepcion());
                    }// si no hay capacidad, responder que no se puede entrar
                    else {
                        ACLMessage respuestaEntrada = new ACLMessage(ACLMessage.INFORM);
                        respuestaEntrada.addReceiver(mensaje.getSender());
                        respuestaEntrada.setContent("NO");
                        send(respuestaEntrada);
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
            //Recepción de la información para realizar la operación
            MessageTemplate plantilla = MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.INFORM),
                    MessageTemplate.not(MessageTemplate.MatchSender(agenteDF)));
            ACLMessage mensaje = myAgent.receive(plantilla);
            if (mensaje != null) {
                //procesamos el mensaje
                String[] contenido = mensaje.getContent().split(",");

                try {
                    myGui.presentarSalida("El cliente: " + mensaje.getSender() + " ha pedido " + contenido[0]);
                    //guardo el plato para proceder a solicitarlo mas tarde
                    int posplatoAPedir = Plato.valueOf(contenido[0]).ordinal();
                    platosPedidos.add(PLATOS[posplatoAPedir]);
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
            } else {
                block();
            }

        }
    }

    public class TareaEnvioCocina extends CyclicBehaviour {

        public TareaEnvioCocina(AgenteRestaurante aThis) {
            super(aThis);
        }

        @Override
        public void action() {

            if (!platosPedidos.isEmpty()) {
                //creo estructura mensaje
                ACLMessage mensaje = new ACLMessage(ACLMessage.INFORM);
                //digo quien lo envia
                mensaje.setSender(myAgent.getAID());
                //Se añaden todos los agentes operación
                int numAgentes = listaAgentes[COCINA.ordinal()].size();
                myGui.presentarSalida("Agentes cocina encontrados:" + numAgentes);
                if (listaAgentes[COCINA.ordinal()].size() < contCocinas) {
                    contCocinas = 0;
                }
                mensaje.addReceiver(listaAgentes[COCINA.ordinal()].get(contCocinas));

                //solicito entrar
                mensaje.setContent(platosPedidos.remove(0).toString());

                myGui.presentarSalida("---> ENVIANDO a la cocina nº:"+contCocinas+": "+mensaje.getContent());

                send(mensaje);
            }

        }

    }

    public class TareaRecibirContestacionCocina extends CyclicBehaviour {

        public TareaRecibirContestacionCocina(AgenteRestaurante aThis) {
            super(aThis);
        }

        @Override
        public void action() {

            MessageTemplate plantilla = MessageTemplate.and(
                    MessageTemplate.MatchPerformative(ACLMessage.CONFIRM),
                    MessageTemplate.not(MessageTemplate.MatchSender(agenteDF)));
            ACLMessage mensaje = myAgent.receive(plantilla);

            if (mensaje != null) {
                String[] contenido = mensaje.getContent().split(",");
  
                if (contenido[0].equals("ENVIADO")) {
                    myGui.presentarSalida("Restaurante ha recibido cocinado el plato: " + contenido[1]);
                    platosCocinados.add(Plato.valueOf(contenido[1]));
                } else {
                    myGui.presentarSalida("Restaurante NO ha recibido cocinado el plato:");
                }
            }

        }

    }

    public class TareaEnvioCliente extends CyclicBehaviour {

        public TareaEnvioCliente(AgenteRestaurante aThis) {
            super(aThis);
        }

        @Override
        public void action() {
            if (!platosCocinados.isEmpty()) {
                //creo estructura mensaje
                ACLMessage mensaje = new ACLMessage(ACLMessage.CONFIRM);
                //digo quien lo envia
                mensaje.setSender(myAgent.getAID());
                //Se envia el cliente previo
                mensaje.addReceiver(cliente);

                //envio el plato
                Plato platoAentregar=platosCocinados.remove(0);
                mensaje.setContent(platoAentregar.toString());

                myGui.presentarSalida("--->  ENVIANDO al cliente: " + cliente+" plato: "+platoAentregar.toString());
                send(mensaje);
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
