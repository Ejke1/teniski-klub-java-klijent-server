/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package models;

import controller.ClientController;
import domain.Clan;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.table.AbstractTableModel;

/**
 *
 * @author Korisnik
 */
public class TableModelClanovi extends AbstractTableModel implements Runnable {

    private ArrayList<Clan> lista;
    private String[] kolone = {"ID", "Kategorija", "Ime", "Prezime", "Email", "Telefon", "Godine"};
    private String parametar = "";
    private Clan clan = new Clan(null, "", "", 0, "", "", null);

    public TableModelClanovi() {
        try {
            lista = ClientController.getInstance()
                    .getAllClan(new Clan(null, "", "", 0, "", "", null));
        } catch (Exception ex) {
            Logger.getLogger(TableModelClanovi.class.getName()).log(Level.SEVERE, null, ex);
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
        Clan c = lista.get(row);

        switch (column) {
            case 0:
                return c.getClanID();
            case 1:
                return c.getKategorija();
            case 2:
                return c.getIme();
            case 3:
                return c.getPrezime();
            case 4:
                return c.getEmail();
            case 5:
                return c.getTelefon();
            case 6:
                return c.getGodine();

            default:
                return null;
        }
    }

    public Clan getSelectedClan(int row) {
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

            clan.setIme(parametar.toLowerCase());
            clan.setPrezime(parametar.toLowerCase());
            clan.setEmail(parametar.toLowerCase());

            lista = ClientController.getInstance().getAllClan(clan);
            fireTableDataChanged();

        } catch (Exception ex) {
            Logger.getLogger(TableModelClanovi.class.getName()).log(Level.SEVERE, "Osvezavanje tabele nije uspelo.", ex);
        }
    }

    public ArrayList<Clan> getLista() {
        return lista;
    }

}
