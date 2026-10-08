/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package models;

import controller.ClientController;
import domain.Administrator;
import domain.Grupa;
import domain.Kategorija;
import domain.Trener;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.table.AbstractTableModel;

/**
 *
 * @author Korisnik
 */
public class TableModelGrupe extends AbstractTableModel implements Runnable {

    private ArrayList<Grupa> lista;
    private String[] kolone = {"ID", "Naziv", "Kategorija", "Trener"};
    private String parametar = "";
    private Grupa grupa = new Grupa(null, "", "", 0, 0,
            new Kategorija(null, "", ""),
            new Trener(null, "", "", "", 0),
            new Administrator(null, "", "", "", ""),
            null);

    public TableModelGrupe() {
        try {
            lista = ClientController.getInstance()
                    .getAllGrupa(new Grupa(null, "", "", 0, 0,
                    new Kategorija(null, "", ""),
                    new Trener(null, "", "", "", 0),
                    new Administrator(null, "", "", "", ""),
                    null));
        } catch (Exception ex) {
            Logger.getLogger(TableModelGrupe.class.getName()).log(Level.SEVERE, null, ex);
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
        Grupa g = lista.get(row);

        switch (column) {
            case 0:
                return g.getGrupaID();
            case 1:
                return g.getNaziv();
            case 2:
                return g.getKategorija();
            case 3:
                return g.getTrener();

            default:
                return null;
        }
    }

    public Grupa getSelectedGrupa(int row) {
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

            grupa.setNaziv(parametar.toLowerCase());
            grupa.getTrener().setIme(parametar.toLowerCase());
            grupa.getTrener().setPrezime(parametar.toLowerCase());

            lista = ClientController.getInstance().getAllGrupa(grupa);
            fireTableDataChanged();

        } catch (Exception ex) {
            Logger.getLogger(TableModelGrupe.class.getName()).log(Level.SEVERE, "Osvezavanje tabele nije uspelo.", ex);
        }
    }

    public ArrayList<Grupa> getLista() {
        return lista;
    }

}
