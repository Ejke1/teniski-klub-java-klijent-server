/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package models;

import controller.ClientController;
import domain.Trener;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.table.AbstractTableModel;

/**
 *
 * @author Korisnik
 */
public class TableModelTreneri extends AbstractTableModel implements Runnable {

    private ArrayList<Trener> lista;
    private String[] kolone = {"ID", "Ime", "Prezime", "Telefon", "Godine iskustva"};
    private String parametar = "";
    private Trener trener = new Trener(null, "", "", "", 0);

    public TableModelTreneri() {
        try {
            lista = ClientController.getInstance().getAllTrener(new Trener(null, "", "", "", 0));
        } catch (Exception ex) {
            Logger.getLogger(TableModelTreneri.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public int getRowCount() {
        return lista.size();
    }

    @Override
    public int getColumnCount() {
        return kolone.length;
    }

    @Override
    public String getColumnName(int i) {
        return kolone[i];
    }

    @Override
    public Object getValueAt(int row, int column) {
        Trener t = lista.get(row);

        switch (column) {
            case 0:
                return t.getTrenerID();
            case 1:
                return t.getIme();
            case 2:
                return t.getPrezime();
            case 3:
                return t.getBrojTelefona();
            case 4:
                return t.getGodineIskustva();

            default:
                return null;
        }
    }

    public Trener getSelectedTrener(int row) {
        return lista.get(row);
    }

    @Override
    public void run() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                Thread.sleep(10000);
                refreshTable();
            }
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }

    public void setParametar(String parametar) {
        this.parametar = parametar;
        refreshTable();
    }

    public void refreshTable() {
        try {

            trener.setIme(parametar.toLowerCase());
            trener.setPrezime(parametar.toLowerCase());

            lista = ClientController.getInstance().getAllTrener(trener);
            fireTableDataChanged();

        } catch (Exception ex) {
            Logger.getLogger(TableModelTreneri.class.getName()).log(Level.SEVERE, "Osvezavanje tabele nije uspelo.", ex);
        }
    }

    public ArrayList<Trener> getLista() {
        return lista;
    }

}
