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
public class ClanGrupe extends AbstractDomainObject {

    private Grupa grupa;
    private int rb;
    private String napomena;
    private Clan clan;

    public ClanGrupe(Grupa grupa, int rb, String napomena, Clan clan) {
        this.grupa = grupa;
        this.rb = rb;
        this.napomena = napomena;
        this.clan = clan;
    }

    public ClanGrupe() {
    }

    @Override
    public String nazivTabele() {
        return " ClanGrupe ";
    }

    @Override
    public String alijas() {
        return " cg ";
    }

    @Override
    public String join() {
        return " JOIN CLAN C ON (C.CLANID = CG.CLANID)\n"
                + "JOIN GRUPA G ON (G.GRUPAID = CG.GRUPAID)\n"
                + "JOIN TRENER T ON (T.TRENERID = G.TRENERID)\n"
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

            Clan c = new Clan(rs.getLong("clanID"), rs.getString("c.ime"),
                    rs.getString("c.prezime"), rs.getInt("godine"),
                    rs.getString("email"), rs.getString("telefon"), k);

            ClanGrupe cg = new ClanGrupe(g, rs.getInt("rb"), rs.getString("napomena"), c);

            lista.add(cg);
        }

        rs.close();
        return lista;
    }

    @Override
    public String koloneZaInsert() {
        return " (grupaID, rb, napomena, clanID) ";
    }

    @Override
    public String vrednostiZaInsert() {
        return " " + grupa.getGrupaID() + ", " + rb + ", "
                + "'" + napomena + "', " + clan.getClanID() + " ";
    }

    @Override
    public String vrednostiZaUpdate() {
        return " napomena = '" + napomena + "', "
                + "clanID = " + clan.getClanID();
    }

    @Override
    public String uslov() {
        return " GRUPAID = " + grupa.getGrupaID() + " AND RB = " + rb;
    }

    @Override
    public String dodatniUslov() {
        return " WHERE G.GRUPAID = " + grupa.getGrupaID();
    }

    @Override
    public String orderBy() {
        return " ORDER BY RB ASC ";
    }

    public Grupa getGrupa() {
        return grupa;
    }

    public void setGrupa(Grupa grupa) {
        this.grupa = grupa;
    }

    public int getRb() {
        return rb;
    }

    public void setRb(int rb) {
        this.rb = rb;
    }

    public String getNapomena() {
        return napomena;
    }

    public void setNapomena(String napomena) {
        this.napomena = napomena;
    }

    public Clan getClan() {
        return clan;
    }

    public void setClan(Clan clan) {
        this.clan = clan;
    }

}
