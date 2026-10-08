package so.clan;

import domain.Clan;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import org.junit.Before;
import org.junit.Test;
import testutil.TestBaza;

public class SOUpdateClanTest {

    @Before
    public void pripremiBazu() throws Exception {
        TestBaza.pripremi();
    }

    @Test
    public void menjaPodatkeClana() throws Exception {
        Clan lana = TestBaza.clan(1);
        lana.setGodine(10);
        lana.setEmail("lana.s@test.rs");
        lana.setTelefon("0641119999");
        lana.setKategorija(TestBaza.kategorija(2));

        new SOUpdateClan().templateExecute(lana);

        assertEquals("10", TestBaza.vrednost("SELECT Godine FROM Clan WHERE ClanID = 1"));
        assertEquals("lana.s@test.rs", TestBaza.vrednost("SELECT Email FROM Clan WHERE ClanID = 1"));
        assertEquals("0641119999", TestBaza.vrednost("SELECT Telefon FROM Clan WHERE ClanID = 1"));
        assertEquals("2", TestBaza.vrednost("SELECT KategorijaID FROM Clan WHERE ClanID = 1"));
    }

    @Test
    public void dozvoljavaDaClanZadrziSvojEmailITelefon() throws Exception {
        Clan lana = TestBaza.clan(1);
        lana.setGodine(11);

        new SOUpdateClan().templateExecute(lana);

        assertEquals("11", TestBaza.vrednost("SELECT Godine FROM Clan WHERE ClanID = 1"));
    }

    @Test
    public void odbijaEmailDrugogClana() throws Exception {
        Clan lana = TestBaza.clan(1);
        lana.setEmail("uros.milosevic@test.rs");

        Exception ex = assertThrows(Exception.class, () -> new SOUpdateClan().templateExecute(lana));

        assertEquals("Clan sa tim emailom vec postoji!", ex.getMessage());
        assertEquals("lana.stojanovic@test.rs", TestBaza.vrednost("SELECT Email FROM Clan WHERE ClanID = 1"));
    }

    @Test
    public void odbijaNeispravneGodine() throws Exception {
        Clan lana = TestBaza.clan(1);
        lana.setGodine(70);

        Exception ex = assertThrows(Exception.class, () -> new SOUpdateClan().templateExecute(lana));

        assertEquals("Godine moraju biti izmedju 5 i 65!", ex.getMessage());
        assertEquals("9", TestBaza.vrednost("SELECT Godine FROM Clan WHERE ClanID = 1"));
    }

    @Test
    public void odbijaNeispravanTelefon() throws Exception {
        Clan lana = TestBaza.clan(1);
        lana.setTelefon("+381641112222");

        Exception ex = assertThrows(Exception.class, () -> new SOUpdateClan().templateExecute(lana));

        assertEquals("Telefon mora biti u formatu 06XXXXXXXX!", ex.getMessage());
    }

    @Test
    public void odbijaTelefonDrugogClana() throws Exception {
        Clan lana = TestBaza.clan(1);
        lana.setTelefon("0621234567");

        Exception ex = assertThrows(Exception.class, () -> new SOUpdateClan().templateExecute(lana));

        assertEquals("Clan sa tim telefonom vec postoji!", ex.getMessage());
        assertEquals("0641112222", TestBaza.vrednost("SELECT Telefon FROM Clan WHERE ClanID = 1"));
    }

    @Test
    public void odbijaNeispravanEmail() throws Exception {
        Clan lana = TestBaza.clan(1);
        lana.setEmail("lana.test.rs");

        Exception ex = assertThrows(Exception.class, () -> new SOUpdateClan().templateExecute(lana));

        assertEquals("Email nije u ispravnom formatu!", ex.getMessage());
        assertEquals("lana.stojanovic@test.rs", TestBaza.vrednost("SELECT Email FROM Clan WHERE ClanID = 1"));
    }
}
