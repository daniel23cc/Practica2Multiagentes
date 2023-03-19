/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package auxiliares;

import es.ujaen.ssmmaa.agentes.Constantes.Plato;
import jade.core.AID;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

/**
 *
 * @author danie
 */
public class Resultado {

    private AID aidCliente;
    private Date fechaLLegada;
    private Date fechaEntrada;
    private Date fechaSalida;
    private ArrayList<Plato> platosCliente;
    private double cajaTotal;
    private String archivo = "resultado.txt";

    public Resultado() {
        this.aidCliente = null;
        this.fechaLLegada = new Date();
        this.fechaEntrada = null;
        this.fechaSalida = null;//si es null nunca habra salido
        this.platosCliente = new ArrayList();
        this.cajaTotal = 0;
    }

    public AID getAidCliente() {
        return aidCliente;
    }

    public void setAidCliente(AID aidCliente) {
        this.aidCliente = aidCliente;
    }

    public Date getFechaLLegada() {
        return fechaLLegada;
    }

    public void setFechaLLegada(Date fechaLLegada) {
        this.fechaLLegada = fechaLLegada;
    }

    public Date getFechaEntrada() {
        return fechaEntrada;
    }

    public void setFechaEntrada(Date fechaEntrada) {
        this.fechaEntrada = fechaEntrada;
    }

    public Date getFechaSalida() {
        return fechaSalida;
    }

    public void setFechaSalida(Date fechaSalida) {
        this.fechaSalida = fechaSalida;
    }

    public ArrayList<Plato> getPlatosCliente() {
        return platosCliente;
    }

    public void setPlatosCliente(ArrayList<Plato> platosCliente) {
        this.platosCliente = platosCliente;
    }

    public double getCajaTotal() {
        return cajaTotal;
    }

    public synchronized void agregarDineroGenerado(double dinero) {
        this.cajaTotal += dinero;
        actualizarArchivo();
    }
    
    private void actualizarArchivo() {
        try {
            FileWriter writer = new FileWriter(archivo);
            writer.write("Dinero total generado: "+Double.toString(cajaTotal)+" €");
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String toString() {
        return "Resultado{" + "aidCliente=" + aidCliente + ", fechaLLegada=" + fechaLLegada + ", fechaEntrada=" + fechaEntrada + ", fechaSalida=" + fechaSalida + ", platosCliente=" + platosCliente + ", cajaTotal=" + cajaTotal + '}';
    }

}
