package mail;

import domain.Grupa;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import testutil.LazniSmtpServer;
import testutil.LazniSmtpServer.Poruka;
import testutil.TestBaza;
import testutil.TestMail;

public class ObavestenjeGrupeTest {

    private LazniSmtpServer smtp;

    @Before
    public void pokreniLazniServer() throws Exception {
        smtp = new LazniSmtpServer();
        TestMail.podesi(true, TestMail.POSILJALAC, smtp.getPort());
    }

    @After
    public void zaustaviLazniServer() {
        smtp.close();
        TestMail.vrati();
    }

    @Test
    public void saljeMailSaPdfomSvimClanovimaGrupe() {
        String izvestaj = ObavestenjeGrupe.posalji(TestBaza.grupa(1), true);

        assertEquals("Mail sa PDF spiskom clanova je poslat na 3 od 3 clanova grupe.", izvestaj);
        List<Poruka> poruke = smtp.getPoruke();
        assertEquals(3, poruke.size());

        Set<String> primaoci = new HashSet<>();
        for (Poruka p : poruke) {
            primaoci.addAll(p.getPrimaoci());
            assertEquals(TestMail.POSILJALAC, p.getPosiljalac());
            assertTrue(new String(p.getPrilog(), StandardCharsets.ISO_8859_1).startsWith("%PDF-"));
            assertTrue(p.getSadrzaj().contains("filename=\"Grupa_Mini_Tenis_A.pdf\""));
            assertTrue(p.getTekst().contains("Mini Tenis A"));
            assertTrue(p.getTekst().contains("kreirana"));
        }
        assertEquals(new HashSet<>(Arrays.asList(
                "lana.stojanovic@test.rs", "uros.milosevic@test.rs", "petar.vasic@test.rs")), primaoci);
        assertEquals(TestMail.POSILJALAC, smtp.getPrijavljeniKorisnik());
    }

    @Test
    public void obavestavaClanoveOIzmeniGrupe() {
        String izvestaj = ObavestenjeGrupe.posalji(TestBaza.grupa(2), false);

        assertEquals("Mail sa PDF spiskom clanova je poslat na 2 od 2 clanova grupe.", izvestaj);
        for (Poruka p : smtp.getPoruke()) {
            assertTrue(p.getTekst().contains("izmenjena"));
        }
    }

    @Test
    public void izvestavaKojemClanuMailNijePoslat() {
        smtp.odbijPrimaoca("uros.milosevic@test.rs");

        String izvestaj = ObavestenjeGrupe.posalji(TestBaza.grupa(1), true);

        assertTrue(izvestaj, izvestaj.startsWith("Mail sa PDF spiskom clanova je poslat na 2 od 3 clanova grupe."));
        assertTrue(izvestaj, izvestaj.contains("Nije poslato: uros.milosevic@test.rs"));
        assertEquals(2, smtp.getPoruke().size());
    }

    @Test
    public void preskaceClanaBezEmailAdrese() {
        Grupa g = TestBaza.grupa(1);
        g.getClanoviGrupe().get(2).getClan().setEmail("");

        String izvestaj = ObavestenjeGrupe.posalji(g, true);

        assertTrue(izvestaj, izvestaj.startsWith("Mail sa PDF spiskom clanova je poslat na 2 od 3 clanova grupe."));
        assertTrue(izvestaj, izvestaj.contains("Petar Vasic (nema email)"));
    }

    @Test
    public void neSaljeKadaJeSlanjeIskljuceno() throws Exception {
        TestMail.podesi(false, TestMail.POSILJALAC, smtp.getPort());

        String izvestaj = ObavestenjeGrupe.posalji(TestBaza.grupa(1), true);

        assertEquals("Slanje maila je iskljuceno (mail.enabled=false u mailconfig.properties).", izvestaj);
        assertEquals(0, smtp.getPoruke().size());
    }

    @Test
    public void neSaljeKadaNalogNijePodesen() throws Exception {
        TestMail.podesi(true, "", smtp.getPort());

        String izvestaj = ObavestenjeGrupe.posalji(TestBaza.grupa(1), true);

        assertTrue(izvestaj, izvestaj.contains("nisu uneti mail.username i mail.password"));
        assertEquals(0, smtp.getPoruke().size());
    }

    @Test
    public void prijavljujeGreskuKadaMailServerNijeDostupan() {
        smtp.close();

        String izvestaj = ObavestenjeGrupe.posalji(TestBaza.grupa(1), true);

        assertTrue(izvestaj, izvestaj.startsWith("Mail nije poslat:"));
    }

    @Test
    public void grupaBezClanovaNeSaljeNista() {
        Grupa g = TestBaza.grupa(1);
        g.setClanoviGrupe(null);

        String izvestaj = ObavestenjeGrupe.posalji(g, true);

        assertEquals("Mail sa PDF spiskom clanova je poslat na 0 od 0 clanova grupe.", izvestaj);
        assertEquals(0, smtp.getPoruke().size());
    }

    @Test
    public void prijavljujeKadaPodesavanjaNeMoguDaSeProcitaju() throws Exception {
        File folder = Files.createTempDirectory("mailconfig-folder").toFile();
        String prethodna = System.getProperty("mailconfig");
        System.setProperty("mailconfig", folder.getPath());
        try {
            String izvestaj = ObavestenjeGrupe.posalji(TestBaza.grupa(1), true);

            assertTrue(izvestaj, izvestaj.startsWith("Mail nije poslat: ne mogu da procitam mailconfig.properties"));
        } finally {
            System.setProperty("mailconfig", prethodna);
            folder.delete();
        }
    }

    @Test
    public void neocekivanaGreskaSePrijavljuje() {
        Grupa g = TestBaza.grupa(1);
        g.setNaziv(null);

        String izvestaj = ObavestenjeGrupe.posalji(g, true);

        assertTrue(izvestaj, izvestaj.startsWith("Mail nije poslat:"));
        assertEquals(0, smtp.getPoruke().size());
    }

    @Test
    public void sporoSlanjeSeNastavljaUPozadini() {
        long prethodno = ObavestenjeGrupe.maxCekanjeSekundi;
        ObavestenjeGrupe.maxCekanjeSekundi = 1;
        smtp.kasniSaPozdravom(2500);
        try {
            String izvestaj = ObavestenjeGrupe.posalji(TestBaza.grupa(1), true);

            assertTrue(izvestaj, izvestaj.startsWith("Slanje maila clanovima je u toku"));
            long kraj = System.currentTimeMillis() + 15000;
            while (smtp.getPoruke().size() < 3 && System.currentTimeMillis() < kraj) {
                testutil.Gui.spavaj(50);
            }
            assertEquals(3, smtp.getPoruke().size());
        } finally {
            ObavestenjeGrupe.maxCekanjeSekundi = prethodno;
        }
    }

    @Test
    public void tekstPorukeKadaNedostajuPodaciOGrupi() {
        Grupa g = TestBaza.grupa(1);
        g.setTrener(null);
        g.setKategorija(null);
        g.getClanoviGrupe().get(1).getClan().setEmail(null);

        String izvestaj = ObavestenjeGrupe.posalji(g, true);

        assertTrue(izvestaj, izvestaj.startsWith("Mail sa PDF spiskom clanova je poslat na 2 od 3 clanova grupe."));
        assertTrue(izvestaj, izvestaj.contains("Uros Milosevic (nema email)"));
        String tekst = smtp.getPoruke().get(0).getTekst();
        assertTrue(tekst, tekst.contains("Kategorija: -"));
        assertTrue(tekst, tekst.contains("Trener: -"));
        assertTrue(tekst, tekst.contains("Broj članova: 3"));
    }
}
