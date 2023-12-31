/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.ujaen.ssmmaa.gui;

import es.ujaen.ssmmaa.agentes.AgenteCocina;

/**
 *
 * @author danie
 */
public class AgenteCocinaJFrame extends javax.swing.JFrame {

    /**
     * Creates new form AgenteCocinaJFrame
     */
    private AgenteCocina myAgent;
    private FinalizacionDialog finalizacion;

    public AgenteCocinaJFrame(AgenteCocina myAgent) {
        initComponents();
        this.myAgent = myAgent;

        this.setTitle(this.myAgent.getName());
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        salidaTexto = new javax.swing.JTextArea();
        finalizar = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        salidaTexto.setColumns(20);
        salidaTexto.setRows(5);
        jScrollPane1.setViewportView(salidaTexto);

        finalizar.setText("Finalizar");
        finalizar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                finalizarActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 345, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(43, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(finalizar)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 242, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(finalizar)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void finalizarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_finalizarActionPerformed
        // TODO add your handling code here:
        myAgent.doDelete();
    }//GEN-LAST:event_finalizarActionPerformed
    public void presentarSalida(String msg) {
        salidaTexto.append(msg + "\n");
        System.out.println(msg);
    }


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton finalizar;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextArea salidaTexto;
    // End of variables declaration//GEN-END:variables

}
