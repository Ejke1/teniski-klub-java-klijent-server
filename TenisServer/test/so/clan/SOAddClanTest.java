package so.clan;

import domain.Clan;
import domain.Trener;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import org.junit.Before;
import org.junit.Test;
import testutil.TestBaza;

public class SOAddClanTest {

    @Before
    public void pripremiBazu() throws Exception {
        TestBaza.pripremi();
    }

    private Clan noviClan() {
        return new Clan(null, "Jovana", "Peric", 11, "jovana.peric@test.rs", "0641234567", TestBaza.kategorija(1));
    }

    private void ocekujGresku(String poruka, Clan c) throws Exception {
        Exception ex = assertThrows(Exception.class, () -> new SOAddClan().templateExecute(c));
        assertEquals(poruka, ex.getMessage());
        assertEquals(6, TestBaza.broj("SELECT COUNT(*) FROM Clan"));
    }

    @Test
    public void dodajeIspravnogClana() throws Exception {
        new SOAddClan().templateExecute(noviClan());

        assertEquals(7, TestBaza.broj("SELECT COUNT(*) FROM Clan"));
        assertEquals("Peric", TestBaza.vrednost("SELECT Prezime FROM Clan WHERE Email = 'jovana.peric@test.rs'"));
        assertEquals("1", TestBaza.vrednost("SELECT KategorijaID FROM Clan WHERE Email = 'jovana.peric@test.rs'"));
    }

    @Test
    public void prihvataGranicneVrednostiGodina() throws Exception {
        Clan najmladji = noviClan();
        najmladji.setGodine(5);
        new SOAddClan().templateExecute(najmladji);

        Clan najstariji = new Clan(null, "Milan", "Jovic", 65, "milan.jovic@test.rs", "0649876543", TestBaza.kategorija(4));
        new SOAddClan().templateExecute(najstariji);

        assertEquals(8, TestBaza.broj("SELECT COUNT(*) FROM Clan"));
    }

    @Test
    public void odbijaNeispravanEmail() throws Exception {
        Clan c = noviClan();
        c.setEmail("jovana.peric.test.rs");
        ocekujGresku("Email nije u ispravnom formatu!", c);
    }

    @Test
    public void odbijaNeispravanTelefon() throws Exception {
        Clan c = noviClan();
        c.setTelefon("0711234567");
        ocekujGresku("Telefon mora biti u formatu 06XXXXXXXX!", c);

        c.setTelefon("064123");
        ocekujGresku("Telefon mora biti u formatu 06XXXXXXXX!", c);
    }

    @Test
    public void odbijaGodineIspodMinimuma() throws Exception {
        Clan c = noviClan();
        c.setGodine(4);
        ocekujGresku("Godine moraju biti izmedju 5 i 65!", c);
    }

    @Test
    public void odbijaGodineIznadMaksimuma() throws Exception {
        Clan c = noviClan();
        c.setGodine(66);
        ocekujGresku("Godine moraju biti izmedju 5 i 65!", c);
    }

    @Test
    public void odbijaEmailKojiVecPostoji() throws Exception {
        Clan c = noviClan();
        c.setEmail("lana.stojanovic@test.rs");
        ocekujGresku("Clan sa tim emailom vec postoji!", c);
    }

    @Test
    public void odbijaTelefonKojiVecPostoji() throws Exception {
        Clan c = new Clan(null, "Lana", "Peric", 9, "lana.peric@test.rs", "0641112222", TestBaza.kategorija(1));
        ocekujGresku("Clan sa tim telefonom vec postoji!", c);
    }

    @Test
    public void odbijaObjekatKojiNijeClan() throws Exception {
        Exception ex = assertThrows(Exception.class, () -> new SOAddClan().templateExecute(new Trener()));
        assertEquals("Prosledjeni objekat nije instanca klase Clan!", ex.getMessage());
    }

    @Test
    public void odbijaTelefonDrugogClanaIakoSeImenaRazlikuju() throws Exception {
        Clan c = noviClan();
        c.setTelefon("0655556666");
        ocekujGresku("Clan sa tim telefonom vec postoji!", c);
    }

    @Test
    public void odbijaEmailClanaIzDrugeKategorije() throws Exception {
        Clan c = noviClan();
        c.setEmail("mina.popovic@test.rs");
        ocekujGresku("Clan sa tim emailom vec postoji!", c);
    }

    @Test
    public void odbijaEmailKojiSeRazlikujeSamoUVelikimSlovima() throws Exception {
        Clan c = noviClan();
        c.setEmail("Lana.Stojanovic@test.rs");
        ocekujGresku("Clan sa tim emailom vec postoji!", c);
    }
}
