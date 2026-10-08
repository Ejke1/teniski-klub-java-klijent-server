package so.grupa;

import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;
import testutil.TestBaza;

public class SODeleteGrupaTest {

    @Before
    public void pripremiBazu() throws Exception {
        TestBaza.pripremi();
    }

    @Test
    public void briseGrupuINjeneStavkeAliNeIClanove() throws Exception {
        new SODeleteGrupa().templateExecute(TestBaza.grupa(1));

        assertEquals(0, TestBaza.broj("SELECT COUNT(*) FROM Grupa WHERE GrupaID = 1"));
        assertEquals(0, TestBaza.broj("SELECT COUNT(*) FROM ClanGrupe WHERE GrupaID = 1"));
        assertEquals(2, TestBaza.broj("SELECT COUNT(*) FROM ClanGrupe WHERE GrupaID = 2"));
        assertEquals(6, TestBaza.broj("SELECT COUNT(*) FROM Clan"));
    }
}
