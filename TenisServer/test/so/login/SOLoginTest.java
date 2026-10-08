package so.login;

import controller.ServerController;
import domain.Administrator;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import testutil.TestBaza;

public class SOLoginTest {

    @Before
    public void pripremi() throws Exception {
        TestBaza.pripremi();
        ServerController.getInstance().getUlogovaniAdministratori().clear();
    }

    @After
    public void odjaviSve() {
        ServerController.getInstance().getUlogovaniAdministratori().clear();
    }

    private Administrator prijava(String korisnickoIme, String lozinka) {
        Administrator a = new Administrator();
        a.setUsername(korisnickoIme);
        a.setPassword(lozinka);
        return a;
    }

    private Administrator prijaviSe(String korisnickoIme, String lozinka) throws Exception {
        SOLogin so = new SOLogin();
        so.templateExecute(prijava(korisnickoIme, lozinka));
        return so.getUlogovani();
    }

    @Test
    public void prijavljujeAdministratoraSaIspravnimPodacima() throws Exception {
        Administrator a = prijaviSe("coa", "coa123");

        assertEquals(Long.valueOf(1), a.getAdministratorID());
        assertEquals("Aleksandar", a.getIme());
        assertEquals(1, ServerController.getInstance().getUlogovaniAdministratori().size());
    }

    @Test
    public void odbijaPogresnuLozinku() {
        Exception ex = assertThrows(Exception.class, () -> prijaviSe("coa", "pogresna"));

        assertEquals("Ne postoji administrator sa tim kredencijalima.", ex.getMessage());
        assertEquals(0, ServerController.getInstance().getUlogovaniAdministratori().size());
    }

    @Test
    public void odbijaNepostojecegKorisnika() {
        Exception ex = assertThrows(Exception.class, () -> prijaviSe("nepostojeci", "123"));

        assertEquals("Ne postoji administrator sa tim kredencijalima.", ex.getMessage());
    }

    @Test
    public void odbijaAdministratoraKojiJeVecPrijavljen() throws Exception {
        prijaviSe("stefan", "stefan123");

        Exception ex = assertThrows(Exception.class, () -> prijaviSe("stefan", "stefan123"));

        assertEquals("Ovaj administrator je vec ulogovan na sistem!", ex.getMessage());
        assertEquals(1, ServerController.getInstance().getUlogovaniAdministratori().size());
    }

    @Test
    public void dozvoljavaNajviseTriPrijavljenaAdministratora() throws Exception {
        prijaviSe("coa", "coa123");
        prijaviSe("stefan", "stefan123");
        prijaviSe("nikola", "nikola123");

        Exception ex = assertThrows(Exception.class, () -> prijaviSe("marko", "marko123"));

        assertEquals("Vec je prijavljeno tri korisnika na sistem!", ex.getMessage());
        assertEquals(3, ServerController.getInstance().getUlogovaniAdministratori().size());
    }

    @Test
    public void korisnickoImeMoraDaSePoklopiUCelosti() {
        Exception ex = assertThrows(Exception.class, () -> prijaviSe("ola", "nikola123"));

        assertEquals("Ne postoji administrator sa tim kredencijalima.", ex.getMessage());
        assertEquals(0, ServerController.getInstance().getUlogovaniAdministratori().size());
    }
}
