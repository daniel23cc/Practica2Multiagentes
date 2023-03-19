/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.ujaen.ssmmaa.agentes;

import auxiliares.Resultado;
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
import java.io.FileWriter;
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
    private int tiempoCreacionAgentes;

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

            DFAgentDescription template = new DFAgentDescription();
            template.setName(getAID());
            ServiceDescription templateSd = new ServiceDescription();
            templateSd.setType(TIPO_SERVICIO);
            templateSd.setName(MONITOR.name());
            template.addServices(templateSd);
            try {
                DFService.register(this, template);
            } catch (FIPAException fe) {
                fe.printStackTrace();
            }
            addBehaviour(new TareaCrearAgentes());
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
                Resultado res = new Resultado(); //guardo los resultados

                arrAux = new Object[arrayArgumentos.get(2).size() / 2];
                for (int i = 0; i < arrayArgumentos.get(2).size() / 2; i++) {
                    arrAux[i] = arrayArgumentos.get(2).get(i);
                }
                myGui2.presentarSalida("\nCreando agente Restaurante...");
                System.out.println("ARGS: " + arrayNombreAgentes.get(2) + "; " + arrayClaseAgentes.get(2) + " " + arrAux);
                MicroRuntime.startAgent(arrayNombreAgentes.get(2), arrayClaseAgentes.get(2), arrAux);

                arrAux = new Object[arrayArgumentos.get(0).size() / 2];
                for (int i = 0; i < arrayArgumentos.get(0).size() / 2; i++) {
                    arrAux[i] = arrayArgumentos.get(0).get(i);
                }
                myGui2.presentarSalida("\nCreando agente Cliente...");
                System.out.println("ARGS: " + arrayNombreAgentes.get(0) + "; " + arrayClaseAgentes.get(0) + " " + arrAux);
                MicroRuntime.startAgent(arrayNombreAgentes.get(0), arrayClaseAgentes.get(0), arrAux);

                arrAux = new Object[(arrayArgumentos.get(1).size() / 2) + 1];
                for (int i = 0; i < arrayArgumentos.get(1).size() / 2; i++) {
                    arrAux[i] = arrayArgumentos.get(1).get(i);
                }
                arrAux[arrayArgumentos.get(1).size() / 2] = res;

                myGui2.presentarSalida("\nCreando agente Cocina...");
                System.out.println("ARGS: " + arrayNombreAgentes.get(1) + "; " + arrayClaseAgentes.get(1) + " " + arrAux);
                MicroRuntime.startAgent(arrayNombreAgentes.get(1), arrayClaseAgentes.get(1), arrAux);

                //arrAux = new Object[arrayArgumentos.get(2).size()/2];
                int c = 0;
//                for (int i = arrayArgumentos.get(2).size()/2; i < arrayArgumentos.get(2).size(); i++) {
//                    arrAux[c++] = arrayArgumentos.get(2).get(i);
//                }
//                myGui2.presentarSalida("\nCreando agente Restaurante...");
//                System.out.println("ARGS: " + arrayNombreAgentes.get(5) + "; " + arrayClaseAgentes.get(2) + " " + arrAux);
//                MicroRuntime.startAgent(arrayNombreAgentes.get(5), arrayClaseAgentes.get(2), arrAux);
//
                arrAux = new Object[arrayArgumentos.get(0).size() / 2];
                c = 0;
                for (int i = arrayArgumentos.get(0).size() / 2; i < arrayArgumentos.get(0).size(); i++) {
                    arrAux[c++] = arrayArgumentos.get(0).get(i);
                }
                myGui2.presentarSalida("\nCreando agente Cliente...");
                System.out.println("ARGS: " + arrayNombreAgentes.get(3) + "; " + arrayClaseAgentes.get(0) + " " + arrAux);
                MicroRuntime.startAgent(arrayNombreAgentes.get(3), arrayClaseAgentes.get(0), arrAux);
//
//                arrAux = new Object[arrayArgumentos.get(1).size()/2];
//                c=0;
//                for (int i = arrayArgumentos.get(1).size()/2; i < arrayArgumentos.get(1).size(); i++) {
//                    arrAux[c++] = arrayArgumentos.get(1).get(i);
//                }
//                myGui2.presentarSalida("\nCreando agente Cocina...");
//                System.out.println("ARGS: " + arrayNombreAgentes.get(4) + "; " + arrayClaseAgentes.get(1) + " " + arrAux);
//                //MicroRuntime.startAgent(arrayNombreAgentes.get(4), arrayClaseAgentes.get(1), arrAux);

                //guardarArchivo(res);
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

    private int numeroAgentes(String nombreFich) {
        int lineas = 0;
        try {
            BufferedReader br = new BufferedReader(new FileReader(nombreFich));
            while (br.readLine() != null) {
                lineas++;
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println(lineas / 2);
        return lineas / 2;
    }

    private void leerArchivo() throws Exception {
        Object[] args = getArguments();
        if (args != null && args.length > 0) {
            // Lee el fichero de configuración
            nombreFichero = (String) args[0];

            myGui2.presentarSalida("****LEYENDO ARCHIVO: " + nombreFichero + " **** \n");
            int numAgentes = numeroAgentes(nombreFichero);
            try (BufferedReader reader = new BufferedReader(new FileReader(nombreFichero))) {
                String linea = null;

                arrayNombreAgentes = new ArrayList<>();
                arrayClaseAgentes = new ArrayList<>();
                arrayArgumentos = new ArrayList<ArrayList<String>>();
                for (int i = 0; i < numAgentes; i++) {
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
                    if (!arrayClaseAgentes.contains(claseAgente)) {
                        arrayClaseAgentes.add(claseAgente);
                    }
                }

                myGui2.presentarSalida("Agentes que se van a crear: \n");
                for (int i = 0; i < arrayNombreAgentes.size(); i++) {
                    myGui2.presentarSalida(arrayNombreAgentes.get(i) + "\n ");
                }

                myGui2.presentarSalida("Clases Agentes que se van a crear: \n");
                for (int i = 0; i < arrayClaseAgentes.size(); i++) {
                    myGui2.presentarSalida(arrayClaseAgentes.get(i) + "\n ");
                }

                myGui2.presentarSalida("Clases Agentes que se van a crear: \n");
                for (int i = 0; i < arrayClaseAgentes.size(); i++) {
                    myGui2.presentarSalida(arrayClaseAgentes.get(i) + "\n ");
                }

                myGui2.presentarSalida("\nArgumentos: \n");
                for (int i = 0; i < arrayArgumentos.size(); i++) {
                    for (int j = 0; j < arrayArgumentos.get(i).size(); j++) {
                        myGui2.presentarSalida(arrayArgumentos.get(i).get(j) + ",");
                    }
                    myGui2.presentarSalida("\n");
                }
            } catch (IOException ex) {
                System.err.println("Error al leer el fichero de configuración: " + ex.getMessage());
                throw new Exception();
            }
        }
    }

    private void guardarArchivo(Resultado res) {
        try {
            FileWriter writer = new FileWriter("resultado.txt", true);
            writer.write(res.getCajaTotal() + "\n");
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int getTiempoCreacionAgentes() {
        return tiempoCreacionAgentes;
    }

}
