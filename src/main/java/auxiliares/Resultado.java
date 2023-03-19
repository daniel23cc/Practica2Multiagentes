/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package auxiliares;

import static es.ujaen.ssmmaa.agentes.Constantes.ARCHIVO_GUARDADO;
import es.ujaen.ssmmaa.agentes.Constantes.Plato;
import jade.core.AID;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

/**
 *
 * @author danie
 */
public class Resultado {

    private AID aidCliente;
    private HashMap<String, Date> fechasLlegada;
    private HashMap<String, Date> fechasEntrada;
    private HashMap<String, Date> fechasSalida;
    private HashMap<String, ArrayList<Plato>> platosCliente;

    private ArrayList<String> arrayNombres;
    private double cajaTotal;

    public Resultado() {
        this.aidCliente = null;
        this.platosCliente = new HashMap<String, ArrayList<Plato>>();
        this.fechasLlegada = new HashMap();
        this.fechasEntrada = new HashMap<>();
        this.fechasSalida = new HashMap<>();
        this.arrayNombres = new ArrayList();
        this.cajaTotal = 0;
    }

    public AID getAidCliente() {
        return aidCliente;
    }

    public void setAidCliente(AID aidCliente) {
        this.aidCliente = aidCliente;
    }

    public void addfechasEntrada(String clave, Date date) {
        fechasEntrada.put(clave, date);
        actualizarArchivo();
    }

    public void addfechasLlegada(String clave, Date date) {
        fechasLlegada.put(clave, date);
        actualizarArchivo();
    }

    public void addfechasSalida(String clave, Date date) {
        fechasSalida.put(clave, date);
        actualizarArchivo();
    }

    public void addPlato(String clave, ArrayList<Plato> platos) {
        platosCliente.put(clave, platos);
    }

    public void generarArrayNombres(ArrayList<String> array2) {
        for (int i = 0; i < array2.size(); i++) {
            arrayNombres.add(i, array2.get(i));
            fechasLlegada.put(arrayNombres.get(i), new Date());
            fechasEntrada.put(arrayNombres.get(i), new Date());
            fechasSalida.put(arrayNombres.get(i), new Date());
      
        }
        
        
    }

    public double getCajaTotal() {
        return cajaTotal;
    }

    public void setCajaTotal(double cajaTotal) {
        this.cajaTotal = cajaTotal;
    }

    public synchronized void agregarDineroGenerado(double dinero) {
        this.cajaTotal += dinero;
        actualizarArchivo();
    }

    private void actualizarArchivo() {
        try {
            FileWriter writer = new FileWriter(ARCHIVO_GUARDADO);
            DecimalFormat decimalFormat = new DecimalFormat("#.##");
            String formattedNumber = decimalFormat.format(cajaTotal);
            writer.write("Dinero total generado: " + formattedNumber + " €");
//            SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
//            writer.write("Fecha llegada clientes");
//            for(int i=0;i<arrayNombres.size();i++){
//                System.out.println(arrayNombres.get(i));
//                System.out.println(fechasLlegada.get(arrayNombres.get(i)));
//                writer.write(formatter.format(fechasLlegada.get(arrayNombres.get(i))));
//            }
//            System.out.println("PASA2");
//            writer.write("Fecha entrada clientes");
//            for(int i=0;i<arrayNombres.size();i++){
//                writer.write(formatter.format(fechasEntrada.get(arrayNombres.get(i))));
//            }
//            writer.write("Fecha salida clientes");
//            for(int i=0;i<arrayNombres.size();i++){
//                writer.write(formatter.format(fechasSalida.get(arrayNombres.get(i))));
//            }
//            writer.write("Platos clientes");
//            for(int i=0;i<arrayNombres.size();i++){
//                writer.write("Platos cliente: "+arrayNombres.get(i));
//                int numPlatos=platosCliente.get(arrayNombres.get(i)).size();
//                for(int j=0;j<numPlatos;j++){
//                    writer.write(platosCliente.get(arrayNombres.get(i)).get(j).getNombre()+",");
//                }
//            }
            
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
