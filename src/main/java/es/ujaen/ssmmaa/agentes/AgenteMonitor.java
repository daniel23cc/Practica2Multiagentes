/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.ujaen.ssmmaa.agentes;

import es.ujaen.ssmmaa.gui.AgenteMonitorJFrame;
import jade.core.Agent;
import jade.core.MicroRuntime;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
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
    private ArrayList<String> arrayEjecucionesAgentes;
    private AgenteMonitorJFrame myGui2;

    private String nombreAgente;
    private String claseAgente;
    private String numEjecuciones;
    private int tiempoCreacionAgentes;
    //    private String textaco;
    //    private int numEjecuciones;

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
            //Registro de la Ontología
            DFAgentDescription dfd = new DFAgentDescription();
            dfd.setName(getAID());
            ServiceDescription sd = new ServiceDescription();
            sd.setType("Tipo de Servicio");
            sd.setName("Nombre del Servicio");
            dfd.addServices(sd);
            try {
                DFService.register(this, dfd);
            } catch (FIPAException fe) {
                fe.printStackTrace();
            }

            // Se añaden las tareas principales
            addBehaviour(new TareaCrearAgentes(this, tiempoCreacionAgentes * 1000));
        } catch (Exception ex) {
            Logger.getLogger(AgenteMonitor.class.getName()).log(Level.SEVERE, null, ex);
        }
        //Añadir las tareas principales
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

    //Métodos de trabajo del agente
    private void leerArchivo() throws Exception {
        Object[] args = getArguments();
        if (args != null && args.length > 0) {
            // Lee el fichero de configuración
            nombreFichero = (String) args[0];
            //System.out.println("****LEYENDO ARCHIVO: " + nombreFichero + " ****");
            myGui2.presentarSalida("****LEYENDO ARCHIVO: " + nombreFichero + " **** \n");
            try (BufferedReader reader = new BufferedReader(new FileReader(nombreFichero))) {
                String linea = reader.readLine();
                tiempoCreacionAgentes = Integer.parseInt(linea);

                arrayNombreAgentes = new ArrayList<>();
                arrayClaseAgentes = new ArrayList<>();
                arrayEjecucionesAgentes = new ArrayList<>();

                while ((linea = reader.readLine()) != null) {
                    String[] argumentos = linea.split(":");
                    nombreAgente = argumentos[0];
                    claseAgente = argumentos[1].split(" ")[0];
                    numEjecuciones = argumentos[1].split(" ")[1];

                    arrayNombreAgentes.add(nombreAgente);
                    //System.out.println("Nombre: " + nombreAgente);
                    arrayClaseAgentes.add(claseAgente);
                    //System.out.println("Clase: " + claseAgente);
                    arrayEjecucionesAgentes.add(numEjecuciones);
                    //System.out.println("Ejecuciones: " + numEjecuciones);
                }

                myGui2.presentarSalida("Agentes que se van a crear: ");
                for (int i = 0; i < arrayNombreAgentes.size(); i++) {
                    myGui2.presentarSalida(arrayNombreAgentes.get(i)+", ");
                }

                myGui2.presentarSalida("\nTiempos de ejecuciones respectivos: ");
                for (int i = 0; i < arrayEjecucionesAgentes.size(); i++) {
                    myGui2.presentarSalida(arrayEjecucionesAgentes.get(i)+", ");
                }

                myGui2.presentarSalida("\nIntervalo ciclico del monitor: "+String.valueOf(tiempoCreacionAgentes)+" segs");
            } catch (IOException ex) {
                System.err.println("Error al leer el fichero de configuración: " + ex.getMessage());
                throw new Exception();
            }
        }
    }

    //Clases internas que representan las tareas del agente
    public class TareaCrearAgentes extends TickerBehaviour {

        private int n = 0;

        //Tarea de ejemplo que se repite cada 10 segundos
        public TareaCrearAgentes(Agent a, long period) {
            super(a, period);
        }

        @Override
        protected void onTick() {

            String arrayAuxiliar[] = new String[1];
            Object[] arrAux = new Object[1];
            arrAux[0] = arrayEjecucionesAgentes.get(n);
            myGui2.presentarSalida("\nCreando agente...");
            try {
                System.out.println(arrayNombreAgentes.get(n) + "; " + arrayClaseAgentes.get(n) + " " + arrAux[0]);
                MicroRuntime.startAgent(arrayNombreAgentes.get(n), arrayClaseAgentes.get(n), arrAux);
            } catch (Exception ex) {
                Logger.getLogger(AgenteMonitor.class.getName()).log(Level.SEVERE, null, ex);
            }
            n++;

            if (n == arrayNombreAgentes.size()) {
                n = 0;
            }
        }
    }

    public int getTiempoCreacionAgentes() {
        return tiempoCreacionAgentes;
    }

}
