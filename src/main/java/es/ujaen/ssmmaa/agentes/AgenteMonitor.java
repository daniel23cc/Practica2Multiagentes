/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.ujaen.ssmmaa.agentes;

import static es.ujaen.ssmmaa.agentes.Constantes.NombreServicio.MONITOR;
import static es.ujaen.ssmmaa.agentes.Constantes.TIPO_SERVICIO;
import es.ujaen.ssmmaa.gui.AgenteMonitorJFrame;
import jade.core.Agent;
import jade.core.MicroRuntime;
import jade.core.behaviours.OneShotBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;
import jade.wrapper.StaleProxyException;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author danie
 */
public class AgenteMonitor extends Agent {

    //Variables del agente
    private String nombreFichero;
    private ArrayList<String> arrayNombreAgentes;
    private ArrayList<String> arrayClaseAgentes;
    private ArrayList<ArrayList<String>> arrayArgumentos;
    private AgenteMonitorJFrame myGui2;

    private String nombreAgente;
    private String claseAgente;
    private String numEjecuciones;
    private int tiempoCreacionAgentes;
    //    private String textaco;
    //    private int numEjecuciones;

    private static final String FILE_PATH = "config.txt";
    //int capacidadCocina;
    private int cantidadPlatosCocina;
    private int capacidadRestaurante;
    private int cantidadServiciosRestaurante;
    private String[] serviciosCliente;

    @Override
    protected void setup() {
        try {
            //Inicialización de las variables del agente
            //archivo="configuracion.txt";

            //Configuración del GUI
            myGui2 = new AgenteMonitorJFrame(this);
            myGui2.setVisible(true);
            myGui2.presentarSalida("Se inicializa la ejecución de " + this.getName() + "\n");
            System.out.println("Se inicia la ejecucion del agente: " + this.getName());
            //Registro del agente en las Páginas Amarrillas
            leerArchivo();

            addBehaviour(new TareaCrearAgentes());

            // Se añaden las tareas principales
//            addBehaviour(new OneShotBehaviour() {
//                //private int n = 0;
//
//                @Override
//                public void action() {
//                    // Crear el contenedor para los agentes
//                    AgentContainer container = getContainerController();
//
//                    // Crear el agente Cocina
//    
//                    Object[] argumentosCocina = new Object[1];
//                    argumentosCocina[0] = arrayArgumentos.get(0);
//                    myGui2.presentarSalida("\nCreando agente Cocina...");
//
//                    try {
//                        System.out.println(arrayNombreAgentes.get(0) + "; " + arrayClaseAgentes.get(0) + " " + argumentosCocina[0]);
//                        MicroRuntime.startAgent(arrayNombreAgentes.get(0), arrayClaseAgentes.get(0), argumentosCocina);
//                    } catch (Exception ex) {
//                        Logger.getLogger(AgenteMonitor.class.getName()).log(Level.SEVERE, null, ex);
//                    }
//
//                    // Crear el agente Restaurante
//                    Object[] argumentosRestaurante = new Object[2];
//                    argumentosRestaurante[0] = arrayArgumentos.get(0);
//                    myGui2.presentarSalida("\nCreando agente Restaurante...");
//                    try {
//                        System.out.println(arrayNombreAgentes.get(1) + "; " + arrayClaseAgentes.get(1) + " " + argumentosRestaurante[0]);
//                        MicroRuntime.startAgent(arrayNombreAgentes.get(1), arrayClaseAgentes.get(1), argumentosRestaurante);
//                    } catch (Exception ex) {
//                        Logger.getLogger(AgenteMonitor.class.getName()).log(Level.SEVERE, null, ex);
//                    }
//
//                    // Crear el agente Cliente
//                    Object[] clienteArgs = new Object[1];
//                    clienteArgs[0] = serviciosCliente;
//                    try {
//                        AgentController clienteController = container.createNewAgent("cliente", "AgenteCliente", clienteArgs);
//                        clienteController.start();
//                    } catch (StaleProxyException e) {
//                        e.printStackTrace();
//                    }
//                }
//            });
        } catch (Exception ex) {
            Logger.getLogger(AgenteMonitor.class.getName()).log(Level.SEVERE, null, ex);
        }
        //Añadir las tareas principales
    }

    public class TareaCrearAgentes extends OneShotBehaviour {

        @Override
        public void action() {

            Object[] arrAux;

            try {
                myGui2.presentarSalida("\nCreando agente Restaurante...");

                arrAux = new Object[arrayArgumentos.get(2).size()];
                for (int i = 0; i < arrayArgumentos.get(2).size(); i++) {
                    arrAux[i] = arrayArgumentos.get(2).get(i);
                }

                System.out.println("ARGS: " + arrayNombreAgentes.get(2) + "; " + arrayClaseAgentes.get(2) + " " + arrAux);
                MicroRuntime.startAgent(arrayNombreAgentes.get(2), arrayClaseAgentes.get(2), arrAux);

                arrAux = new Object[arrayArgumentos.get(0).size()];
                for (int i = 0; i < arrayArgumentos.get(0).size(); i++) {
                    arrAux[i] = arrayArgumentos.get(0).get(i);
                }
                myGui2.presentarSalida("\nCreando agente Cliente...");
                //System.out.println(arrayNombreAgentes.get(n) + "; " + arrayClaseAgentes.get(n) + " " + arrAux[0]);
                MicroRuntime.startAgent(arrayNombreAgentes.get(0), arrayClaseAgentes.get(0), arrAux);
                
                arrAux = new Object[arrayArgumentos.get(1).size()];
                for (int i = 0; i < arrayArgumentos.get(1).size(); i++) {
                    arrAux[i] = arrayArgumentos.get(1).get(i);
                }
                myGui2.presentarSalida("\nCreando agente Cocina...");
                //System.out.println(arrayNombreAgentes.get(n) + "; " + arrayClaseAgentes.get(n) + " " + arrAux[0]);
                MicroRuntime.startAgent(arrayNombreAgentes.get(1), arrayClaseAgentes.get(1), arrAux);

            } catch (Exception ex) {
                Logger.getLogger(AgenteMonitor.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

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
        myGui2.dispose();
        System.out.println("Finaliza la ejecución de " + this.getName());
        MicroRuntime.stopJADE();
    }

    private void leerArchivo() throws Exception {
        Object[] args = getArguments();
        if (args != null && args.length > 0) {
            // Lee el fichero de configuración
            nombreFichero = (String) args[0];

            myGui2.presentarSalida("****LEYENDO ARCHIVO: " + nombreFichero + " **** \n");

            try (BufferedReader reader = new BufferedReader(new FileReader(nombreFichero))) {
                String linea = null;

                arrayNombreAgentes = new ArrayList<>();
                arrayClaseAgentes = new ArrayList<>();
                arrayArgumentos = new ArrayList<ArrayList<String>>();
                for (int i = 0; i < 3; i++) {
                    arrayArgumentos.add(new ArrayList<>());
                }

                while ((linea = reader.readLine()) != null) {
                    String[] argumentos = linea.split(":");
                    nombreAgente = argumentos[0];
                    claseAgente = argumentos[1];

                    if ((linea = reader.readLine()) != null) {
                        if (claseAgente.contains("Cliente")) {
                            argumentos = linea.split(":");
                            for (int i = 0; i < argumentos.length; i++) {
                                arrayArgumentos.get(0).add(argumentos[i]);
                            }
                        } else if (claseAgente.contains("Restaurante")) {
                            argumentos = linea.split(":");
                            for (int i = 0; i < argumentos.length; i++) {
                                arrayArgumentos.get(2).add(argumentos[i]);
                            }
                        } else {
                            arrayArgumentos.get(1).add(linea);
                        }
                    }
                    arrayNombreAgentes.add(nombreAgente);
                    //System.out.println("Nombre: " + nombreAgente);
                    arrayClaseAgentes.add(claseAgente);
                }

                myGui2.presentarSalida("Agentes que se van a crear: \n");
                for (int i = 0; i < arrayNombreAgentes.size(); i++) {
                    myGui2.presentarSalida(arrayNombreAgentes.get(i) + "\n ");
                }

                myGui2.presentarSalida("\nArgumentos: \n");
                for (int i = 0; i < arrayArgumentos.size(); i++) {
                    myGui2.presentarSalida(arrayArgumentos.get(i) + ", ");
                    if (i > 0 && i % 3 == 0) {
                        myGui2.presentarSalida("\n");
                    }
                }
            } catch (IOException ex) {
                System.err.println("Error al leer el fichero de configuración: " + ex.getMessage());
                throw new Exception();
            }
        }
    }

    public int getTiempoCreacionAgentes() {
        return tiempoCreacionAgentes;
    }

}
