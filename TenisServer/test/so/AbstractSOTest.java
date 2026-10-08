package so;

import db.DBBroker;
import domain.AbstractDomainObject;
import domain.Kategorija;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import testutil.TestBaza;

public class AbstractSOTest {

    private final Kategorija juniori = new Kategorija(null, "Juniori", "Takmicari do 18 godina.");

    @Before
    public void pripremiBazu() throws Exception {
        TestBaza.pripremi();
    }

    @Test
    public void potvrdjujeTransakcijuKadaJeOperacijaUspesna() throws Exception {
        AbstractSO so = new AbstractSO() {
            @Override
            protected void validate(AbstractDomainObject ado) {
            }

            @Override
            protected void execute(AbstractDomainObject ado) throws Exception {
                DBBroker.getInstance().insert(ado);
            }
        };

        so.templateExecute(juniori);

        assertEquals(1, TestBaza.broj("SELECT COUNT(*) FROM Kategorija WHERE Naziv = 'Juniori'"));
    }

    @Test
    public void ponistavaTransakcijuKadaIzvrsavanjePukne() {
        Exception greska = new Exception("Greska posle upisa");
        AbstractSO so = new AbstractSO() {
            @Override
            protected void validate(AbstractDomainObject ado) {
            }

            @Override
            protected void execute(AbstractDomainObject ado) throws Exception {
                DBBroker.getInstance().insert(ado);
                throw greska;
            }
        };

        Exception ex = assertThrows(Exception.class, () -> so.templateExecute(juniori));

        assertSame(greska, ex);
        assertEquals(0, broj());
    }

    @Test
    public void neIzvrsavaOperacijuAkoValidacijaNeProdje() {
        boolean[] izvrseno = {false};
        AbstractSO so = new AbstractSO() {
            @Override
            protected void validate(AbstractDomainObject ado) throws Exception {
                throw new Exception("Neispravni podaci");
            }

            @Override
            protected void execute(AbstractDomainObject ado) {
                izvrseno[0] = true;
            }
        };

        Exception ex = assertThrows(Exception.class, () -> so.templateExecute(juniori));

        assertEquals("Neispravni podaci", ex.getMessage());
        assertTrue(!izvrseno[0]);
    }

    private int broj() {
        try {
            return TestBaza.broj("SELECT COUNT(*) FROM Kategorija WHERE Naziv = 'Juniori'");
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}
