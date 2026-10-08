/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package models;

import controller.ClientController;
import domain.Clan;
import domain.ClanGrupe;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import javax.swing.table.AbstractTableModel;

/**
 *
 * @author Korisnik
 */
public class TableModelClanoviGrupe extends AbstractTableModel {

    private ArrayList<ClanGrupe> lista;
    private String[] kolone = {"Rb", "Ime i prezime", "Godine"};
    private int rb;

    public TableModelClanoviGrupe() {
        lista = new ArrayList<>();
    }

    public TableModelClanoviGrupe(ArrayList<ClanGrupe> clanoviGrupe) {
        lista = clanoviGrupe != null ? clanoviGrupe : new ArrayList<>();
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
        ClanGrupe cg = lista.get(row);

        switch (column) {
            case 0:
                return cg.getRb();
            case 1:
                return cg.getClan().getIme() + " " + cg.getClan().getPrezime();
            case 2:
                return cg.getClan().getGodine();

            default:
                return null;
        }
    }

    public void dodajClana(ClanGrupe cg) {
        rb = lista.size();
        cg.setRb(++rb);
        lista.add(cg);
        fireTableDataChanged();
    }

    public void izmeniClana(int row, Clan clan, String napomena) {
        ClanGrupe cg = lista.get(row);
        cg.setClan(clan);
        cg.setNapomena(napomena);
        fireTableRowsUpdated(row, row);
    }

    public void obrisiClana(int row) {
        lista.remove(row);

        rb = 0;
        for (ClanGrupe clanGrupe : lista) {
            clanGrupe.setRb(++rb);
        }

        fireTableDataChanged();
    }

    public boolean postojiClan(Clan clan) {
        return postojiClanOsimReda(clan, -1);
    }

    public boolean postojiClanOsimReda(Clan clan, int row) {
        for (int i = 0; i < lista.size(); i++) {
            if (i == row) {
                continue;
            }
            if (lista.get(i).getClan().getClanID().equals(clan.getClanID())) {
                return true;
            }
        }
        return false;
    }

    public ArrayList<String> osveziSaServera(Long kategorijaID) throws Exception {
        ArrayList<String> uklonjeni = new ArrayList<>();
        if (lista.isEmpty()) {
            return uklonjeni;
        }

        ArrayList<Clan> sviClanovi = ClientController.getInstance()
                .getAllClan(new Clan(null, "", "", 0, "", "", null));

        HashMap<Long, Clan> mapa = new HashMap<>();
        for (Clan c : sviClanovi) {
            mapa.put(c.getClanID(), c);
        }

        Iterator<ClanGrupe> it = lista.iterator();
        while (it.hasNext()) {
            ClanGrupe cg = it.next();
            Clan aktuelni = mapa.get(cg.getClan().getClanID());

            boolean drugaKategorija = aktuelni != null && kategorijaID != null
                    && aktuelni.getKategorija() != null
                    && !kategorijaID.equals(aktuelni.getKategorija().getKategorijaID());

            if (aktuelni == null || drugaKategorija) {
                uklonjeni.add(cg.getClan().getIme() + " " + cg.getClan().getPrezime());
                it.remove();
            } else {
                cg.setClan(aktuelni);
            }
        }

        if (uklonjeni.isEmpty()) {
            fireTableRowsUpdated(0, lista.size() - 1);
        } else {
            rb = 0;
            for (ClanGrupe clanGrupe : lista) {
                clanGrupe.setRb(++rb);
            }
            fireTableDataChanged();
        }
        return uklonjeni;
    }

    public ClanGrupe getStavka(int row) {
        return lista.get(row);
    }

    public void ocisti() {
        lista.clear();
        fireTableDataChanged();
    }

    public ArrayList<ClanGrupe> getLista() {
        return lista;
    }

}
