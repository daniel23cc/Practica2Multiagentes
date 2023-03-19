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

    public static Random aleatorio = new Random();

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
        Helado_frambuesa(POSTRE, 1.99);

        @Override
        public String toString() {
            return name() + ","+ ordenComanda +","+ precio;
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

        public String getNombre() {
            return name();
        }

        public static Plato pedirPlato() {//Obtengo un plato aleatorio
            int tiradaDado = aleatorio.nextInt(CATEGORIAS.length);
            return PLATOS[tiradaDado];
        }
    }

    public static final Plato[] PLATOS = Plato.values();
}
