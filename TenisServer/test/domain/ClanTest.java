package domain;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import org.junit.Test;

public class ClanTest {

    private final Kategorija pocetni = new Kategorija(1L, "Pocetni", "Opis");

    private Clan lana() {
        return new Clan(1L, "Lana", "Stojanovic", 9, "lana@test.rs", "0641112222", pocetni);
    }

    @Test
    public void konstruktorGeteriISeteri() {
        Clan c = lana();

        assertEquals(Long.valueOf(1), c.getClanID());
        assertEquals("Lana", c.getIme());
        assertEquals("Stojanovic", c.getPrezime());
        assertEquals(9, c.getGodine());
        assertEquals("lana@test.rs", c.getEmail());
        assertEquals("0641112222", c.getTelefon());
        assertSame(pocetni, c.getKategorija());
        assertEquals("Lana Stojanovic (Godine: 9)", c.toString());

        Clan nov = new Clan();
        nov.setClanID(2L);
        nov.setIme("Uros");
        nov.setPrezime("Milosevic");
        nov.setGodine(10);
        nov.setEmail("uros@test.rs");
        nov.setTelefon("0643334444");
        nov.setKategorija(pocetni);
        assertEquals(Long.valueOf(2), nov.getClanID());
        assertEquals("Uros Milosevic (Godine: 10)", nov.toString());
        assertEquals("uros@test.rs", nov.getEmail());
        assertEquals("0643334444", nov.getTelefon());
        assertSame(pocetni, nov.getKategorija());
    }

    @Test
    public void sqlDelovi() {
        Clan c = lana();

        assertEquals(" Clan ", c.nazivTabele());
        assertEquals(" c ", c.alijas());
        assertEquals(" JOIN KATEGORIJA K ON (K.KATEGORIJAID = C.KATEGORIJAID) ", c.join());
        assertEquals(" (Ime, Prezime, godine, email, telefon, kategorijaID) ", c.koloneZaInsert());
        assertEquals("'Lana', 'Stojanovic',  9, 'lana@test.rs', '0641112222', 1", c.vrednostiZaInsert());
        assertEquals(" godine = 9, email = 'lana@test.rs', telefon = '0641112222', kategorijaID = 1 ", c.vrednostiZaUpdate());
        assertEquals(" clanID = 1", c.uslov());
        assertEquals(" ORDER BY CLANID ASC ", c.orderBy());
    }

    @Test
    public void pretragaBezKategorije() {
        Clan kriterijum = new Clan(null, "ana", "ana", 0, "ana", "", null);

        assertEquals(" WHERE LOWER(IME) LIKE '%ana%' OR LOWER(PREZIME) LIKE '%ana%' OR LOWER(EMAIL) LIKE '%ana%' ",
                kriterijum.dodatniUslov());
    }

    @Test
    public void pretragaSaKategorijom() {
        Clan kriterijum = new Clan(null, "ana", "ana", 0, "ana", "", new Kategorija(2L, "Srednji", ""));

        assertEquals(" WHERE (LOWER(IME) LIKE '%ana%' OR LOWER(PREZIME) LIKE '%ana%' OR LOWER(EMAIL) LIKE '%ana%') "
                + "AND K.KATEGORIJAID = 2", kriterijum.dodatniUslov());
    }
}
