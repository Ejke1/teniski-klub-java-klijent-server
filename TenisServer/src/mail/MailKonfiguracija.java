package mail;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

public class MailKonfiguracija {

    public static final String FAJL = "mailconfig.properties";

    private boolean ukljuceno;
    private String host;
    private int port;
    private String korisnik;
    private String lozinka;
    private String imePosiljaoca;
    private boolean starttls;
    private boolean ssl;

    public static MailKonfiguracija ucitaj() throws IOException {
        File f = new File(putanja());
        if (!f.exists()) {
            napraviPrazan(f);
        }
        Properties p = new Properties();
        try (InputStreamReader r = new InputStreamReader(new FileInputStream(f), StandardCharsets.UTF_8)) {
            p.load(r);
        }
        MailKonfiguracija k = new MailKonfiguracija();
        k.ukljuceno = Boolean.parseBoolean(p.getProperty("mail.enabled", "true").trim());
        k.host = p.getProperty("mail.smtp.host", "smtp.gmail.com").trim();
        k.port = Integer.parseInt(p.getProperty("mail.smtp.port", "587").trim());
        k.korisnik = p.getProperty("mail.username", "").trim();
        k.lozinka = p.getProperty("mail.password", "").replace(" ", "");
        k.imePosiljaoca = p.getProperty("mail.from.name", "Teniski klub").trim();
        k.starttls = Boolean.parseBoolean(p.getProperty("mail.smtp.starttls",
                String.valueOf(k.port != 25 && k.port != 465)).trim());
        k.ssl = Boolean.parseBoolean(p.getProperty("mail.smtp.ssl",
                String.valueOf(k.port == 465)).trim());
        return k;
    }

    public static String putanja() {
        return System.getProperty("mailconfig", FAJL);
    }

    private static void napraviPrazan(File f) throws IOException {
        try (OutputStreamWriter w = new OutputStreamWriter(new FileOutputStream(f), StandardCharsets.UTF_8)) {
            w.write("# Podesavanja za slanje obavestenja clanovima grupe\n"
                    + "mail.enabled=true\n"
                    + "mail.smtp.host=smtp.gmail.com\n"
                    + "mail.smtp.port=587\n"
                    + "mail.username=\n"
                    + "mail.password=\n"
                    + "mail.from.name=Teniski klub\n");
        }
    }

    public boolean isPodesen() {
        return !korisnik.isEmpty() && !lozinka.isEmpty();
    }

    public boolean isUkljuceno() {
        return ukljuceno;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public String getKorisnik() {
        return korisnik;
    }

    public String getLozinka() {
        return lozinka;
    }

    public String getImePosiljaoca() {
        return imePosiljaoca;
    }

    public boolean isStarttls() {
        return starttls;
    }

    public boolean isSsl() {
        return ssl;
    }
}
