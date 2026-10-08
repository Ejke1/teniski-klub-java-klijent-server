package domain;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class DomenskiObjektiTest {

    private final List<AbstractDomainObject> objekti = Arrays.asList(
            new Administrator(1L, "A", "B", "c", "d"),
            new Kategorija(1L, "K", "O"),
            new Trener(1L, "T", "R", "0601010101", 5),
            new Clan(1L, "C", "L", 10, "c@test.rs", "0641112222", new Kategorija(1L, "K", "O")),
            new Grupa(1L, "G", "O", 2, 10, new Kategorija(1L, "K", "O"), new Trener(1L, "T", "R", "0601010101", 5),
                    new Administrator(1L, "A", "B", "c", "d"), null),
            new ClanGrupe(new Grupa(1L, "G", "O", 2, 10, null, null, null, null), 1, "/",
                    new Clan(1L, "C", "L", 10, "c@test.rs", "0641112222", null)));

    @Test
    public void sviDomenskiObjektiSeMoguSlatiPrekoMreze() {
        for (AbstractDomainObject o : objekti) {
            assertTrue(o.getClass().getSimpleName(), o instanceof Serializable);
        }
    }

    @Test
    public void sviDomenskiObjektiDajuSveDeloveSqlUpita() {
        for (AbstractDomainObject o : objekti) {
            String ime = o.getClass().getSimpleName();
            assertTrue(ime, o.nazivTabele().trim().length() > 0);
            assertTrue(ime, o.alijas().trim().length() > 0);
            assertNotNull(ime, o.join());
            assertTrue(ime, o.koloneZaInsert().trim().startsWith("("));
            assertTrue(ime, o.vrednostiZaInsert().trim().length() > 0);
            assertTrue(ime, o.vrednostiZaUpdate().contains("="));
            assertTrue(ime, o.uslov().contains("= 1"));
            assertTrue(ime, o.dodatniUslov().trim().startsWith("WHERE"));
            assertTrue(ime, o.orderBy().contains("ORDER BY"));
        }
    }
}
