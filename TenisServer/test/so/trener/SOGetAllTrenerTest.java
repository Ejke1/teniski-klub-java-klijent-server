package so.trener;

import domain.Trener;
import java.util.ArrayList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import testutil.TestBaza;

public class SOGetAllTrenerTest {

    @Before
    public void pripremiBazu() throws Exception {
        TestBaza.pripremi();
    }

    private ArrayList<Trener> pretrazi(String tekst) throws Exception {
        SOGetAllTrener so = new SOGetAllTrener();
        so.templateExecute(new Trener(null, tekst, tekst, "", 0));
        return so.getLista();
    }

    @Test
    public void vracaSveTrenereSaSvimPodacima() throws Exception {
        ArrayList<Trener> lista = pretrazi("");

        assertEquals(2, lista.size());
        Trener ivan = lista.get(0);
        assertEquals(Long.valueOf(1), ivan.getTrenerID());
        assertEquals("Ivan", ivan.getIme());
        assertEquals("Ristic", ivan.getPrezime());
        assertEquals("0601010101", ivan.getBrojTelefona());
        assertEquals(8, ivan.getGodineIskustva());
    }

    @Test
    public void pretrazujePoImenu() throws Exception {
        ArrayList<Trener> lista = pretrazi("tamara");

        assertEquals(1, lista.size());
        assertEquals("Vukovic", lista.get(0).getPrezime());
    }

    @Test
    public void pretrazujePoPrezimenu() throws Exception {
        ArrayList<Trener> lista = pretrazi("rist");

        assertEquals(1, lista.size());
        assertEquals("Ivan", lista.get(0).getIme());
    }

    @Test
    public void vracaPraznuListuKadaNemaPoklapanja() throws Exception {
        assertTrue(pretrazi("nepostojeci").isEmpty());
    }
}
