/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.ujaen.ssmmaa.agentes;

import static es.ujaen.ssmmaa.agentes.Constantes.CATEGORIAS;
import static es.ujaen.ssmmaa.agentes.Constantes.D100;
import es.ujaen.ssmmaa.agentes.Constantes.NombreServicio;
import static es.ujaen.ssmmaa.agentes.Constantes.NombreServicio.CLIENTE;
import static es.ujaen.ssmmaa.agentes.Constantes.NombreServicio.COCINA;
import static es.ujaen.ssmmaa.agentes.Constantes.NombreServicio.RESTAURANTE;
import es.ujaen.ssmmaa.agentes.Constantes.OrdenComanda;
import static es.ujaen.ssmmaa.agentes.Constantes.PLATOS;
import static es.ujaen.ssmmaa.agentes.Constantes.PRIMERO;
import es.ujaen.ssmmaa.agentes.Constantes.Plato;
import static es.ujaen.ssmmaa.agentes.Constantes.TIPO_SERVICIO;
import static es.ujaen.ssmmaa.agentes.Constantes.aleatorio;
import es.ujaen.ssmmaa.gui.AgenteRestauranteJFrame;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.DataStore;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.DFSubscriber;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.FailureException;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.ContractNetInitiator;
import jade.proto.ContractNetResponder;
import jade.proto.ProposeInitiator;
import jade.util.leap.Iterator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import static es.ujaen.ssmmaa.agentes.Constantes.PLATOS;
import es.ujaen.ssmmaa.agentes.Constantes.Plato;

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
    private int numPlatosServicio;
    private ArrayList<Plato> platosPedidos;
    private ArrayList<Plato> platosCocinados;
    private ArrayList<Plato> platosAentregar;
    private ACLMessage reply;
    private int contCocinas = 0;
    private ACLMessage inform;

    /**
     * Se ejecuta cuando se inicia el agente
     */
    @Override
    protected void setup() {
        //Configuración del GUI y presentación
        // System.getProperty("java.classpath");

        listaAgentes = new ArrayList[CATEGORIAS.length];
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
        platosAentregar = new ArrayList<>();
        numPlatosServicio = 0;
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

        addBehaviour(new TareaSuscripcionDF(this, template));
        MessageTemplate mt = MessageTemplate.and(
                MessageTemplate.MatchProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET),
                MessageTemplate.MatchPerformative(ACLMessage.CFP));

        addBehaviour(new TareaResponderServicio(this, mt));

    }

    public class TareaResponderServicio extends ContractNetResponder {

        public TareaResponderServicio(Agent a, MessageTemplate mt) {
            super(a, mt);
        }

        @Override
        protected ACLMessage handleCfp(ACLMessage cfp) throws RefuseException, FailureException, NotUnderstoodException {

            if (numServicios < capacidadServicios) {

                ACLMessage propose = cfp.createReply();
                if (numComensales < capacidadComensales) {
                    propose.setPerformative(ACLMessage.PROPOSE);

                    //obtenemos el precio del servicio
                    String[] platos = cfp.getContent().split(":");
                    float precioServicio = 0;
                    for (int i = 0; i < platos.length; i++) {
                        precioServicio += PLATOS[Plato.valueOf(platos[i]).ordinal()].calcularPrecio(numServicios);
                    }

                    propose.setContent(Float.toString(precioServicio));
                    numComensales++;
                    myGui.presentarSalida("--> Cliente " + cfp.getSender().getName() + " cabe, proponiendo presupuesto...");
                } else {
                    propose.setPerformative(ACLMessage.REFUSE);
                    myGui.presentarSalida("--> Cliente " + cfp.getSender().getName() + " NO cabe");
                }
                return propose;
            } else {
                myGui.presentarSalida("Agente restaurante no tolera mas servicios");
                myAgent.doDelete();
            }
            return null;
        }

        @Override
        protected ACLMessage handleAcceptProposal(ACLMessage cfp, ACLMessage propose, ACLMessage accept) throws FailureException {
            //myGui.presentarSalida("Agent " + getLocalName() + ": Proposal accepted con mensage " + accept);
            myGui.presentarSalida("Cliente: " + cfp.getSender().getName() + " finalmente entró");
            String[] platos = cfp.getContent().split(":");
            if (numPlatosServicio == 0) {
                for (int i = 0; i < platos.length; i++) {
                    Plato plato = PLATOS[Plato.valueOf(platos[i]).ordinal()];
                    plato.setAIDcliente(cfp.getSender());
                    platosPedidos.add(plato);
                    numPlatosServicio++;
                }
            } else {
                ACLMessage reply2 = cfp.createReply();
                myGui.presentarSalida("B");
                reply2.setPerformative(ACLMessage.FAILURE);
                reply2.setContent("FALLO");
                reply2.addReceiver(cfp.getSender());
                return (reply2);
            }

            reply = accept.createReply();
            for (int i = 0; i < numPlatosServicio; i++) {
                //inicio protocolo propose con la cocina
                if (!platosPedidos.isEmpty()) {
                    ACLMessage msg = new ACLMessage(ACLMessage.PROPOSE);
                    msg.setProtocol(FIPANames.InteractionProtocol.FIPA_PROPOSE);
                    msg.setSender(getAID());
                    msg.setContent(platosPedidos.remove(PRIMERO).toString());
                    if (listaAgentes[COCINA.ordinal()].size() > 0) {
                        java.util.Iterator<AID> it = listaAgentes[COCINA.ordinal()].iterator();
                        int cocinaAenviar;
                        int numAgentes = listaAgentes[COCINA.ordinal()].size();
                        myGui.presentarSalida("Agentes cocina encontrados:" + numAgentes);
                        if (numAgentes <= contCocinas) {
                            contCocinas = 0;
                        }

                        //aqui aplico un algoritmo donde reparto la presion selectiva, priorizaré el 90% de las veces repartir aleatoriamente los pedidos entre las cocinas
                        //sin embargo, dare oportunidad a cambiar la politica de reparto en caso de que las cocinas esten sobresaturadas
                        if (aleatorio.nextInt(D100) > 80) {
                            cocinaAenviar = contCocinas;
                        } else {
                            cocinaAenviar = aleatorio.nextInt(numAgentes);
                        }

                        msg.addReceiver(listaAgentes[COCINA.ordinal()].get(cocinaAenviar));
                        //solicito a la cocina cocinar el plato
                        addBehaviour(new TareaSolicitarCocinado(myAgent, msg));
                    }
                }
            }

            return null;
        }

    }

    public class TareaSolicitarCocinado extends ProposeInitiator {

        public TareaSolicitarCocinado(Agent a, ACLMessage initiation) {
            super(a, initiation);
            myGui.presentarSalida("Se va enviar a la cocina: " + initiation.getContent());
        }

        @Override
        protected void handleAcceptProposal(ACLMessage accept_proposal) {
            myGui.presentarSalida("<--- Restaurante ha recibido cocinado el plato: " + accept_proposal.getContent());
            platosCocinados.add(Plato.valueOf(accept_proposal.getContent()));

            // Se han recibido platos cocinados, realizar la lógica correspondiente
            platosAentregar.add(platosCocinados.remove(PRIMERO));
            myGui.presentarSalida("--->  Preparando en la bandeja de: " + platosAentregar.get(platosAentregar.size() - 1).getAIDcliente().getName() + " plato: " + platosAentregar.get(platosAentregar.size() - 1).toString());
            //myGui.presentarSalida("pla Entr: "+platosAentregar.size()+", numPlServ: "+numPlatosServicio);
            if (platosAentregar.size() == numPlatosServicio) {

                // Crear estructura del mensaje
                inform = reply;
                inform.setPerformative(ACLMessage.INFORM);
                inform.setContent(platosAentregar.toString());
                // Enviar el plato al cliente
                inform.addReceiver(platosAentregar.get(PRIMERO).getAIDcliente());
                // Incrementar contador de servicios
                numServicios++;
                // Finalizar el comportamiento
                numPlatosServicio = 0;
                platosAentregar.clear();
                numComensales--;
                send(inform);
            }

        }

        @Override
        protected void handleRejectProposal(ACLMessage reject_proposal) {
            myGui.presentarSalida("<--- Restaurante NO ha recibido cocinado el plato: " + reject_proposal.getContent());
            contCocinas++;

        }

    }

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
//                    System.out.println("El agente: " + agente.getName()
//                            + " ha sido eliminado de la lista de "
//                            + myAgent.getName());
                    myGui.presentarSalida("El agente: " + agente.getName()
                            + " ha sido eliminado de la lista de "
                            + myAgent.getName());
                }
            }
        }
    }
}
