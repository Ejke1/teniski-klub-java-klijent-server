package mail;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class MailKonfiguracijaTest {

    private String prethodna;
    private File fajl;

    @Before
    public void pripremi() throws IOException {
        prethodna = System.getProperty("mailconfig");
        fajl = File.createTempFile("mailconfig-", ".properties");
        System.setProperty("mailconfig", fajl.getPath());
    }

    @After
    public void vrati() {
        if (prethodna == null) {
            System.clearProperty("mailconfig");
        } else {
            System.setProperty("mailconfig", prethodna);
        }
        fajl.delete();
    }

    private void upisi(String sadrzaj) throws IOException {
        try (Writer w = new OutputStreamWriter(new FileOutputStream(fajl), StandardCharsets.UTF_8)) {
            w.write(sadrzaj);
        }
    }

    @Test
    public void citaSvaPodesavanja() throws IOException {
        upisi("mail.enabled=true\n"
                + "mail.smtp.host=smtp.test.rs\n"
                + "mail.smtp.port=465\n"
                + "mail.username=  klub@test.rs  \n"
                + "mail.password=abcd efgh ijkl mnop\n"
                + "mail.from.name=Teniski klub\n");

        MailKonfiguracija k = MailKonfiguracija.ucitaj();

        assertTrue(k.isUkljuceno());
        assertEquals("smtp.test.rs", k.getHost());
        assertEquals(465, k.getPort());
        assertEquals("klub@test.rs", k.getKorisnik());
        assertEquals("abcdefghijklmnop", k.getLozinka());
        assertEquals("Teniski klub", k.getImePosiljaoca());
        assertTrue(k.isPodesen());
    }

    @Test
    public void starttlsZavisiOdPorta() throws IOException {
        upisi("mail.smtp.port=587\n");
        assertTrue(MailKonfiguracija.ucitaj().isStarttls());

        upisi("mail.smtp.port=465\n");
        assertFalse(MailKonfiguracija.ucitaj().isStarttls());

        upisi("mail.smtp.port=25\n");
        assertFalse(MailKonfiguracija.ucitaj().isStarttls());

        upisi("mail.smtp.port=587\nmail.smtp.starttls=false\n");
        assertFalse(MailKonfiguracija.ucitaj().isStarttls());
    }

    @Test
    public void nijePodesenaBezKorisnikaILozinke() throws IOException {
        upisi("mail.enabled=true\nmail.username=klub@test.rs\nmail.password=\n");
        assertFalse(MailKonfiguracija.ucitaj().isPodesen());

        upisi("mail.enabled=true\nmail.username=\nmail.password=tajna\n");
        assertFalse(MailKonfiguracija.ucitaj().isPodesen());
    }

    @Test
    public void citaIskljucenoSlanje() throws IOException {
        upisi("mail.enabled=false\n");
        assertFalse(MailKonfiguracija.ucitaj().isUkljuceno());
    }

    @Test
    public void praviSablonAkoFajlNePostoji() throws IOException {
        assertTrue(fajl.delete());

        MailKonfiguracija k = MailKonfiguracija.ucitaj();

        assertTrue(fajl.exists());
        assertTrue(k.isUkljuceno());
        assertEquals("smtp.gmail.com", k.getHost());
        assertEquals(587, k.getPort());
        assertFalse(k.isPodesen());
    }

    @Test
    public void sslZavisiOdPorta() throws IOException {
        upisi("mail.smtp.port=465\n");
        assertTrue(MailKonfiguracija.ucitaj().isSsl());

        upisi("mail.smtp.port=587\n");
        assertFalse(MailKonfiguracija.ucitaj().isSsl());

        upisi("mail.smtp.port=2525\nmail.smtp.ssl=true\n");
        assertTrue(MailKonfiguracija.ucitaj().isSsl());
    }

    @Test
    public void putanjaSeCitaIzPodesavanja() {
        assertEquals(fajl.getPath(), MailKonfiguracija.putanja());

        System.clearProperty("mailconfig");
        assertEquals("mailconfig.properties", MailKonfiguracija.putanja());
        System.setProperty("mailconfig", fajl.getPath());
    }
}
