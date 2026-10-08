package domain;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class AdministratorTest {

    private Administrator coa() {
        return new Administrator(1L, "Aleksandar", "Eic", "coa", "coa123");
    }

    @Test
    public void konstruktorIGeteri() {
        Administrator a = coa();

        assertEquals(Long.valueOf(1), a.getAdministratorID());
        assertEquals("Aleksandar", a.getIme());
        assertEquals("Eic", a.getPrezime());
        assertEquals("coa", a.getUsername());
        assertEquals("coa123", a.getPassword());
        assertEquals("Aleksandar Eic", a.toString());
    }

    @Test
    public void seteri() {
        Administrator a = new Administrator();
        a.setAdministratorID(2L);
        a.setIme("Stefan");
        a.setPrezime("Markovic");
        a.setUsername("stefan");
        a.setPassword("stefan123");

        assertEquals(Long.valueOf(2), a.getAdministratorID());
        assertEquals("Stefan Markovic", a.toString());
        assertEquals("stefan", a.getUsername());
        assertEquals("stefan123", a.getPassword());
    }

    @Test
    public void jednakostSeOdredjujePoId() {
        Administrator a = coa();

        assertTrue(a.equals(a));
        assertTrue(a.equals(new Administrator(1L, "Drugo", "Ime", "x", "y")));
        assertFalse(a.equals(new Administrator(2L, "Aleksandar", "Eic", "coa", "coa123")));
        assertFalse(a.equals(null));
        assertNotEquals(a, "coa");
        assertTrue(new Administrator().equals(new Administrator()));
    }

    @Test
    public void sqlDelovi() {
        Administrator a = coa();

        assertEquals(" administrator ", a.nazivTabele());
        assertEquals(" a ", a.alijas());
        assertEquals("", a.join());
        assertEquals(" (Ime, Prezime, Username, Password) ", a.koloneZaInsert());
        assertEquals(" 'Aleksandar', 'Eic', 'coa', 'coa123' ", a.vrednostiZaInsert());
        assertEquals(" Ime = 'Aleksandar', Prezime = 'Eic', Username = 'coa', Password = 'coa123' ", a.vrednostiZaUpdate());
        assertEquals(" AdministratorID = 1", a.uslov());
        assertEquals(" WHERE LOWER(IME) LIKE '%Aleksandar%' OR LOWER(PREZIME) LIKE '%Eic%' OR LOWER(Username) LIKE '%coa%' ",
                a.dodatniUslov());
        assertEquals(" ORDER BY ADMINISTRATORID ASC ", a.orderBy());
    }
}
