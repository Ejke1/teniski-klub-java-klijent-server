package so.grupa;

import domain.ClanGrupe;
import domain.Grupa;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import org.junit.Before;
import org.junit.Test;
import testutil.TestBaza;

public class SOUpdateGrupaTest {

    @Before
    public void pripremiBazu() throws Exception {
        TestBaza.pripremi();
    }

    @Test
    public void menjaOpisKapacitetITrenera() throws Exception {
        Grupa g = TestBaza.grupa(1);
        g.setOpis("Novi opis grupe.");
        g.setMaxKapacitet(8);
        g.setTrener(TestBaza.trener(2));

        new SOUpdateGrupa().templateExecute(g);

        assertEquals("Novi opis grupe.", TestBaza.vrednost("SELECT Opis FROM Grupa WHERE GrupaID = 1"));
        assertEquals("8", TestBaza.vrednost("SELECT MaxKapacitet FROM Grupa WHERE GrupaID = 1"));
        assertEquals("2", TestBaza.vrednost("SELECT TrenerID FROM Grupa WHERE GrupaID = 1"));
        assertEquals(3, TestBaza.broj("SELECT COUNT(*) FROM ClanGrupe WHERE GrupaID = 1"));
    }

    @Test
    public void zamenjujeIUklanjaClanove() throws Exception {
        Grupa g = TestBaza.grupa(1);
        g.getClanoviGrupe().remove(2);
        g.getClanoviGrupe().get(1).setClan(TestBaza.clan(3));
        g.getClanoviGrupe().get(1).setNapomena("Zamenjen clan.");
        g.setBrojClanova(2);

        new SOUpdateGrupa().templateExecute(g);

        assertEquals(2, TestBaza.broj("SELECT COUNT(*) FROM ClanGrupe WHERE GrupaID = 1"));
        assertEquals("3", TestBaza.vrednost("SELECT ClanID FROM ClanGrupe WHERE GrupaID = 1 AND Rb = 2"));
        assertEquals("Zamenjen clan.", TestBaza.vrednost("SELECT Napomena FROM ClanGrupe WHERE GrupaID = 1 AND Rb = 2"));
        assertEquals(0, TestBaza.broj("SELECT COUNT(*) FROM ClanGrupe WHERE GrupaID = 1 AND Rb = 3"));
        assertEquals("2", TestBaza.vrednost("SELECT BrojClanova FROM Grupa WHERE GrupaID = 1"));
    }

    @Test
    public void dodajeNovogClana() throws Exception {
        Grupa g = TestBaza.grupa(2);
        g.getClanoviGrupe().add(new ClanGrupe(g, 3, "/", TestBaza.clan(6)));
        g.setBrojClanova(3);

        new SOUpdateGrupa().templateExecute(g);

        assertEquals(3, TestBaza.broj("SELECT COUNT(*) FROM ClanGrupe WHERE GrupaID = 2"));
        assertEquals("6", TestBaza.vrednost("SELECT ClanID FROM ClanGrupe WHERE GrupaID = 2 AND Rb = 3"));
    }

    @Test
    public void odbijaIzmenuSaManjeOdDvaClana() throws Exception {
        Grupa g = TestBaza.grupa(2);
        g.getClanoviGrupe().remove(1);
        g.setBrojClanova(1);

        Exception ex = assertThrows(Exception.class, () -> new SOUpdateGrupa().templateExecute(g));

        assertEquals("Morate uneti barem 2 clana!", ex.getMessage());
        assertEquals(2, TestBaza.broj("SELECT COUNT(*) FROM ClanGrupe WHERE GrupaID = 2"));
    }

    @Test
    public void odbijaKapacitetManjiOdBrojaClanova() throws Exception {
        Grupa g = TestBaza.grupa(1);
        g.setMaxKapacitet(2);

        Exception ex = assertThrows(Exception.class, () -> new SOUpdateGrupa().templateExecute(g));

        assertEquals("Maksimalni kapacitet je 2, a vi ste uneli 3!", ex.getMessage());
        assertEquals("10", TestBaza.vrednost("SELECT MaxKapacitet FROM Grupa WHERE GrupaID = 1"));
    }

    @Test
    public void odbijaKapacitetVanOpsega() throws Exception {
        Grupa g = TestBaza.grupa(1);
        g.setMaxKapacitet(1);
        Exception ex = assertThrows(Exception.class, () -> new SOUpdateGrupa().templateExecute(g));
        assertEquals("Maksimalni kapacitet mora biti izmedju 2 i 15!", ex.getMessage());

        g.setMaxKapacitet(16);
        ex = assertThrows(Exception.class, () -> new SOUpdateGrupa().templateExecute(g));
        assertEquals("Maksimalni kapacitet mora biti izmedju 2 i 15!", ex.getMessage());

        assertEquals("10", TestBaza.vrednost("SELECT MaxKapacitet FROM Grupa WHERE GrupaID = 1"));
    }
}
