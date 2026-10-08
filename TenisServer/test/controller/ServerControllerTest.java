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
import static org.junit.Assert.assertTrue;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import testutil.TestBaza;

public class ServerControllerTest {

    private ServerController kontroler;

    @Before
    public void pripremi() throws Exception {
        TestBaza.pripremi();
        kontroler = ServerController.getInstance();
        kontroler.getUlogovaniAdministratori().clear();
    }

    @After
    public void odjaviSve() {
        kontroler.getUlogovaniAdministratori().clear();
    }

    private Administrator prijava(String korisnik, String lozinka) {
        Administrator a = new Administrator();
        a.setUsername(korisnik);
        a.setPassword(lozinka);
        return a;
    }

    @Test
    public void uvekVracaIstuInstancu() {
        assertSame(kontroler, ServerController.getInstance());
    }

    @Test
    public void prijavaIOdjavaAdministratora() throws Exception {
        Administrator a = kontroler.login(prijava("coa", "coa123"));

        assertEquals("Aleksandar", a.getIme());
        assertEquals(1, kontroler.getUlogovaniAdministratori().size());

        kontroler.logout(new Administrator(1L, null, null, null, null));

        assertTrue(kontroler.getUlogovaniAdministratori().isEmpty());
    }

    @Test
    public void odjavaNeprijavljenogNeMenjaListu() throws Exception {
        kontroler.login(prijava("coa", "coa123"));

        kontroler.logout(new Administrator(2L, null, null, null, null));

        assertEquals(1, kontroler.getUlogovaniAdministratori().size());
    }

    @Test
    public void listaUlogovanihMozeDaSeZameni() {
        ArrayList<Administrator> nova = new ArrayList<>();
        nova.add(TestBaza.administrator(2));
        ArrayList<Administrator> stara = kontroler.getUlogovaniAdministratori();

        kontroler.setUlogovaniAdministratori(nova);
        assertSame(nova, kontroler.getUlogovaniAdministratori());

        kontroler.setUlogovaniAdministratori(stara);
    }

    @Test
    public void neuspesnaPrijavaProsledjujeGresku() {
        Exception ex = assertThrows(Exception.class, () -> kontroler.login(prijava("coa", "pogresna")));

        assertEquals("Ne postoji administrator sa tim kredencijalima.", ex.getMessage());
    }

    @Test
    public void radSaClanovima() throws Exception {
        kontroler.addClan(new Clan(null, "Jovana", "Peric", 11, "jovana.peric@test.rs", "0641234567", TestBaza.kategorija(1)));
        Long id = Long.valueOf(TestBaza.vrednost("SELECT ClanID FROM Clan WHERE Email = 'jovana.peric@test.rs'"));

        Clan jovana = new Clan(id, "Jovana", "Peric", 12, "jovana.peric@test.rs", "0641234567", TestBaza.kategorija(2));
        kontroler.updateClan(jovana);
        assertEquals("12", TestBaza.vrednost("SELECT Godine FROM Clan WHERE ClanID = " + id));

        ArrayList<Clan> pronadjeni = kontroler.getAllClan(new Clan(null, "jovana", "jovana", 0, "jovana", "", null));
        assertEquals(1, pronadjeni.size());
        assertEquals("Srednji", pronadjeni.get(0).getKategorija().getNaziv());

        kontroler.deleteClan(jovana);
        assertEquals(0, TestBaza.broj("SELECT COUNT(*) FROM Clan WHERE ClanID = " + id));
    }

    @Test
    public void greskeKodClanovaSeProsledjuju() {
        Clan neispravan = new Clan(null, "Jovana", "Peric", 11, "pogresan-mejl", "0641234567", TestBaza.kategorija(1));

        Exception ex = assertThrows(Exception.class, () -> kontroler.addClan(neispravan));
        assertEquals("Email nije u ispravnom formatu!", ex.getMessage());

        Clan lana = TestBaza.clan(1);
        lana.setGodine(3);
        ex = assertThrows(Exception.class, () -> kontroler.updateClan(lana));
        assertEquals("Godine moraju biti izmedju 5 i 65!", ex.getMessage());

        assertThrows(Exception.class, () -> kontroler.deleteClan(TestBaza.clan(1)));
    }

    @Test
    public void radSaTrenerima() throws Exception {
        kontroler.addTrener(new Trener(null, "Milan", "Jovic", "0603030303", 7));
        Long id = Long.valueOf(TestBaza.vrednost("SELECT TrenerID FROM Trener WHERE BrojTelefona = '0603030303'"));

        Trener milan = new Trener(id, "Milan", "Jovic", "0603030304", 9);
        kontroler.updateTrener(milan);
        assertEquals("9", TestBaza.vrednost("SELECT GodineIskustva FROM Trener WHERE TrenerID = " + id));

        ArrayList<Trener> pronadjeni = kontroler.getAllTrener(new Trener(null, "milan", "milan", "", 0));
        assertEquals(1, pronadjeni.size());
        assertEquals("0603030304", pronadjeni.get(0).getBrojTelefona());

        kontroler.deleteTrener(milan);
        assertEquals(0, TestBaza.broj("SELECT COUNT(*) FROM Trener WHERE TrenerID = " + id));
    }

    @Test
    public void greskeKodTreneraSeProsledjuju() {
        Exception ex = assertThrows(Exception.class,
                () -> kontroler.addTrener(new Trener(null, "Milan", "Jovic", "123", 7)));
        assertEquals("Telefon mora biti u formatu 06XXXXXXXX!", ex.getMessage());

        Trener ivan = TestBaza.trener(1);
        ivan.setGodineIskustva(80);
        ex = assertThrows(Exception.class, () -> kontroler.updateTrener(ivan));
        assertEquals("Godine moraju biti izmedju 5 i 70!", ex.getMessage());

        assertThrows(Exception.class, () -> kontroler.deleteTrener(TestBaza.trener(1)));
    }

    @Test
    public void pretragaIBrisanjeGrupa() throws Exception {
        ArrayList<Grupa> grupe = kontroler.getAllGrupa(new Grupa(null, "mini", "", 0, 0,
                new Kategorija(null, "", ""), new Trener(null, "mini", "mini", "", 0),
                new Administrator(null, "", "", "", ""), null));
        assertEquals(1, grupe.size());
        assertEquals(3, grupe.get(0).getClanoviGrupe().size());

        kontroler.deleteGrupa(grupe.get(0));

        assertEquals(0, TestBaza.broj("SELECT COUNT(*) FROM Grupa WHERE GrupaID = 1"));
    }

    @Test
    public void vracaSveKategorije() throws Exception {
        ArrayList<Kategorija> kategorije = kontroler.getAllKategorija(new Kategorija(null, "", ""));

        assertEquals(4, kategorije.size());
    }
}
