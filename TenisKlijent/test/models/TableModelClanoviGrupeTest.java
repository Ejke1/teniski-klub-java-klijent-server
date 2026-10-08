package models;

import domain.Clan;
import domain.ClanGrupe;
import domain.Kategorija;
import java.util.ArrayList;
import javax.swing.event.TableModelEvent;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import testutil.LazniServer;
import transfer.util.Operation;

public class TableModelClanoviGrupeTest {

    private TableModelClanoviGrupe model;
    private Clan lana;
    private Clan uros;
    private Clan petar;
    private int brojDogadjaja;

    private static Clan clan(long id, String ime, String prezime, int godine) {
        return new Clan(id, ime, prezime, godine, ime.toLowerCase() + "@test.rs", "0640000000",
                new Kategorija(1L, "Pocetni", "Opis"));
    }

    @Before
    public void pripremi() {
        model = new TableModelClanoviGrupe();
        lana = clan(1, "Lana", "Stojanovic", 9);
        uros = clan(2, "Uros", "Milosevic", 10);
        petar = clan(3, "Petar", "Vasic", 7);
        brojDogadjaja = 0;
        model.addTableModelListener((TableModelEvent e) -> brojDogadjaja++);
    }

    private void dodaj(Clan c) {
        model.dodajClana(new ClanGrupe(null, -1, "/", c));
    }

    @Test
    public void imaTriKoloneBezNapomene() {
        assertEquals(3, model.getColumnCount());
        assertEquals("Rb", model.getColumnName(0));
        assertEquals("Ime i prezime", model.getColumnName(1));
        assertEquals("Godine", model.getColumnName(2));
    }

    @Test
    public void dodavanjeDodeljujeRedneBrojeveRedom() {
        dodaj(lana);
        dodaj(uros);
        dodaj(petar);

        assertEquals(3, model.getRowCount());
        assertEquals(1, model.getStavka(0).getRb());
        assertEquals(2, model.getStavka(1).getRb());
        assertEquals(3, model.getStavka(2).getRb());
        assertEquals(3, brojDogadjaja);
    }

    @Test
    public void prikazujeRbImeIPrezimeIGodine() {
        dodaj(lana);

        assertEquals(1, model.getValueAt(0, 0));
        assertEquals("Lana Stojanovic", model.getValueAt(0, 1));
        assertEquals(9, model.getValueAt(0, 2));
    }

    @Test
    public void brisanjePrenumerisePreostaleClanove() {
        dodaj(lana);
        dodaj(uros);
        dodaj(petar);

        model.obrisiClana(0);

        assertEquals(2, model.getRowCount());
        assertSame(uros, model.getStavka(0).getClan());
        assertEquals(1, model.getStavka(0).getRb());
        assertSame(petar, model.getStavka(1).getClan());
        assertEquals(2, model.getStavka(1).getRb());
    }

    @Test
    public void dodavanjePosleBrisanjaNastavljaRedneBrojeve() {
        dodaj(lana);
        dodaj(uros);
        model.obrisiClana(1);

        dodaj(petar);

        assertEquals(2, model.getStavka(1).getRb());
    }

    @Test
    public void zamenaClanaZadrzavaRedniBroj() {
        dodaj(lana);
        dodaj(uros);

        model.izmeniClana(1, petar, "Zamena");

        assertEquals(2, model.getRowCount());
        assertSame(petar, model.getStavka(1).getClan());
        assertEquals("Zamena", model.getStavka(1).getNapomena());
        assertEquals(2, model.getStavka(1).getRb());
        assertEquals("Petar Vasic", model.getValueAt(1, 1));
    }

    @Test
    public void prepoznajeClanaKojiJeVecUGrupi() {
        dodaj(lana);
        dodaj(uros);

        assertTrue(model.postojiClan(clan(1, "Lana", "Stojanovic", 9)));
        assertFalse(model.postojiClan(petar));
    }

    @Test
    public void priZameniIgnoriseRedKojiSeMenja() {
        dodaj(lana);
        dodaj(uros);

        assertFalse(model.postojiClanOsimReda(lana, 0));
        assertTrue(model.postojiClanOsimReda(lana, 1));
    }

    @Test
    public void ocistiUklanjaSveClanove() {
        dodaj(lana);
        dodaj(uros);

        model.ocisti();

        assertEquals(0, model.getRowCount());
        assertTrue(model.getLista().isEmpty());
    }

    @Test
    public void konstruktorPrihvataPostojecuListuINull() {
        ArrayList<ClanGrupe> postojeci = new ArrayList<>();
        postojeci.add(new ClanGrupe(null, 1, "/", lana));

        assertEquals(1, new TableModelClanoviGrupe(postojeci).getRowCount());
        assertEquals(0, new TableModelClanoviGrupe(null).getRowCount());
    }

    @Test
    public void osvezavanjePraznogModelaNeKontaktiraServer() throws Exception {
        assertTrue(model.osveziSaServera(1L).isEmpty());
    }

    private TableModelClanoviGrupe modelSaServera(LazniServer server, long... idClanova) {
        TableModelClanoviGrupe m = new TableModelClanoviGrupe();
        for (long id : idClanova) {
            m.dodajClana(new ClanGrupe(null, -1, "/", server.clan(id)));
        }
        return m;
    }

    @Test
    public void osvezavanjePreuzimaIzmeneClanovaSaServera() throws Exception {
        LazniServer server = testutil.TestKlijent.server();
        TableModelClanoviGrupe m = modelSaServera(server, 1, 2);
        server.izmeniClana(1, 11, 1);

        ArrayList<String> uklonjeni = m.osveziSaServera(1L);

        assertTrue(uklonjeni.isEmpty());
        assertEquals(11, m.getValueAt(0, 2));
        assertEquals(2, m.getRowCount());
    }

    @Test
    public void osvezavanjeUklanjaObrisaneIClanoveDrugeKategorije() throws Exception {
        LazniServer server = testutil.TestKlijent.server();
        TableModelClanoviGrupe m = modelSaServera(server, 1, 2, 3);
        server.ukloniClana(2);
        server.izmeniClana(1, 9, 2);

        ArrayList<String> uklonjeni = m.osveziSaServera(1L);

        assertEquals(2, uklonjeni.size());
        assertTrue(uklonjeni.contains("Lana Stojanovic"));
        assertTrue(uklonjeni.contains("Uros Milosevic"));
        assertEquals(1, m.getRowCount());
        assertEquals("Petar Vasic", m.getValueAt(0, 1));
        assertEquals(1, m.getValueAt(0, 0));
    }

    @Test
    public void osvezavanjeBezKategorijeNeUklanjaClanove() throws Exception {
        LazniServer server = testutil.TestKlijent.server();
        TableModelClanoviGrupe m = modelSaServera(server, 1);
        server.izmeniClana(1, 9, 2);

        assertTrue(m.osveziSaServera(null).isEmpty());
        assertEquals(1, m.getRowCount());
    }

    @Test(expected = Exception.class)
    public void greskaSaServeraPriOsvezavanjuSeProsledjuje() throws Exception {
        LazniServer server = testutil.TestKlijent.server();
        TableModelClanoviGrupe m = modelSaServera(server, 1);
        server.greska(Operation.GET_ALL_CLAN, "Server ne radi");

        m.osveziSaServera(1L);
    }

    @Test
    public void nepostojecaKolonaVracaNull() {
        dodaj(lana);

        org.junit.Assert.assertNull(model.getValueAt(0, 3));
    }
}
