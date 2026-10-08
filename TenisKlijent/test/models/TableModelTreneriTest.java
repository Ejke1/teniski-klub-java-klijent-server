package models;

import domain.Trener;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import org.junit.Before;
import org.junit.Test;
import testutil.LazniServer;
import transfer.util.Operation;

public class TableModelTreneriTest {

    private LazniServer server;

    @Before
    public void pripremi() {
        server = testutil.TestKlijent.server();
    }

    @Test
    public void ucitavaTrenereIPrikazujeKolone() {
        TableModelTreneri model = new TableModelTreneri();
        String[] kolone = {"ID", "Ime", "Prezime", "Telefon", "Godine iskustva"};

        assertEquals(2, model.getRowCount());
        assertEquals(kolone.length, model.getColumnCount());
        for (int i = 0; i < kolone.length; i++) {
            assertEquals(kolone[i], model.getColumnName(i));
        }
        assertEquals(2L, model.getValueAt(1, 0));
        assertEquals("Tamara", model.getValueAt(1, 1));
        assertEquals("Vukovic", model.getValueAt(1, 2));
        assertEquals("0602020202", model.getValueAt(1, 3));
        assertEquals(11, model.getValueAt(1, 4));
        assertNull(model.getValueAt(1, 5));
        assertEquals("Ivan", model.getSelectedTrener(0).getIme());
        assertEquals(2, model.getLista().size());
    }

    @Test
    public void pretragaPoImenuIPrezimenu() {
        TableModelTreneri model = new TableModelTreneri();

        model.setParametar("RISTIC");

        assertEquals(1, model.getRowCount());
        assertEquals("Ivan", model.getValueAt(0, 1));
        assertEquals("ristic", ((Trener) server.poslednji(Operation.GET_ALL_TRENER)).getPrezime());
    }

    @Test
    public void greskeSaServeraNeObarajuModel() {
        server.greska(Operation.GET_ALL_TRENER, "Server ne radi");
        assertNull(new TableModelTreneri().getLista());

        TableModelTreneri model = new TableModelTreneri();
        server.greska(Operation.GET_ALL_TRENER, "Server ne radi");
        try (testutil.HvatacLogova log = new testutil.HvatacLogova(TableModelTreneri.class)) {
            model.refreshTable();
            assertEquals("Osvezavanje tabele nije uspelo.", log.cekajGresku().getMessage());
        }
        assertEquals(2, model.getRowCount());
    }

    @Test
    public void nitOsvezavanjaSeZaustavljaPrekidom() throws Exception {
        Thread nit = new Thread(new TableModelTreneri());
        try (testutil.HvatacLogova log = new testutil.HvatacLogova(TableModelTreneri.class)) {
            nit.start();

            nit.interrupt();
            nit.join(5000);

            assertFalse(nit.isAlive());
            org.junit.Assert.assertTrue(log.greske().isEmpty());
        }
    }
}
