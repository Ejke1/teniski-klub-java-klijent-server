package so.clan;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import org.junit.Before;
import org.junit.Test;
import testutil.TestBaza;

public class SODeleteClanTest {

    @Before
    public void pripremiBazu() throws Exception {
        TestBaza.pripremi();
    }

    @Test
    public void briseClanaKojiNijeUGrupi() throws Exception {
        new SODeleteClan().templateExecute(TestBaza.clan(6));

        assertEquals(0, TestBaza.broj("SELECT COUNT(*) FROM Clan WHERE ClanID = 6"));
        assertEquals(5, TestBaza.broj("SELECT COUNT(*) FROM Clan"));
    }

    @Test
    public void neBriseClanaKojiJeUGrupi() throws Exception {
        assertThrows(Exception.class, () -> new SODeleteClan().templateExecute(TestBaza.clan(1)));

        assertEquals(1, TestBaza.broj("SELECT COUNT(*) FROM Clan WHERE ClanID = 1"));
        assertEquals(3, TestBaza.broj("SELECT COUNT(*) FROM ClanGrupe WHERE GrupaID = 1"));
    }
}
