package controller;

import domain.Administrator;
import domain.Clan;
import domain.Grupa;
import domain.Kategorija;
import domain.Trener;
import java.util.ArrayList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThrows;
import org.junit.Before;
import org.junit.Test;
import testutil.LazniServer;
import transfer.util.Operation;

public class ClientControllerTest {

    private LazniServer server;
    private ClientController kontroler;

    @Before
    public void pripremi() {
        server = testutil.TestKlijent.server();
        kontroler = ClientController.getInstance();
    }

    @Test
    public void uvekVracaIstuInstancu() {
        assertSame(kontroler, ClientController.getInstance());
    }

    @Test
    public void prijavaSaljeKorisnickoImeILozinku() throws Exception {
        Administrator a = new Administrator();
        a.setUsername("coa");
        a.setPassword("coa123");

        Administrator ulogovani = kontroler.login(a);

        assertEquals("Aleksandar", ulogovani.getIme());
        Administrator poslat = (Administrator) server.poslednji(Operation.LOGIN);
        assertEquals("coa", poslat.getUsername());
        assertEquals("coa123", poslat.getPassword());
    }

    @Test
    public void greskaSaServeraSeProsledjujeKaoIzuzetak() {
        Administrator a = new Administrator();
        a.setUsername("coa");
        a.setPassword("pogresna");

        Exception ex = assertThrows(Exception.class, () -> kontroler.login(a));

        assertEquals("Ne postoji administrator sa tim kredencijalima.", ex.getMessage());
    }

    @Test
    public void odjavaSaljeUlogovanogAdministratora() throws Exception {
        kontroler.logout(new Administrator(1L, "Aleksandar", "Eic", "coa", "coa123"));

        assertEquals(Long.valueOf(1), ((Administrator) server.poslednji(Operation.LOGOUT)).getAdministratorID());
    }

    @Test
    public void operacijeNadClanovima() throws Exception {
        Clan c = new Clan(null, "Jovana", "Peric", 11, "jovana@test.rs", "0641234567", server.kategorija(1));

        kontroler.addClan(c);
        assertEquals("Jovana", ((Clan) server.poslednji(Operation.ADD_CLAN)).getIme());

        ArrayList<Clan> pronadjeni = kontroler.getAllClan(new Clan(null, "jovana", "jovana", 0, "jovana", "", null));
        assertEquals(1, pronadjeni.size());
        Clan jovana = pronadjeni.get(0);

        jovana.setGodine(12);
        kontroler.updateClan(jovana);
        assertEquals(12, ((Clan) server.poslednji(Operation.UPDATE_CLAN)).getGodine());

        kontroler.deleteClan(jovana);
        assertEquals(jovana.getClanID(), ((Clan) server.poslednji(Operation.DELETE_CLAN)).getClanID());
        assertEquals(5, kontroler.getAllClan(new Clan(null, "", "", 0, "", "", null)).size());
    }

    @Test
    public void operacijeNadTrenerima() throws Exception {
        kontroler.addTrener(new Trener(null, "Milan", "Jovic", "0603030303", 7));
        assertEquals("Milan", ((Trener) server.poslednji(Operation.ADD_TRENER)).getIme());

        ArrayList<Trener> pronadjeni = kontroler.getAllTrener(new Trener(null, "milan", "milan", "", 0));
        assertEquals(1, pronadjeni.size());
        Trener milan = pronadjeni.get(0);

        milan.setGodineIskustva(9);
        kontroler.updateTrener(milan);
        assertEquals(9, ((Trener) server.poslednji(Operation.UPDATE_TRENER)).getGodineIskustva());

        kontroler.deleteTrener(milan);
        assertEquals(2, kontroler.getAllTrener(new Trener(null, "", "", "", 0)).size());
    }

    @Test
    public void operacijeNadGrupamaVracajuIzvestajOSlanjuMaila() throws Exception {
        Grupa nova = server.grupa(1);
        nova.setGrupaID(null);
        nova.setNaziv("Nova grupa");

        String izvestaj = kontroler.addGrupa(nova);
        assertEquals("Mail sa PDF spiskom clanova je poslat na 2 od 2 clanova grupe.", izvestaj);
        assertEquals("Nova grupa", ((Grupa) server.poslednji(Operation.ADD_GRUPA)).getNaziv());

        Grupa postojeca = server.grupa(2);
        postojeca.setOpis("Izmenjen opis");
        assertEquals("Mail sa PDF spiskom clanova je poslat na 2 od 2 clanova grupe.", kontroler.updateGrupa(postojeca));
        assertEquals("Izmenjen opis", ((Grupa) server.poslednji(Operation.UPDATE_GRUPA)).getOpis());

        ArrayList<Grupa> grupe = kontroler.getAllGrupa(new Grupa(null, "", "", 0, 0, new Kategorija(null, "", ""),
                new Trener(null, "", "", "", 0), new Administrator(null, "", "", "", ""), null));
        assertEquals(3, grupe.size());

        kontroler.deleteGrupa(postojeca);
        assertEquals(2, server.brojGrupa());
    }

    @Test
    public void ucitavaKategorije() throws Exception {
        ArrayList<Kategorija> kategorije = kontroler.getAllKategorija(new Kategorija(null, "", ""));

        assertEquals(3, kategorije.size());
        assertEquals("Pocetni", kategorije.get(0).getNaziv());
    }

    @Test
    public void greskeZaSveOperacijeSeProsledjuju() {
        int[] operacije = {Operation.ADD_CLAN, Operation.UPDATE_CLAN, Operation.DELETE_CLAN, Operation.GET_ALL_CLAN,
            Operation.ADD_TRENER, Operation.UPDATE_TRENER, Operation.DELETE_TRENER, Operation.GET_ALL_TRENER,
            Operation.ADD_GRUPA, Operation.UPDATE_GRUPA, Operation.DELETE_GRUPA, Operation.GET_ALL_GRUPA,
            Operation.GET_ALL_KATEGORIJA, Operation.LOGOUT};
        for (int op : operacije) {
            server.greska(op, "Greska " + op);
        }

        Clan c = server.clan(3);
        Trener t = server.trener(1);
        Grupa g = server.grupa(1);
        assertEquals("Greska 2", assertThrows(Exception.class, () -> kontroler.addClan(c)).getMessage());
        assertEquals("Greska 4", assertThrows(Exception.class, () -> kontroler.updateClan(c)).getMessage());
        assertEquals("Greska 3", assertThrows(Exception.class, () -> kontroler.deleteClan(c)).getMessage());
        assertEquals("Greska 5", assertThrows(Exception.class, () -> kontroler.getAllClan(c)).getMessage());
        assertEquals("Greska 10", assertThrows(Exception.class, () -> kontroler.addTrener(t)).getMessage());
        assertEquals("Greska 12", assertThrows(Exception.class, () -> kontroler.updateTrener(t)).getMessage());
        assertEquals("Greska 11", assertThrows(Exception.class, () -> kontroler.deleteTrener(t)).getMessage());
        assertEquals("Greska 13", assertThrows(Exception.class, () -> kontroler.getAllTrener(t)).getMessage());
        assertEquals("Greska 6", assertThrows(Exception.class, () -> kontroler.addGrupa(g)).getMessage());
        assertEquals("Greska 8", assertThrows(Exception.class, () -> kontroler.updateGrupa(g)).getMessage());
        assertEquals("Greska 7", assertThrows(Exception.class, () -> kontroler.deleteGrupa(g)).getMessage());
        assertEquals("Greska 9", assertThrows(Exception.class, () -> kontroler.getAllGrupa(g)).getMessage());
        assertEquals("Greska 14", assertThrows(Exception.class,
                () -> kontroler.getAllKategorija(new Kategorija(null, "", ""))).getMessage());
        assertEquals("Greska 1", assertThrows(Exception.class,
                () -> kontroler.logout(new Administrator())).getMessage());
    }
}
