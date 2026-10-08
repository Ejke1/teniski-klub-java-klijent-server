package mail;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class MailLogTest {

    private String prethodni;
    private File log;

    @Before
    public void pripremi() throws Exception {
        prethodni = System.getProperty("maillog");
        log = File.createTempFile("mail-log-", ".log");
        assertTrue(log.delete());
        System.setProperty("maillog", log.getPath());
    }

    @After
    public void vrati() {
        if (prethodni == null) {
            System.clearProperty("maillog");
        } else {
            System.setProperty("maillog", prethodni);
        }
        log.delete();
    }

    @Test
    public void upisujeRedSaVremenomINiti() throws Exception {
        MailLog.upisi("Prva poruka - Đorđe");

        List<String> redovi = Files.readAllLines(log.toPath(), StandardCharsets.UTF_8);
        assertEquals(1, redovi.size());
        assertTrue(redovi.get(0), redovi.get(0).matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}\\.\\d{3} \\[.+\\] Prva poruka - Đorđe"));
        assertTrue(redovi.get(0).contains("[" + Thread.currentThread().getName() + "]"));
    }

    @Test
    public void dopisujeNaKrajPostojecegDnevnika() throws Exception {
        MailLog.upisi("jedan");
        MailLog.upisi("dva");

        List<String> redovi = Files.readAllLines(log.toPath(), StandardCharsets.UTF_8);
        assertEquals(2, redovi.size());
        assertTrue(redovi.get(0).endsWith("jedan"));
        assertTrue(redovi.get(1).endsWith("dva"));
    }

    @Test
    public void greskaPriUpisuNeObaraProgram() throws Exception {
        File folder = Files.createTempDirectory("mail-log-folder").toFile();
        System.setProperty("maillog", folder.getPath());
        try {
            MailLog.upisi("ne moze da se upise u folder");
        } finally {
            folder.delete();
        }
    }
}
