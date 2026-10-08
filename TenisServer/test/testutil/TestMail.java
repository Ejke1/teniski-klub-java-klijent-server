package testutil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import javax.net.ssl.SSLContext;

public final class TestMail {

    public static final String POSILJALAC = "lazni.posiljalac@test.rs";
    public static final String LOZINKA = "test-lozinka";

    private static String prethodnaKonfiguracija;
    private static SSLContext prethodniSsl;
    private static File fajl;

    private TestMail() {
    }

    public static void podesi(boolean ukljuceno, String korisnik, int port) throws IOException {
        podesi(ukljuceno, korisnik, port, false, false);
    }

    public static void podesi(boolean ukljuceno, String korisnik, int port, boolean starttls, boolean ssl)
            throws IOException {
        if (fajl == null) {
            prethodnaKonfiguracija = System.getProperty("mailconfig");
            fajl = File.createTempFile("mailconfig-test-", ".properties");
            fajl.deleteOnExit();
        }
        if (System.getProperty("maillog") == null) {
            File log = File.createTempFile("mail-test-", ".log");
            log.deleteOnExit();
            System.setProperty("maillog", log.getPath());
        }
        try (Writer w = new OutputStreamWriter(new FileOutputStream(fajl), StandardCharsets.UTF_8)) {
            w.write("mail.enabled=" + ukljuceno + "\n"
                    + "mail.smtp.host=127.0.0.1\n"
                    + "mail.smtp.port=" + port + "\n"
                    + "mail.smtp.starttls=" + starttls + "\n"
                    + "mail.smtp.ssl=" + ssl + "\n"
                    + "mail.username=" + korisnik + "\n"
                    + "mail.password=" + LOZINKA + "\n"
                    + "mail.from.name=Teniski klub\n");
        }
        System.setProperty("mailconfig", fajl.getPath());
    }

    public static void veruj(SSLContext kontekst) throws Exception {
        if (prethodniSsl == null) {
            prethodniSsl = SSLContext.getDefault();
        }
        SSLContext.setDefault(kontekst);
    }

    public static void vrati() {
        if (prethodniSsl != null) {
            SSLContext.setDefault(prethodniSsl);
            prethodniSsl = null;
        }
        if (fajl == null) {
            return;
        }
        if (prethodnaKonfiguracija == null) {
            System.clearProperty("mailconfig");
        } else {
            System.setProperty("mailconfig", prethodnaKonfiguracija);
        }
        fajl.delete();
        fajl = null;
    }
}
