package so.trener;

import domain.Trener;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import org.junit.Before;
import org.junit.Test;
import testutil.TestBaza;

public class SOAddTrenerTest {

    @Before
    public void pripremiBazu() throws Exception {
        TestBaza.pripremi();
    }

    private Trener noviTrener() {
        return new Trener(null, "Milan", "Jovic", "0603030303", 7);
    }

    private void ocekujGresku(String poruka, Trener t) throws Exception {
        Exception ex = assertThrows(Exception.class, () -> new SOAddTrener().templateExecute(t));
        assertEquals(poruka, ex.getMessage());
        assertEquals(2, TestBaza.broj("SELECT COUNT(*) FROM Trener"));
    }

    @Test
    public void dodajeIspravnogTrenera() throws Exception {
        new SOAddTrener().templateExecute(noviTrener());

        assertEquals(3, TestBaza.broj("SELECT COUNT(*) FROM Trener"));
        assertEquals("7", TestBaza.vrednost("SELECT GodineIskustva FROM Trener WHERE BrojTelefona = '0603030303'"));
    }

    @Test
    public void odbijaNeispravanTelefon() throws Exception {
        Trener t = noviTrener();
        t.setBrojTelefona("060-303-0303");
        ocekujGresku("Telefon mora biti u formatu 06XXXXXXXX!", t);
    }

    @Test
    public void odbijaGodineIskustvaVanOpsega() throws Exception {
        Trener t = noviTrener();
        t.setGodineIskustva(4);
        ocekujGresku("Godine moraju biti izmedju 5 i 70!", t);

        t.setGodineIskustva(71);
        ocekujGresku("Godine moraju biti izmedju 5 i 70!", t);
    }

    @Test
    public void odbijaTelefonKojiVecPostoji() throws Exception {
        Trener t = new Trener(null, "Ivan", "Petrovic", "0601010101", 9);
        ocekujGresku("Trener sa tim brojem telefona vec postoji!", t);
    }

    @Test
    public void odbijaTelefonDrugogTreneraIakoSeImenaRazlikuju() throws Exception {
        Trener t = noviTrener();
        t.setBrojTelefona("0602020202");
        ocekujGresku("Trener sa tim brojem telefona vec postoji!", t);
    }
}
