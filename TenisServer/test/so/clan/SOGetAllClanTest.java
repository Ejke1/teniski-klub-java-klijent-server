package so.clan;

import domain.Clan;
import java.util.ArrayList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import testutil.TestBaza;

public class SOGetAllClanTest {

    @Before
    public void pripremiBazu() throws Exception {
        TestBaza.pripremi();
    }

    private ArrayList<Clan> pretrazi(String tekst, Long kategorijaID) throws Exception {
        Clan kriterijum = new Clan(null, tekst, tekst, 0, tekst, "",
                kategorijaID == null ? null : TestBaza.kategorija(kategorijaID));
        SOGetAllClan so = new SOGetAllClan();
        so.templateExecute(kriterijum);
        return so.getLista();
    }

    @Test
    public void vracaSveClanoveSaKategorijom() throws Exception {
        ArrayList<Clan> lista = pretrazi("", null);

        assertEquals(6, lista.size());
        assertEquals(Long.valueOf(1), lista.get(0).getClanID());
        assertEquals("Lana", lista.get(0).getIme());
        assertEquals("Pocetni", lista.get(0).getKategorija().getNaziv());
    }

    @Test
    public void pretrazujePoImenuPrezimenuIEmailu() throws Exception {
        ArrayList<Clan> lista = pretrazi("mina", null);

        assertEquals(1, lista.size());
        assertEquals("Popovic", lista.get(0).getPrezime());
    }

    @Test
    public void filtriraPoKategoriji() throws Exception {
        ArrayList<Clan> lista = pretrazi("", 1L);

        assertEquals(3, lista.size());
        for (Clan c : lista) {
            assertEquals(Long.valueOf(1), c.getKategorija().getKategorijaID());
        }
    }

    @Test
    public void vracaPraznuListuKadaNemaPoklapanja() throws Exception {
        assertTrue(pretrazi("nepostojeci", null).isEmpty());
    }
}
