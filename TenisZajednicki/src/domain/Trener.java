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
public class Trener extends AbstractDomainObject {
    
    private Long trenerID;
    private String ime;
    private String prezime;
    private String brojTelefona;
    private int godineIskustva;

    @Override
    public String toString() {
        return ime + " " + prezime;
    }

    public Trener(Long trenerID, String ime, String prezime, String brojTelefona, int godineIskustva) {
        this.trenerID = trenerID;
        this.ime = ime;
        this.prezime = prezime;
        this.brojTelefona = brojTelefona;
        this.godineIskustva = godineIskustva;
    }

    public Trener() {
    }
    
    @Override
    public String nazivTabele() {
        return " Trener ";
    }

    @Override
    public String alijas() {
        return " t ";
    }

    @Override
    public String join() {
        return "";
    }

    @Override
    public ArrayList<AbstractDomainObject> vratiListu(ResultSet rs) throws SQLException {
        ArrayList<AbstractDomainObject> lista = new ArrayList<>();

        while (rs.next()) {
            Trener t = new Trener(rs.getLong("TrenerID"),
                    rs.getString("Ime"), rs.getString("Prezime"),
                    rs.getString("brojTelefona"), rs.getInt("godineIskustva"));

            lista.add(t);
        }

        rs.close();
        return lista;
    }

    @Override
    public String koloneZaInsert() {
        return " (Ime, Prezime, brojTelefona, godineIskustva) ";
    }

    @Override
    public String vrednostiZaInsert() {
        return "'" + ime + "', '" + prezime + "', "
                + "'" + brojTelefona + "', " + godineIskustva + " ";
    }
    
    @Override
    public String vrednostiZaUpdate() {
        return " brojTelefona = '" + brojTelefona + "', "
                + "godineIskustva = " + godineIskustva + " ";
    }
    
    @Override
    public String uslov() {
        return " trenerID = " + trenerID;
    }

    @Override
    public String dodatniUslov() {
        return " WHERE LOWER(IME) LIKE '%" + ime + "%' "
                + "OR LOWER(PREZIME) LIKE '%" + prezime + "%'";
    }

    @Override
    public String orderBy() {
        return " ORDER BY TRENERID ASC ";
    }

    public Long getTrenerID() {
        return trenerID;
    }

    public void setTrenerID(Long trenerID) {
        this.trenerID = trenerID;
    }

    public String getIme() {
        return ime;
    }

    public void setIme(String ime) {
        this.ime = ime;
    }

    public String getPrezime() {
        return prezime;
    }

    public void setPrezime(String prezime) {
        this.prezime = prezime;
    }

    public String getBrojTelefona() {
        return brojTelefona;
    }

    public void setBrojTelefona(String brojTelefona) {
        this.brojTelefona = brojTelefona;
    }

    public int getGodineIskustva() {
        return godineIskustva;
    }

    public void setGodineIskustva(int godineIskustva) {
        this.godineIskustva = godineIskustva;
    }
    
}
