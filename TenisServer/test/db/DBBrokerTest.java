package db;

import domain.AbstractDomainObject;
import domain.Kategorija;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import testutil.TestBaza;

public class DBBrokerTest {

    private DBBroker broker;

    @Before
    public void pripremi() throws Exception {
        TestBaza.pripremi();
        broker = DBBroker.getInstance();
    }

    @After
    public void ponisti() throws Exception {
        broker.getConnection().rollback();
    }

    @Test
    public void uvekVracaIstuInstancu() {
        assertSame(broker, DBBroker.getInstance());
    }

    @Test
    public void koristiTestBazuITransakcije() throws Exception {
        assertNotNull(broker.getConnection());
        assertFalse(broker.getConnection().getAutoCommit());
        assertTrue(broker.getConnection().getCatalog().toLowerCase().endsWith("_test"));
    }

    @Test
    public void selectVracaListuObjekata() throws Exception {
        ArrayList<AbstractDomainObject> lista = broker.select(new Kategorija(null, "", ""));

        assertEquals(4, lista.size());
        assertEquals("Pocetni", ((Kategorija) lista.get(0)).getNaziv());
    }

    @Test
    public void selectPrimenjujeUsloveIzObjekta() throws Exception {
        ArrayList<AbstractDomainObject> lista = broker.select(new Kategorija(null, "napred", ""));

        assertEquals(1, lista.size());
        assertEquals(Long.valueOf(3), ((Kategorija) lista.get(0)).getKategorijaID());
    }

    @Test
    public void insertVracaGenerisaniKljuc() throws Exception {
        PreparedStatement ps = broker.insert(new Kategorija(null, "Juniori", "Takmicari"));
        ResultSet kljucevi = ps.getGeneratedKeys();

        assertTrue(kljucevi.next());
        long id = kljucevi.getLong(1);
        assertTrue(id > 4);
        broker.getConnection().commit();
        assertEquals("Juniori", TestBaza.vrednost("SELECT Naziv FROM Kategorija WHERE KategorijaID = " + id));
    }

    @Test
    public void updateMenjaRedPoUslovu() throws Exception {
        broker.update(new Kategorija(4L, "Rekreacija", "Novi opis"));
        broker.getConnection().commit();

        assertEquals("Rekreacija", TestBaza.vrednost("SELECT Naziv FROM Kategorija WHERE KategorijaID = 4"));
        assertEquals("Novi opis", TestBaza.vrednost("SELECT Opis FROM Kategorija WHERE KategorijaID = 4"));
    }

    @Test
    public void deleteBriseRedPoUslovu() throws Exception {
        broker.delete(new Kategorija(4L, "", ""));
        broker.getConnection().commit();

        assertEquals(0, TestBaza.broj("SELECT COUNT(*) FROM Kategorija WHERE KategorijaID = 4"));
    }

    @Test
    public void rollbackPonistavaNepotvrdjeneIzmene() throws Exception {
        broker.delete(new Kategorija(4L, "", ""));
        broker.getConnection().rollback();

        assertEquals(1, TestBaza.broj("SELECT COUNT(*) FROM Kategorija WHERE KategorijaID = 4"));
    }

    @Test
    public void neispravnaKonfiguracijaNeObaraServer() throws Exception {
        String prethodna = System.getProperty("dbconfig");
        System.setProperty("dbconfig", "ne-postoji-dbconfig.properties");
        try {
            java.lang.reflect.Constructor<DBBroker> konstruktor = DBBroker.class.getDeclaredConstructor();
            konstruktor.setAccessible(true);

            DBBroker bezBaze = konstruktor.newInstance();

            org.junit.Assert.assertNull(bezBaze.getConnection());
        } finally {
            System.setProperty("dbconfig", prethodna);
        }
    }
}
