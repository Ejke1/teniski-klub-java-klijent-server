package domain;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class TrenerTest {

    @Test
    public void konstruktorGeteriISeteri() {
        Trener t = new Trener(1L, "Ivan", "Ristic", "0601010101", 8);

        assertEquals(Long.valueOf(1), t.getTrenerID());
        assertEquals("Ivan", t.getIme());
        assertEquals("Ristic", t.getPrezime());
        assertEquals("0601010101", t.getBrojTelefona());
        assertEquals(8, t.getGodineIskustva());
        assertEquals("Ivan Ristic", t.toString());

        Trener nov = new Trener();
        nov.setTrenerID(2L);
        nov.setIme("Tamara");
        nov.setPrezime("Vukovic");
        nov.setBrojTelefona("0602020202");
        nov.setGodineIskustva(11);
        assertEquals(Long.valueOf(2), nov.getTrenerID());
        assertEquals("Tamara Vukovic", nov.toString());
        assertEquals("0602020202", nov.getBrojTelefona());
        assertEquals(11, nov.getGodineIskustva());
    }

    @Test
    public void sqlDelovi() {
        Trener t = new Trener(1L, "Ivan", "Ristic", "0601010101", 8);

        assertEquals(" Trener ", t.nazivTabele());
        assertEquals(" t ", t.alijas());
        assertEquals("", t.join());
        assertEquals(" (Ime, Prezime, brojTelefona, godineIskustva) ", t.koloneZaInsert());
        assertEquals("'Ivan', 'Ristic', '0601010101', 8 ", t.vrednostiZaInsert());
        assertEquals(" brojTelefona = '0601010101', godineIskustva = 8 ", t.vrednostiZaUpdate());
        assertEquals(" trenerID = 1", t.uslov());
        assertEquals(" WHERE LOWER(IME) LIKE '%Ivan%' OR LOWER(PREZIME) LIKE '%Ristic%'", t.dodatniUslov());
        assertEquals(" ORDER BY TRENERID ASC ", t.orderBy());
    }
}
