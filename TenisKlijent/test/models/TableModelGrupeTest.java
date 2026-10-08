package models;

import domain.Grupa;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import org.junit.Before;
import org.junit.Test;
import testutil.LazniServer;
import transfer.util.Operation;

public class TableModelGrupeTest {

    private LazniServer server;

    @Before
    public void pripremi() {
        server = testutil.TestKlijent.server();
    }

    @Test
    public void ucitavaGrupeIPrikazujeKolone() {
        TableModelGrupe model = new TableModelGrupe();
        String[] kolone = {"ID", "Naziv", "Kategorija", "Trener"};

        assertEquals(2, model.getRowCount());
        assertEquals(kolone.length, model.getColumnCount());
        for (int i = 0; i < kolone.length; i++) {
            assertEquals(kolone[i], model.getColumnName(i));
        }
        assertEquals(1L, model.getValueAt(0, 0));
        assertEquals("Mini Tenis A", model.getValueAt(0, 1));
        assertEquals("Pocetni", model.getValueAt(0, 2).toString());
        assertEquals("Ivan Ristic", model.getValueAt(0, 3).toString());
        assertNull(model.getValueAt(0, 4));
        assertEquals(2, model.getSelectedGrupa(0).getClanoviGrupe().size());
        assertEquals(2, model.getLista().size());
    }

    @Test
    public void pretragaPoNazivuIImenuTrenera() {
        TableModelGrupe model = new TableModelGrupe();

        model.setParametar("SREDNJI");
        assertEquals(1, model.getRowCount());
        assertEquals("srednji", ((Grupa) server.poslednji(Operation.GET_ALL_GRUPA)).getNaziv());

        model.setParametar("ivan");
        assertEquals(1, model.getRowCount());
        assertEquals("Mini Tenis A", model.getValueAt(0, 1));
    }

    @Test
    public void greskeSaServeraNeObarajuModel() {
        server.greska(Operation.GET_ALL_GRUPA, "Server ne radi");
        assertNull(new TableModelGrupe().getLista());

        TableModelGrupe model = new TableModelGrupe();
        server.greska(Operation.GET_ALL_GRUPA, "Server ne radi");
        try (testutil.HvatacLogova log = new testutil.HvatacLogova(TableModelGrupe.class)) {
            model.refreshTable();
            assertEquals("Osvezavanje tabele nije uspelo.", log.cekajGresku().getMessage());
        }
        assertEquals(2, model.getRowCount());
    }

    @Test
    public void nitOsvezavanjaSeZaustavljaPrekidom() throws Exception {
        Thread nit = new Thread(new TableModelGrupe());
        try (testutil.HvatacLogova log = new testutil.HvatacLogova(TableModelGrupe.class)) {
            nit.start();

            nit.interrupt();
            nit.join(5000);

            assertFalse(nit.isAlive());
            org.junit.Assert.assertTrue(log.greske().isEmpty());
        }
    }
}
