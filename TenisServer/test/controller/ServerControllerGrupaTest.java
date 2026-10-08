package controller;

import domain.Grupa;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import testutil.LazniSmtpServer;
import testutil.TestBaza;
import testutil.TestMail;

public class ServerControllerGrupaTest {

    private LazniSmtpServer smtp;

    @Before
    public void pripremi() throws Exception {
        TestBaza.pripremi();
        smtp = new LazniSmtpServer();
        TestMail.podesi(true, TestMail.POSILJALAC, smtp.getPort());
    }

    @After
    public void zavrsi() {
        if (smtp != null) {
            smtp.close();
        }
        TestMail.vrati();
    }

    @Test
    public void kreiranjeGrupeCuvaGrupuISaljeMailSvimClanovima() throws Exception {
        Grupa g = TestBaza.novaGrupa("Test mail", 1, 2);

        String izvestaj = ServerController.getInstance().addGrupa(g);

        assertEquals("Mail sa PDF spiskom clanova je poslat na 2 od 2 clanova grupe.", izvestaj);
        assertEquals(1, TestBaza.broj("SELECT COUNT(*) FROM Grupa WHERE Naziv = 'Test mail'"));
        assertEquals(2, smtp.getPoruke().size());
    }

    @Test
    public void grupaJeSacuvanaIKadaMailNeMozeDaSePosalje() throws Exception {
        smtp.close();
        Grupa g = TestBaza.novaGrupa("Test mail", 1, 2);

        String izvestaj = ServerController.getInstance().addGrupa(g);

        assertTrue(izvestaj, izvestaj.startsWith("Mail nije poslat:"));
        assertEquals(1, TestBaza.broj("SELECT COUNT(*) FROM Grupa WHERE Naziv = 'Test mail'"));
        assertEquals(2, TestBaza.broj("SELECT COUNT(*) FROM ClanGrupe WHERE GrupaID = " + g.getGrupaID()));
    }

    @Test
    public void izmenaGrupeSaljeMailSvimClanovima() throws Exception {
        Grupa g = TestBaza.grupa(1);
        g.setOpis("Termini su pomereni.");

        String izvestaj = ServerController.getInstance().updateGrupa(g);

        assertEquals("Mail sa PDF spiskom clanova je poslat na 3 od 3 clanova grupe.", izvestaj);
        assertEquals("Termini su pomereni.", TestBaza.vrednost("SELECT Opis FROM Grupa WHERE GrupaID = 1"));
        assertEquals(3, smtp.getPoruke().size());
    }

    @Test
    public void neispravnaGrupaSeNeCuvaIMailSeNeSalje() throws Exception {
        Grupa g = TestBaza.novaGrupa("Test mail", 1);

        Exception ex = assertThrows(Exception.class, () -> ServerController.getInstance().addGrupa(g));

        assertEquals("Morate uneti barem 2 clana!", ex.getMessage());
        assertEquals(0, TestBaza.broj("SELECT COUNT(*) FROM Grupa WHERE Naziv = 'Test mail'"));
        assertEquals(0, smtp.getPoruke().size());
    }
}
