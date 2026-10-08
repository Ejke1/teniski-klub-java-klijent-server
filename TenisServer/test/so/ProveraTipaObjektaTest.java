package so;

import domain.AbstractDomainObject;
import domain.Clan;
import domain.Kategorija;
import java.util.Arrays;
import java.util.Collection;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import so.clan.SOAddClan;
import so.clan.SODeleteClan;
import so.clan.SOGetAllClan;
import so.clan.SOUpdateClan;
import so.grupa.SOAddGrupa;
import so.grupa.SODeleteGrupa;
import so.grupa.SOGetAllGrupa;
import so.grupa.SOUpdateGrupa;
import so.kategorija.SOGetAllKategorija;
import so.login.SOLogin;
import so.trener.SOAddTrener;
import so.trener.SODeleteTrener;
import so.trener.SOGetAllTrener;
import so.trener.SOUpdateTrener;
import testutil.TestBaza;

@RunWith(Parameterized.class)
public class ProveraTipaObjektaTest {

    @Parameters(name = "{0}")
    public static Collection<Object[]> operacije() {
        return Arrays.asList(new Object[][]{
            {"SOAddClan", new SOAddClan(), "Clan"},
            {"SOUpdateClan", new SOUpdateClan(), "Clan"},
            {"SODeleteClan", new SODeleteClan(), "Clan"},
            {"SOGetAllClan", new SOGetAllClan(), "Clan"},
            {"SOAddTrener", new SOAddTrener(), "Trener"},
            {"SOUpdateTrener", new SOUpdateTrener(), "Trener"},
            {"SODeleteTrener", new SODeleteTrener(), "Trener"},
            {"SOGetAllTrener", new SOGetAllTrener(), "Trener"},
            {"SOAddGrupa", new SOAddGrupa(), "Grupa"},
            {"SOUpdateGrupa", new SOUpdateGrupa(), "Grupa"},
            {"SODeleteGrupa", new SODeleteGrupa(), "Grupa"},
            {"SOGetAllGrupa", new SOGetAllGrupa(), "Grupa"},
            {"SOGetAllKategorija", new SOGetAllKategorija(), "Kategorija"},
            {"SOLogin", new SOLogin(), "Administrator"}
        });
    }

    private final AbstractSO so;
    private final String klasa;

    public ProveraTipaObjektaTest(String naziv, AbstractSO so, String klasa) {
        this.so = so;
        this.klasa = klasa;
    }

    @Before
    public void pripremiBazu() throws Exception {
        TestBaza.pripremi();
    }

    @Test
    public void odbijaObjekatPogresneKlase() throws Exception {
        AbstractDomainObject pogresan = "Kategorija".equals(klasa) ? new Clan() : new Kategorija(1L, "Pocetni", "Opis");

        Exception ex = assertThrows(Exception.class, () -> so.templateExecute(pogresan));

        assertEquals("Prosledjeni objekat nije instanca klase " + klasa + "!", ex.getMessage());
        assertEquals(4, TestBaza.broj("SELECT COUNT(*) FROM Kategorija"));
    }
}
