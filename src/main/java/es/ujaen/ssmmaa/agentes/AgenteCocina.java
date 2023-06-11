/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.ujaen.ssmmaa.agentes;

import auxiliares.Resultado;
import static es.ujaen.ssmmaa.agentes.Constantes.CATEGORIAS;
import static es.ujaen.ssmmaa.agentes.Constantes.MAX_TIEMPO_COCINADO;
import static es.ujaen.ssmmaa.agentes.Constantes.MIN_TIEMPO_COCINADO;
import static es.ujaen.ssmmaa.agentes.Constantes.NombreServicio.COCINA;
import static es.ujaen.ssmmaa.agentes.Constantes.NombreServicio.RESTAURANTE;
import es.ujaen.ssmmaa.agentes.Constantes.OrdenComanda;
import static es.ujaen.ssmmaa.agentes.Constantes.PLATOS;
import es.ujaen.ssmmaa.agentes.Constantes.Plato;
import static es.ujaen.ssmmaa.agentes.Constantes.TIPO_SERVICIO;
import static es.ujaen.ssmmaa.agentes.Constantes.aleatorio;
import es.ujaen.ssmmaa.gui.AgenteCocinaJFrame;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.WakerBehaviour;
import jade.domain.DFService;
import jade.domain.DFSubscriber;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.ProposeResponder;
import jade.util.leap.Iterator;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author danie
 */
public class AgenteCocina extends Agent {

    //Variables del agente
    // Parámetros del agente
    private int capacidadPlatos;
    // Creamos un HashSet para almacenar los diferentes tipos de OrdenComanda
    private Set<OrdenComanda> tiposOrdenComanda;
    private Map<String, Integer> comandasDisponiblesPorOrdenComanda;
    private ArrayList<AID>[] listaAgentes;
    private AID agenteDF;
    // Contador de platos preparados
    private int platosPreparados;
    private AgenteCocinaJFrame myGui;

    private Resultado resultado;

    @Override
    protected void setup() {
        myGui = new AgenteCocinaJFrame(this);
        myGui.setVisible(true);
        myGui.presentarSalida("Se inicializa la ejecución de " + this.getName() + "\n");

        listaAgentes = new ArrayList[CATEGORIAS.length];
        agenteDF = new AID("df", AID.ISLOCALNAME); //evitar recibir mensajes de DF
        for (Constantes.NombreServicio categoria : CATEGORIAS) {
            listaAgentes[categoria.ordinal()] = new ArrayList<>();
        }
        //obtengo el argumento
        Object[] args = getArguments();

        if (args != null && args.length > 0) {
            String argumento = (String) args[0];
            capacidadPlatos = Integer.parseInt(argumento);
            myGui.presentarSalida("Cantidad que puedo preparar de cada tipo: " + capacidadPlatos + "\n");
            resultado = (Resultado) args[1];
        }

        // Inicializamos variablles
        platosPreparados = 0;
        tiposOrdenComanda = new HashSet<>();
        comandasDisponiblesPorOrdenComanda = new HashMap<>();

        // Iteramos sobre los elementos del enum Plato
        for (Plato p : Plato.values()) {
            // Añadimos el tipo de OrdenComanda de este Plato al HashSet
            tiposOrdenComanda.add(p.getOrdenComanda());
        }

        // Creamos un array de String con los elementos del HashSet
        String[] tiposOrdenComandaArray = tiposOrdenComanda.stream()
                .map(OrdenComanda::name)
                .toArray(String[]::new);

        for (int i = 0; i < tiposOrdenComandaArray.length; i++) {
            comandasDisponiblesPorOrdenComanda.put(tiposOrdenComandaArray[i], capacidadPlatos);
        }

        //inicializacion agente cocina
        DFAgentDescription template = new DFAgentDescription();
        template.setName(getAID());
        ServiceDescription templateSd = new ServiceDescription();
        templateSd.setType(TIPO_SERVICIO);
        templateSd.setName(COCINA.name());
        template.addServices(templateSd);
        try {
            DFService.register(this, template);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }

        //Busco agentes restaurante
        // Se añaden las tareas principales
        DFAgentDescription template2 = new DFAgentDescription();
        ServiceDescription templateSd2 = new ServiceDescription();
        templateSd2.setType(TIPO_SERVICIO);
        templateSd2.setName(RESTAURANTE.name());
        template2.addServices(templateSd2);

        // Se añaden las tareas principales
        // long delay = 5000; // Retraso de 5 segundos
        //addBehaviour(new MiWakerBehaviour(this, delay));
        addBehaviour(new TareaSuscripcionDF(this, template2));
        MessageTemplate mt = MessageTemplate.and(
                MessageTemplate.MatchProtocol(FIPANames.InteractionProtocol.FIPA_PROPOSE),
                MessageTemplate.MatchPerformative(ACLMessage.PROPOSE));
        addBehaviour(new TareaEntradaComandas(this, mt));

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

                for (Constantes.NombreServicio nombreServicio : CATEGORIAS) {
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

            for (Constantes.NombreServicio servicio : CATEGORIAS) {
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

    public class TareaEntradaComandas extends ProposeResponder {

        public TareaEntradaComandas(Agent a, MessageTemplate mt) {
            super(a, mt);
        }

        @Override
        protected ACLMessage prepareResponse(ACLMessage propose) throws NotUnderstoodException, RefuseException {

            ACLMessage respuesta = propose.createReply();

            String[] contenido = propose.getContent().split(",");
            myGui.presentarSalida("<-- Recibida una solicitud de cocinar: " + contenido[0]);
            //Compruebo de que tipo es el plato(entrante,principal o postre)
            String tipoComanda = Plato.valueOf(contenido[0]).getOrdenComanda().name();

            int comandasDisp = comandasDisponiblesPorOrdenComanda.get(tipoComanda);
            if (comandasDisp > 0) {//si todavia la cocina tiene capacidad de cocinar ese tipo de plato lo hace, sino dice al restaurante que no puede

                respuesta.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
                respuesta.setContent(contenido[0]);
                comandasDisp--;
                comandasDisponiblesPorOrdenComanda.put(contenido[1], comandasDisp);

                myGui.presentarSalida("Cocina cocinando el plato: " + contenido[0] + "...");
                try {
                    int tiempoPreparacion = aleatorio.nextInt(MAX_TIEMPO_COCINADO - MIN_TIEMPO_COCINADO) + MIN_TIEMPO_COCINADO;
                    myGui.presentarSalida("((Necesito " + tiempoPreparacion + " milisegundos))");
                    Thread.sleep(tiempoPreparacion);
                } catch (InterruptedException ex) {
                    Logger.getLogger(AgenteCocina.class.getName()).log(Level.SEVERE, null, ex);
                }
                myGui.presentarSalida("Cocina YA HA cocinado el plato: " + contenido[0]);
                myGui.presentarSalida("Aun puedo preparar: " + comandasDisp + " más del tipo: " + tipoComanda);
                myGui.presentarSalida("--> Enviando el plato al restaurante");

                //aumento la caja de lo que genera la cocina
                resultado.agregarDineroGenerado(PLATOS[Plato.valueOf(contenido[0]).ordinal()].getPrecio());
                myGui.presentarSalida("La caja va por: " + resultado.getCajaTotal() + "\n");

            }// si no hay comandas disponibles, la cocina no puede atender mas platos de ese tipo
            else {
                //ACLMessage respuestaCocina = new ACLMessage(ACLMessage.INFORM);
                // respuestaCocina.addReceiver(mensaje.getSender());
                respuesta.setPerformative(ACLMessage.REJECT_PROPOSAL);
                respuesta.setContent(contenido[0]);

            }

            return respuesta;
        }

    }

//    public class TareaEntradaComandas extends CyclicBehaviour {
//
//        public TareaEntradaComandas(AgenteCocina aThis) {
//            super(aThis);
//        }
//
//        @Override
//        public void action() {
//
//            MessageTemplate plantilla = MessageTemplate.and(
//                    MessageTemplate.MatchPerformative(ACLMessage.INFORM),
//                    MessageTemplate.not(MessageTemplate.MatchSender(agenteDF)));
//            ACLMessage mensaje = myAgent.receive(plantilla);
//
//            if (mensaje != null) {
//                String[] contenido = mensaje.getContent().split(",");
//                myGui.presentarSalida("<-- Recibida una solicitud de cocinar: " + contenido[0]);
//                //Compruebo de que tipo es el plato(entrante,principal o postre)
//                String tipoComanda = Plato.valueOf(contenido[0]).getOrdenComanda().name();
//
//                int comandasDisp = comandasDisponiblesPorOrdenComanda.get(tipoComanda);
//                if (comandasDisp > 0) {//si todavia la cocina tiene capacidad de cocinar ese tipo de plato lo hace, sino dice al restaurante que no puede
//
//                    ACLMessage respuestaCocina = new ACLMessage(ACLMessage.INFORM);
//                    respuestaCocina.addReceiver(mensaje.getSender());
//                    respuestaCocina.setContent("ENVIADO," + contenido[0]);
//                    comandasDisp--;
//                    comandasDisponiblesPorOrdenComanda.put(contenido[1], comandasDisp);
//
//                    myGui.presentarSalida("Cocina cocinando el plato: " + contenido[0] + "...");
//                    try {
//                        int tiempoPreparacion = aleatorio.nextInt(MAX_TIEMPO_COCINADO - MIN_TIEMPO_COCINADO) + MIN_TIEMPO_COCINADO;
//                        myGui.presentarSalida("((Necesito " + tiempoPreparacion + " milisegundos))");
//                        Thread.sleep(tiempoPreparacion);
//                    } catch (InterruptedException ex) {
//                        Logger.getLogger(AgenteCocina.class.getName()).log(Level.SEVERE, null, ex);
//                    }
//                    myGui.presentarSalida("Cocina YA HA cocinado el plato: " + contenido[0]);
//                    myGui.presentarSalida("Aun puedo preparar: " + comandasDisp + " más del tipo: " + tipoComanda);
//                    myGui.presentarSalida("--> Enviando el plato al restaurante");
//
//                    //aumento la caja de lo que genera la cocina
//                    resultado.agregarDineroGenerado(PLATOS[Plato.valueOf(contenido[0]).ordinal()].getPrecio());
//                    myGui.presentarSalida("La caja va por: " + resultado.getCajaTotal() + "\n");
//
//                    send(respuestaCocina);
//                }// si no hay comandas disponibles, la cocina no puede atender mas platos de ese tipo
//                else {
//                    ACLMessage respuestaCocina = new ACLMessage(ACLMessage.INFORM);
//                    respuestaCocina.addReceiver(mensaje.getSender());
//                    respuestaCocina.setContent("SORRY," + contenido[0]);
//                    send(respuestaCocina);
//                }
//            }
//
//        }
//    }
}
