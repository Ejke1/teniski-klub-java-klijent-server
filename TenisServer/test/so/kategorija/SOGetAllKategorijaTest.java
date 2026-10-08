package so.kategorija;

import domain.Kategorija;
import java.util.ArrayList;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;
import testutil.TestBaza;

public class SOGetAllKategorijaTest {

    @Before
    public void pripremiBazu() throws Exception {
        TestBaza.pripremi();
    }

    @Test
    public void vracaSveKategorijePoRedosleduId() throws Exception {
        SOGetAllKategorija so = new SOGetAllKategorija();
        so.templateExecute(new Kategorija(null, "", ""));
        ArrayList<Kategorija> lista = so.getLista();

        assertEquals(4, lista.size());
        assertEquals("Pocetni", lista.get(0).getNaziv());
        assertEquals("Rekreativci", lista.get(3).getNaziv());
    }
}
