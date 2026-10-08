package domain;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import org.junit.Test;

public class KategorijaTest {

    @Test
    public void konstruktorGeteriISeteri() {
        Kategorija k = new Kategorija(1L, "Pocetni", "Osnovna tehnika.");

        assertEquals(Long.valueOf(1), k.getKategorijaID());
        assertEquals("Pocetni", k.getNaziv());
        assertEquals("Osnovna tehnika.", k.getOpis());
        assertEquals("Pocetni", k.toString());

        Kategorija prazna = new Kategorija();
        assertNull(prazna.getKategorijaID());
        prazna.setKategorijaID(2L);
        prazna.setNaziv("Srednji");
        prazna.setOpis("Opis");
        assertEquals(Long.valueOf(2), prazna.getKategorijaID());
        assertEquals("Srednji", prazna.toString());
        assertEquals("Opis", prazna.getOpis());
    }

    @Test
    public void sqlDelovi() {
        Kategorija k = new Kategorija(1L, "Pocetni", "Osnovna tehnika.");

        assertEquals(" Kategorija ", k.nazivTabele());
        assertEquals(" k ", k.alijas());
        assertEquals("", k.join());
        assertEquals(" (naziv, opis) ", k.koloneZaInsert());
        assertEquals("'Pocetni', 'Osnovna tehnika.' ", k.vrednostiZaInsert());
        assertEquals(" naziv = 'Pocetni', opis = 'Osnovna tehnika.' ", k.vrednostiZaUpdate());
        assertEquals(" KategorijaID = 1", k.uslov());
        assertEquals(" WHERE LOWER(NAZIV) LIKE '%Pocetni%' ", k.dodatniUslov());
        assertEquals(" ORDER BY KATEGORIJAID ASC ", k.orderBy());
    }
}
