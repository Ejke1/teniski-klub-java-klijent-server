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
public class Grupa extends AbstractDomainObject {

    private Long grupaID;
    private String naziv;
    private String opis;
    private int brojClanova;
    private int maxKapacitet;
    private Kategorija kategorija;
    private Trener trener;
    private Administrator administrator;
    private ArrayList<ClanGrupe> clanoviGrupe;

    public Grupa(Long grupaID, String naziv, String opis, int brojClanova, int maxKapacitet, Kategorija kategorija, Trener trener, Administrator administrator, ArrayList<ClanGrupe> clanoviGrupe) {
        this.grupaID = grupaID;
        this.naziv = naziv;
        this.opis = opis;
        this.brojClanova = brojClanova;
        this.maxKapacitet = maxKapacitet;
        this.kategorija = kategorija;
        this.trener = trener;
        this.administrator = administrator;
        this.clanoviGrupe = clanoviGrupe;
    }

    public Grupa() {
    }

    @Override
    public String nazivTabele() {
        return " Grupa ";
    }

    @Override
    public String alijas() {
        return " g ";
    }

    @Override
    public String join() {
        return " JOIN TRENER T ON (T.TRENERID = G.TRENERID)\n"
                + "JOIN KATEGORIJA K ON (K.KATEGORIJAID = G.KATEGORIJAID)\n"
                + "JOIN ADMINISTRATOR A ON (A.ADMINISTRATORID = G.ADMINISTRATORID) ";
    }

    @Override
    public ArrayList<AbstractDomainObject> vratiListu(ResultSet rs) throws SQLException {
        ArrayList<AbstractDomainObject> lista = new ArrayList<>();

        while (rs.next()) {
            Administrator a = new Administrator(rs.getLong("AdministratorID"),
                    rs.getString("a.Ime"), rs.getString("a.Prezime"),
                    rs.getString("Username"), rs.getString("Password"));

            Trener t = new Trener(rs.getLong("TrenerID"),
                    rs.getString("t.Ime"), rs.getString("t.Prezime"),
                    rs.getString("brojTelefona"), rs.getInt("godineIskustva"));

            Kategorija k = new Kategorija(rs.getLong("KategorijaID"),
                    rs.getString("k.naziv"), rs.getString("k.opis"));

            Grupa g = new Grupa(rs.getLong("grupaID"), rs.getString("g.naziv"),
                    rs.getString("g.opis"), rs.getInt("brojClanova"),
                    rs.getInt("maxKapacitet"), k, t, a, null);

            lista.add(g);
        }

        rs.close();
        return lista;
    }

    @Override
    public String koloneZaInsert() {
        return " (naziv, opis, brojClanova, maxKapacitet, "
                + "KategorijaID, TrenerID, AdministratorID) ";
    }

    @Override
    public String vrednostiZaInsert() {
        return "'" + naziv + "', '" + opis + "', "
                + brojClanova + ", " + maxKapacitet + ", "
                + kategorija.getKategorijaID() + ", "
                + trener.getTrenerID() + ", "
                + administrator.getAdministratorID();
    }

    @Override
    public String vrednostiZaUpdate() {
        return " trenerID = " + trener.getTrenerID() + ", "
                + "brojClanova = " + brojClanova + ", maxKapacitet = " + maxKapacitet + ", "
                + "opis = '" + opis + "' ";
    }

    @Override
    public String uslov() {
        return " grupaID = " + grupaID;
    }

    @Override
    public String dodatniUslov() {
        return " WHERE LOWER(T.IME) LIKE '%" + trener.getIme() + "%' "
                + "OR LOWER(T.PREZIME) LIKE '%" + trener.getPrezime() + "%' "
                + "OR LOWER(G.NAZIV) LIKE '%" + naziv + "%'";
    }

    @Override
    public String orderBy() {
        return " ORDER BY GRUPAID ASC ";
    }

    public Long getGrupaID() {
        return grupaID;
    }

    public void setGrupaID(Long grupaID) {
        this.grupaID = grupaID;
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

    public Kategorija getKategorija() {
        return kategorija;
    }

    public void setKategorija(Kategorija kategorija) {
        this.kategorija = kategorija;
    }

    public Trener getTrener() {
        return trener;
    }

    public void setTrener(Trener trener) {
        this.trener = trener;
    }

    public Administrator getAdministrator() {
        return administrator;
    }

    public void setAdministrator(Administrator administrator) {
        this.administrator = administrator;
    }

    public ArrayList<ClanGrupe> getClanoviGrupe() {
        return clanoviGrupe;
    }

    public void setClanoviGrupe(ArrayList<ClanGrupe> clanoviGrupe) {
        this.clanoviGrupe = clanoviGrupe;
    }

    public int getBrojClanova() {
        return brojClanova;
    }

    public void setBrojClanova(int brojClanova) {
        this.brojClanova = brojClanova;
    }

    public int getMaxKapacitet() {
        return maxKapacitet;
    }

    public void setMaxKapacitet(int maxKapacitet) {
        this.maxKapacitet = maxKapacitet;
    }

}
