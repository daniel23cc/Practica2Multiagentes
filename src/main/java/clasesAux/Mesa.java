/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clasesAux;

import java.util.ArrayList;

/**
 *
 * @author danie
 */
public class Mesa {

        private int capacidadMaxima;
        private ArrayList<String> comanda;
        private boolean disponible;

        public Mesa(int capacidadMaxima) {
            this.capacidadMaxima = capacidadMaxima;
            this.comanda = new ArrayList<>();
            this.disponible = true;
        }

        public synchronized boolean agregarComanda(String plato) {
            if (this.comanda.size() < this.capacidadMaxima) {
                this.comanda.add(plato);
                return true;
            } else {
                return false;
            }
        }

        public synchronized void reset() {
            this.comanda.clear();
            this.disponible = true;
        }

        public synchronized ArrayList<String> getComanda() {
            return comanda;
        }

        public synchronized boolean isDisponible() {
            return disponible;
        }

        public synchronized void setDisponible(boolean disponible) {
            this.disponible = disponible;
        }
    }