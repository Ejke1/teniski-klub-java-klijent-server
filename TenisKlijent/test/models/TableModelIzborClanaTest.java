package models;

import domain.Clan;
import domain.Kategorija;
import java.util.ArrayList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import org.junit.Before;
import org.junit.Test;

public class TableModelIzborClanaTest {

    private TableModelIzborClana model;
    private ArrayList<Clan> clanovi;

    @Before
    public void pripremi() {
        model = new TableModelIzborClana();
        Kategorija pocetni = new Kategorija(1L, "Pocetni", "Opis");
        clanovi = new ArrayList<>();
        clanovi.add(new Clan(1L, "Lana", "Stojanovic", 9, "lana@test.rs", "0641112222", pocetni));
        clanovi.add(new Clan(2L, "Uros", "Milosevic", 10, "uros@test.rs", "0643334444", pocetni));
    }

    @Test
    public void imaKoloneZaIzborClana() {
        String[] ocekivane = {"ID", "Ime", "Prezime", "Godine", "Email", "Telefon"};

        assertEquals(ocekivane.length, model.getColumnCount());
        for (int i = 0; i < ocekivane.length; i++) {
            assertEquals(ocekivane[i], model.getColumnName(i));
        }
    }

    @Test
    public void prikazujePodatkeIzabranogClana() {
        model.setLista(clanovi);

        assertEquals(2, model.getRowCount());
        assertEquals(2L, model.getValueAt(1, 0));
        assertEquals("Uros", model.getValueAt(1, 1));
        assertEquals("Milosevic", model.getValueAt(1, 2));
        assertEquals(10, model.getValueAt(1, 3));
        assertEquals("uros@test.rs", model.getValueAt(1, 4));
        assertEquals("0643334444", model.getValueAt(1, 5));
    }

    @Test
    public void vracaClanaIzOznacenogReda() {
        model.setLista(clanovi);

        assertSame(clanovi.get(0), model.getClan(0));
    }

    @Test
    public void nullListaDajePraznuTabelu() {
        model.setLista(clanovi);
        model.setLista(null);

        assertEquals(0, model.getRowCount());
    }

    @Test
    public void nepostojecaKolonaVracaNull() {
        model.setLista(clanovi);

        org.junit.Assert.assertNull(model.getValueAt(0, 6));
        assertSame(clanovi, model.getLista());
    }
}
