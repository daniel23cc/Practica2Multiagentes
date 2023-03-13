/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.ujaen.ssmmaa.agentes;

import static es.ujaen.ssmmaa.agentes.Constantes.OrdenComanda.ENTRANTE;
import static es.ujaen.ssmmaa.agentes.Constantes.OrdenComanda.POSTRE;
import static es.ujaen.ssmmaa.agentes.Constantes.OrdenComanda.PRINCIPAL;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Random;

/**
 *
 * @author danie
 */
public class Constantes {

    public enum OrdenComanda {
        ENTRANTE, PRINCIPAL, POSTRE;
    }

    public Random aleatorio = new Random();

    public static final String TIPO_SERVICIO = "SERVICIO"; //padre

    public enum NombreServicio { //hijo
        CLIENTE, RESTAURANTE, COCINA, MONITOR
    }

    public static final NombreServicio[] CATEGORIAS = NombreServicio.values();

    public enum Plato {
        Aceitunas(ENTRANTE, 2.50),
        Nachos_especiales(ENTRANTE, 3.99),
        Macarrones_con_tomatico(PRINCIPAL, 5.99),
        Solomillo_a_la_pimienta(PRINCIPAL, 8.65),
        Tarta_de_queso(POSTRE, 5.19),
        Helado_frambuesa(POSTRE,1.99);

        @Override
        public String toString() {
            return "Plato{" + "ordenComanda=" + ordenComanda + ", precio=" + precio + '}';
        }

        private OrdenComanda ordenComanda;
        private double precio;

        private Plato(OrdenComanda ordenComanda, double precio) {
            this.ordenComanda = ordenComanda;
            this.precio = precio;
        }

        public OrdenComanda getOrdenComanda() {
            return ordenComanda;
        }

        public double getPrecio() {
            return precio;
        }

    }

    //Serualizable para enviar la clase mediante cadena de bytes de forma eficiente
    public class Comanda implements Serializable {

        private static final long serialVersionUID = 1L;
        private int numMesa;
        private ArrayList<Plato> platos;

        public Comanda(int numMesa) {
            this.numMesa = numMesa;
            this.platos = new ArrayList<>();
        }

        public int getNumMesa() {
            return numMesa;
        }

        public void addPlato(Plato plato) {
            platos.add(plato);
        }

        public ArrayList<Plato> getPlatos() {
            return platos;
        }

        public double calcularTotal() {
            double total = 0;
            for (Plato plato : platos) {
                total += plato.getPrecio();
            }
            return total;
        }
    }
}
