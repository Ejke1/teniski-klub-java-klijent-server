package so.grupa;

import domain.Clan;
import domain.ClanGrupe;
import domain.Grupa;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import org.junit.Before;
import org.junit.Test;
import testutil.TestBaza;

public class SOAddGrupaTest {

    @Before
    public void pripremiBazu() throws Exception {
        TestBaza.pripremi();
    }

    private void ocekujGresku(String poruka, Grupa g) throws Exception {
        Exception ex = assertThrows(Exception.class, () -> new SOAddGrupa().templateExecute(g));
        assertEquals(poruka, ex.getMessage());
        assertEquals(2, TestBaza.broj("SELECT COUNT(*) FROM Grupa"));
        assertEquals(5, TestBaza.broj("SELECT COUNT(*) FROM ClanGrupe"));
    }

    @Test
    public void cuvaGrupuISveNjeneClanove() throws Exception {
        Grupa g = TestBaza.novaGrupa("Test grupa", 2, 3);

        new SOAddGrupa().templateExecute(g);

        assertNotNull(g.getGrupaID());
        assertEquals(1, TestBaza.broj("SELECT COUNT(*) FROM Grupa WHERE Naziv = 'Test grupa'"));
        assertEquals(2, TestBaza.broj("SELECT COUNT(*) FROM ClanGrupe WHERE GrupaID = " + g.getGrupaID()));
        assertEquals("3", TestBaza.vrednost("SELECT ClanID FROM ClanGrupe WHERE GrupaID = " + g.getGrupaID() + " AND Rb = 2"));
        assertEquals("2", TestBaza.vrednost("SELECT BrojClanova FROM Grupa WHERE GrupaID = " + g.getGrupaID()));
    }

    @Test
    public void prihvataGranicneKapacitete() throws Exception {
        Grupa najmanja = TestBaza.novaGrupa("Kapacitet dva", 1, 2);
        najmanja.setMaxKapacitet(2);
        new SOAddGrupa().templateExecute(najmanja);

        Grupa najveca = TestBaza.novaGrupa("Kapacitet petnaest", 1, 2, 3);
        najveca.setMaxKapacitet(15);
        new SOAddGrupa().templateExecute(najveca);

        assertEquals(4, TestBaza.broj("SELECT COUNT(*) FROM Grupa"));
    }

    @Test
    public void odbijaKapacitetManjiOdDva() throws Exception {
        Grupa g = TestBaza.novaGrupa("Test grupa", 2, 3);
        g.setMaxKapacitet(1);
        ocekujGresku("Maksimalni kapacitet mora biti izmedju 2 i 15!", g);
    }

    @Test
    public void odbijaKapacitetVeciOdPetnaest() throws Exception {
        Grupa g = TestBaza.novaGrupa("Test grupa", 2, 3);
        g.setMaxKapacitet(16);
        ocekujGresku("Maksimalni kapacitet mora biti izmedju 2 i 15!", g);
    }

    @Test
    public void odbijaViseClanovaOdKapaciteta() throws Exception {
        Grupa g = TestBaza.novaGrupa("Test grupa", 1, 2, 3);
        g.setMaxKapacitet(2);
        ocekujGresku("Maksimalni kapacitet je 2, a vi ste uneli 3!", g);
    }

    @Test
    public void odbijaGrupuSaManjeOdDvaClana() throws Exception {
        Grupa g = TestBaza.novaGrupa("Test grupa", 2);
        ocekujGresku("Morate uneti barem 2 clana!", g);
    }

    @Test
    public void odbijaNazivKojiVecPostoji() throws Exception {
        Grupa g = TestBaza.novaGrupa("Mini Tenis A", 2, 3);
        ocekujGresku("Grupa sa tim nazivom vec postoji!", g);
    }

    @Test
    public void ponistavaCeluTransakcijuAkoUpisClanaNeUspe() throws Exception {
        Grupa g = TestBaza.novaGrupa("Test grupa", 2, 3);
        Clan nepostojeci = new Clan(999L, "Nema", "Ga", 10, "nema.ga@test.rs", "0640000000", TestBaza.kategorija(1));
        g.getClanoviGrupe().add(new ClanGrupe(g, 3, "/", nepostojeci));
        g.setBrojClanova(3);

        assertThrows(Exception.class, () -> new SOAddGrupa().templateExecute(g));

        assertEquals(0, TestBaza.broj("SELECT COUNT(*) FROM Grupa WHERE Naziv = 'Test grupa'"));
        assertEquals(5, TestBaza.broj("SELECT COUNT(*) FROM ClanGrupe"));
    }
}
