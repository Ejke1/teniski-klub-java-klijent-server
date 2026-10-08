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
public class Clan extends AbstractDomainObject {

    private Long clanID;
    private String ime;
    private String prezime;
    private int godine;
    private String email;
    private String telefon;
    private Kategorija kategorija;

    @Override
    public String toString() {
        return ime + " " + prezime + " (Godine: " + godine + ")";
    }

    public Clan() {
    }

    public Clan(Long clanID, String ime, String prezime, int godine, String email, String telefon, Kategorija kategorija) {
        this.clanID = clanID;
        this.ime = ime;
        this.prezime = prezime;
        this.godine = godine;
        this.email = email;
        this.telefon = telefon;
        this.kategorija = kategorija;
    }

    @Override
    public String nazivTabele() {
        return " Clan ";
    }

    @Override
    public String alijas() {
        return " c ";
    }

    @Override
    public String join() {
        return " JOIN KATEGORIJA K ON (K.KATEGORIJAID = C.KATEGORIJAID) ";
    }

    @Override
    public ArrayList<AbstractDomainObject> vratiListu(ResultSet rs) throws SQLException {
        ArrayList<AbstractDomainObject> lista = new ArrayList<>();

        while (rs.next()) {

            Kategorija k = new Kategorija(rs.getLong("KategorijaID"),
                    rs.getString("naziv"), rs.getString("opis"));

            Clan c = new Clan(rs.getLong("clanID"), rs.getString("ime"),
                    rs.getString("prezime"), rs.getInt("godine"),
                    rs.getString("email"), rs.getString("telefon"), k);

            lista.add(c);
        }

        rs.close();
        return lista;
    }

    @Override
    public String koloneZaInsert() {
        return " (Ime, Prezime, godine, email, telefon, kategorijaID) ";
    }

    @Override
    public String vrednostiZaInsert() {
        return "'" + ime + "', '" + prezime + "', "
                + " " + godine + ", '" + email + "', '" + telefon + "', "
                + kategorija.getKategorijaID();
    }

    @Override
    public String vrednostiZaUpdate() {
        return " godine = " + godine + ", email = '" + email + "', "
                + "telefon = '" + telefon + "', "
                + "kategorijaID = " + kategorija.getKategorijaID() + " ";
    }

    @Override
    public String uslov() {
        return " clanID = " + clanID;
    }

    @Override
    public String dodatniUslov() {
        if (kategorija != null) {
            return " WHERE (LOWER(IME) LIKE '%" + ime + "%' "
                    + "OR LOWER(PREZIME) LIKE '%" + prezime + "%' "
                    + "OR LOWER(EMAIL) LIKE '%" + email + "%') "
                    + "AND K.KATEGORIJAID = " + kategorija.getKategorijaID();
        }
        return " WHERE LOWER(IME) LIKE '%" + ime + "%' "
                + "OR LOWER(PREZIME) LIKE '%" + prezime + "%' "
                + "OR LOWER(EMAIL) LIKE '%" + email + "%' ";
    }

    @Override
    public String orderBy() {
        return " ORDER BY CLANID ASC ";
    }

    public Long getClanID() {
        return clanID;
    }

    public void setClanID(Long clanID) {
        this.clanID = clanID;
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

    public int getGodine() {
        return godine;
    }

    public void setGodine(int godine) {
        this.godine = godine;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelefon() {
        return telefon;
    }

    public void setTelefon(String telefon) {
        this.telefon = telefon;
    }

    public Kategorija getKategorija() {
        return kategorija;
    }

    public void setKategorija(Kategorija kategorija) {
        this.kategorija = kategorija;
    }

}
