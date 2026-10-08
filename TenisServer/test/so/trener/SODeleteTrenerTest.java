package so.trener;

import domain.Trener;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import org.junit.Before;
import org.junit.Test;
import testutil.TestBaza;

public class SODeleteTrenerTest {

    @Before
    public void pripremiBazu() throws Exception {
        TestBaza.pripremi();
    }

    @Test
    public void briseTreneraKojiNeVodiGrupu() throws Exception {
        new SOAddTrener().templateExecute(new Trener(null, "Milan", "Jovic", "0603030303", 7));
        Long id = Long.valueOf(TestBaza.vrednost("SELECT TrenerID FROM Trener WHERE BrojTelefona = '0603030303'"));

        new SODeleteTrener().templateExecute(new Trener(id, "Milan", "Jovic", "0603030303", 7));

        assertEquals(2, TestBaza.broj("SELECT COUNT(*) FROM Trener"));
    }

    @Test
    public void neBriseTreneraKojiVodiGrupu() throws Exception {
        assertThrows(Exception.class, () -> new SODeleteTrener().templateExecute(TestBaza.trener(1)));

        assertEquals(1, TestBaza.broj("SELECT COUNT(*) FROM Trener WHERE TrenerID = 1"));
    }
}
