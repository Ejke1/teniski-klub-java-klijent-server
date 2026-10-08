package models;

import domain.Clan;
import javax.swing.event.TableModelEvent;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import testutil.LazniServer;
import transfer.util.Operation;

public class TableModelClanoviTest {

    private LazniServer server;

    @Before
    public void pripremi() {
        server = testutil.TestKlijent.server();
    }

    @Test
    public void ucitavaSveClanoveSaServera() {
        TableModelClanovi model = new TableModelClanovi();

        assertEquals(5, model.getRowCount());
        assertEquals(5, model.getLista().size());
        Clan kriterijum = (Clan) server.poslednji(Operation.GET_ALL_CLAN);
        assertEquals("", kriterijum.getIme());
    }

    @Test
    public void koloneIPrikazPodataka() {
        TableModelClanovi model = new TableModelClanovi();
        String[] kolone = {"ID", "Kategorija", "Ime", "Prezime", "Email", "Telefon", "Godine"};

        assertEquals(kolone.length, model.getColumnCount());
        for (int i = 0; i < kolone.length; i++) {
            assertEquals(kolone[i], model.getColumnName(i));
        }
        assertEquals(1L, model.getValueAt(0, 0));
        assertEquals("Pocetni", model.getValueAt(0, 1).toString());
        assertEquals("Lana", model.getValueAt(0, 2));
        assertEquals("Stojanovic", model.getValueAt(0, 3));
        assertEquals("lana@test.rs", model.getValueAt(0, 4));
        assertEquals("0641112222", model.getValueAt(0, 5));
        assertEquals(9, model.getValueAt(0, 6));
        assertNull(model.getValueAt(0, 7));
        assertEquals("Mina", model.getSelectedClan(3).getIme());
    }

    @Test
    public void pretragaSaljeParametarMalimSlovimaIObavestavaTabelu() {
        TableModelClanovi model = new TableModelClanovi();
        boolean[] promenjeno = {false};
        model.addTableModelListener((TableModelEvent e) -> promenjeno[0] = true);

        model.setParametar("MINA");

        assertEquals(1, model.getRowCount());
        assertEquals("Popovic", model.getValueAt(0, 3));
        assertTrue(promenjeno[0]);
        Clan kriterijum = (Clan) server.poslednji(Operation.GET_ALL_CLAN);
        assertEquals("mina", kriterijum.getIme());
        assertEquals("mina", kriterijum.getPrezime());
        assertEquals("mina", kriterijum.getEmail());
    }

    @Test
    public void osvezavanjeZadrzavaPretragu() {
        TableModelClanovi model = new TableModelClanovi();
        model.setParametar("vasic");
        server.izmeniClana(3, 8, 1);

        model.refreshTable();

        assertEquals(1, model.getRowCount());
        assertEquals(8, model.getValueAt(0, 6));
    }

    @Test
    public void greskaPriUcitavanjuNeObaraFormu() {
        server.greska(Operation.GET_ALL_CLAN, "Server ne radi");

        TableModelClanovi model = new TableModelClanovi();

        assertNull(model.getLista());
    }

    @Test
    public void greskaPriOsvezavanjuZadrzavaStareRezultate() {
        TableModelClanovi model = new TableModelClanovi();
        server.greska(Operation.GET_ALL_CLAN, "Server ne radi");

        try (testutil.HvatacLogova log = new testutil.HvatacLogova(TableModelClanovi.class)) {
            model.setParametar("mina");

            assertEquals("Osvezavanje tabele nije uspelo.", log.cekajGresku().getMessage());
        }
        assertEquals(5, model.getRowCount());
    }

    @Test
    public void nitOsvezavanjaSeZaustavljaPrekidom() throws Exception {
        TableModelClanovi model = new TableModelClanovi();
        Thread nit = new Thread(model);
        try (testutil.HvatacLogova log = new testutil.HvatacLogova(TableModelClanovi.class)) {
            nit.start();

            nit.interrupt();
            nit.join(5000);

            assertFalse(nit.isAlive());
            assertTrue(log.greske().isEmpty());
        }
    }
}
