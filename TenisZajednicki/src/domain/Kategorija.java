/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package domain;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 *
 * @author Korisnik
 */
public class Kategorija extends AbstractDomainObject {
    
    private Long kategorijaID;
    private String naziv;
    private String opis;

    @Override
    public String toString() {
        return naziv;
    }

    public Kategorija(Long kategorijaID, String naziv, String opis) {
        this.kategorijaID = kategorijaID;
        this.naziv = naziv;
        this.opis = opis;
    }

    public Kategorija() {
    }
    
    @Override
    public String nazivTabele() {
        return " Kategorija ";
    }

    @Override
    public String alijas() {
        return " k ";
    }

    @Override
    public String join() {
        return "";
    }

    @Override
    public ArrayList<AbstractDomainObject> vratiListu(ResultSet rs) throws SQLException {
        ArrayList<AbstractDomainObject> lista = new ArrayList<>();

        while (rs.next()) {
            Kategorija k = new Kategorija(rs.getLong("KategorijaID"),
                    rs.getString("naziv"), rs.getString("opis"));

            lista.add(k);
        }

        rs.close();
        return lista;
    }

    @Override
    public String koloneZaInsert() {
        return " (naziv, opis) ";
    }

    @Override
    public String vrednostiZaInsert() {
        return "'" + naziv + "', '" + opis + "' ";
    }
    
    @Override
    public String vrednostiZaUpdate() {
        return " naziv = '" + naziv + "', opis = '" + opis + "' ";
    }
    
    @Override
    public String uslov() {
        return " KategorijaID = " + kategorijaID;
    }

    @Override
    public String dodatniUslov() {
        return " WHERE LOWER(NAZIV) LIKE '%" + naziv + "%' ";
    }

    @Override
    public String orderBy() {
        return " ORDER BY KATEGORIJAID ASC ";
    }

    public Long getKategorijaID() {
        return kategorijaID;
    }

    public void setKategorijaID(Long kategorijaID) {
        this.kategorijaID = kategorijaID;
    }

    public String getNaziv() {
        return naziv;
    }

    public void setNaziv(String naziv) {
        this.naziv = naziv;
    }

    public String getOpis() {
        return opis;
    }

    public void setOpis(String opis) {
        this.opis = opis;
    }
    
}
