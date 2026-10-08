package mail;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import org.junit.After;
import org.junit.Test;
import testutil.LazniSmtpServer;
import testutil.LazniSmtpServer.Poruka;
import testutil.LazniSmtpServer.Rezim;
import testutil.TestMail;

public class SmtpKlijentTest {

    private LazniSmtpServer smtp;

    @After
    public void zavrsi() {
        if (smtp != null) {
            smtp.close();
        }
        TestMail.vrati();
    }

    private SmtpKlijent klijent(Rezim rezim, String korisnik) throws Exception {
        smtp = new LazniSmtpServer(rezim);
        TestMail.podesi(true, korisnik, smtp.getPort(), rezim == Rezim.STARTTLS, rezim == Rezim.SSL);
        if (rezim == Rezim.STARTTLS || rezim == Rezim.SSL) {
            TestMail.veruj(LazniSmtpServer.sslKontekst());
        }
        return new SmtpKlijent(MailKonfiguracija.ucitaj());
    }

    private static String dekodirajZaglavlje(String vrednost) {
        String b64 = vrednost.substring("=?UTF-8?B?".length(), vrednost.indexOf("?=", 10));
        return new String(Base64.getDecoder().decode(b64), StandardCharsets.UTF_8);
    }

    @Test
    public void saljePorukuSaPrilogomIUtf8Zaglavljima() throws Exception {
        byte[] prilog = "%PDF-1.4 sadrzaj".getBytes(StandardCharsets.ISO_8859_1);
        try (SmtpKlijent k = klijent(Rezim.OBICAN, TestMail.POSILJALAC)) {
            k.otvori();
            k.posalji("lana@test.rs", "Lana Stojanović", "Grupa „Mini Tenis A“ je kreirana",
                    "Poštovana Lana,\nčlanovi grupe su u prilogu.", "Grupa.pdf", prilog, "application/pdf");
        }

        assertEquals(1, smtp.getPoruke().size());
        Poruka p = smtp.getPoruke().get(0);
        assertEquals(TestMail.POSILJALAC, p.getPosiljalac());
        assertEquals("lana@test.rs", p.getPrimaoci().get(0));
        assertEquals("Grupa „Mini Tenis A“ je kreirana", dekodirajZaglavlje(p.getZaglavlje("Subject")));
        assertTrue(p.getZaglavlje("To").endsWith("<lana@test.rs>"));
        assertEquals("Lana Stojanović", dekodirajZaglavlje(p.getZaglavlje("To")));
        assertEquals("Teniski klub", dekodirajZaglavlje(p.getZaglavlje("From")));
        assertEquals("Poštovana Lana,\nčlanovi grupe su u prilogu.", p.getTekst());
        assertArrayEquals(prilog, p.getPrilog());
        assertTrue(p.getSadrzaj().contains("filename=\"Grupa.pdf\""));
        assertEquals(TestMail.POSILJALAC, smtp.getPrijavljeniKorisnik());
        assertEquals(TestMail.LOZINKA, smtp.getPrimljenaLozinka());
        assertTrue(smtp.getKomande().contains("QUIT"));
    }

    @Test
    public void saljePorukuBezPriloga() throws Exception {
        try (SmtpKlijent k = klijent(Rezim.OBICAN, TestMail.POSILJALAC)) {
            k.otvori();
            k.posalji("lana@test.rs", "", "Naslov", "Tekst", null, null, null);
        }

        Poruka p = smtp.getPoruke().get(0);
        assertFalse(p.imaPrilog());
        assertEquals("<lana@test.rs>", p.getZaglavlje("To"));
        assertEquals("Tekst", p.getTekst());
    }

    @Test
    public void bezKorisnickogImenaNePrijavljujeNalog() throws Exception {
        try (SmtpKlijent k = klijent(Rezim.OBICAN, "")) {
            k.otvori();
        }

        assertNull(smtp.getPrijavljeniKorisnik());
        assertFalse(smtp.getKomande().contains("AUTH LOGIN"));
    }

    @Test
    public void pogresnaLozinkaSePrijavljujeBezOtkrivanjaLozinke() throws Exception {
        SmtpKlijent k = klijent(Rezim.OBICAN, TestMail.POSILJALAC);
        smtp.odbijPrijavu();

        IOException ex = assertThrows(IOException.class, k::otvori);
        k.close();

        assertTrue(ex.getMessage(), ex.getMessage().startsWith("SMTP komanda '(lozinka)' nije uspela: 535"));
        assertFalse(ex.getMessage().contains(TestMail.LOZINKA));
    }

    @Test
    public void odbijenPrimalacNePrekidaVezu() throws Exception {
        try (SmtpKlijent k = klijent(Rezim.OBICAN, TestMail.POSILJALAC)) {
            smtp.odbijPrimaoca("nema@test.rs");
            k.otvori();

            IOException ex = assertThrows(IOException.class,
                    () -> k.posalji("nema@test.rs", "Nema", "Naslov", "Tekst", null, null, null));
            assertTrue(ex.getMessage().contains("550"));

            k.reset();
            k.posalji("lana@test.rs", "Lana", "Naslov", "Tekst", null, null, null);
        }

        assertEquals(1, smtp.getPoruke().size());
        assertEquals("lana@test.rs", smtp.getPoruke().get(0).getPrimaoci().get(0));
        assertTrue(smtp.getKomande().contains("RSET"));
    }

    @Test
    public void koristiStarttlsZaSifrovanuVezu() throws Exception {
        try (SmtpKlijent k = klijent(Rezim.STARTTLS, TestMail.POSILJALAC)) {
            k.otvori();
            k.posalji("lana@test.rs", "Lana", "Naslov", "Tekst", null, null, null);
        }

        assertTrue(smtp.isKoriscenTls());
        assertTrue(smtp.getKomande().contains("STARTTLS"));
        assertEquals(1, smtp.getPoruke().size());
        assertEquals(TestMail.POSILJALAC, smtp.getPrijavljeniKorisnik());
    }

    @Test
    public void koristiSslOdPocetkaVeze() throws Exception {
        try (SmtpKlijent k = klijent(Rezim.SSL, TestMail.POSILJALAC)) {
            k.otvori();
            k.posalji("lana@test.rs", "Lana", "Naslov", "Tekst", null, null, null);
        }

        assertTrue(smtp.isKoriscenTls());
        assertFalse(smtp.getKomande().contains("STARTTLS"));
        assertEquals(1, smtp.getPoruke().size());
    }

    @Test
    public void prekidVezeSePrijavljujeKaoGreska() throws Exception {
        SmtpKlijent k = klijent(Rezim.PREKID_ODMAH, TestMail.POSILJALAC);

        IOException ex = assertThrows(IOException.class, k::otvori);
        k.reset();
        k.close();

        assertEquals("server je prekinuo vezu", ex.getMessage());
    }

    @Test
    public void zatvaranjeBezOtvaranjaNijeGreska() throws Exception {
        SmtpKlijent k = klijent(Rezim.OBICAN, TestMail.POSILJALAC);

        k.close();

        assertTrue(smtp.getKomande().isEmpty());
    }

    @Test
    public void primalacBezImenaIDvostrukoZatvaranje() throws Exception {
        SmtpKlijent k = klijent(Rezim.OBICAN, TestMail.POSILJALAC);
        k.otvori();
        k.posalji("lana@test.rs", null, "Naslov", "Tekst", null, null, null);
        k.close();
        k.close();

        assertEquals("<lana@test.rs>", smtp.getPoruke().get(0).getZaglavlje("To"));
    }
}
