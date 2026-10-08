package so.trener;

import domain.Trener;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import org.junit.Before;
import org.junit.Test;
import testutil.TestBaza;

public class SOUpdateTrenerTest {

    @Before
    public void pripremiBazu() throws Exception {
        TestBaza.pripremi();
    }

    @Test
    public void menjaTelefonIGodineIskustva() throws Exception {
        Trener tamara = TestBaza.trener(2);
        tamara.setBrojTelefona("0609998877");
        tamara.setGodineIskustva(12);

        new SOUpdateTrener().templateExecute(tamara);

        assertEquals("0609998877", TestBaza.vrednost("SELECT BrojTelefona FROM Trener WHERE TrenerID = 2"));
        assertEquals("12", TestBaza.vrednost("SELECT GodineIskustva FROM Trener WHERE TrenerID = 2"));
    }

    @Test
    public void odbijaNeispravanTelefon() throws Exception {
        Trener tamara = TestBaza.trener(2);
        tamara.setBrojTelefona("06020");

        Exception ex = assertThrows(Exception.class, () -> new SOUpdateTrener().templateExecute(tamara));

        assertEquals("Telefon mora biti u formatu 06XXXXXXXX!", ex.getMessage());
        assertEquals("0602020202", TestBaza.vrednost("SELECT BrojTelefona FROM Trener WHERE TrenerID = 2"));
    }

    @Test
    public void odbijaTelefonDrugogTrenera() throws Exception {
        Trener tamara = TestBaza.trener(2);
        tamara.setBrojTelefona("0601010101");

        Exception ex = assertThrows(Exception.class, () -> new SOUpdateTrener().templateExecute(tamara));

        assertEquals("Trener sa tim brojem telefona vec postoji!", ex.getMessage());
        assertEquals("0602020202", TestBaza.vrednost("SELECT BrojTelefona FROM Trener WHERE TrenerID = 2"));
    }

    @Test
    public void odbijaGodineIskustvaVanOpsega() throws Exception {
        Trener tamara = TestBaza.trener(2);
        tamara.setGodineIskustva(4);
        Exception ex = assertThrows(Exception.class, () -> new SOUpdateTrener().templateExecute(tamara));
        assertEquals("Godine moraju biti izmedju 5 i 70!", ex.getMessage());

        tamara.setGodineIskustva(71);
        ex = assertThrows(Exception.class, () -> new SOUpdateTrener().templateExecute(tamara));
        assertEquals("Godine moraju biti izmedju 5 i 70!", ex.getMessage());

        assertEquals("11", TestBaza.vrednost("SELECT GodineIskustva FROM Trener WHERE TrenerID = 2"));
    }

    @Test
    public void prihvataGranicneGodineIskustva() throws Exception {
        Trener tamara = TestBaza.trener(2);
        tamara.setGodineIskustva(5);
        new SOUpdateTrener().templateExecute(tamara);
        assertEquals("5", TestBaza.vrednost("SELECT GodineIskustva FROM Trener WHERE TrenerID = 2"));

        tamara.setGodineIskustva(70);
        new SOUpdateTrener().templateExecute(tamara);
        assertEquals("70", TestBaza.vrednost("SELECT GodineIskustva FROM Trener WHERE TrenerID = 2"));
    }
}
