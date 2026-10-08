package testutil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManagerFactory;

public class LazniSmtpServer implements AutoCloseable {

    public enum Rezim {
        OBICAN, STARTTLS, SSL, PREKID_ODMAH
    }

    public static class Poruka {

        private final String posiljalac;
        private final List<String> primaoci;
        private final String sadrzaj;

        Poruka(String posiljalac, List<String> primaoci, String sadrzaj) {
            this.posiljalac = posiljalac;
            this.primaoci = primaoci;
            this.sadrzaj = sadrzaj;
        }

        public String getPosiljalac() {
            return posiljalac;
        }

        public List<String> getPrimaoci() {
            return primaoci;
        }

        public String getSadrzaj() {
            return sadrzaj;
        }

        public String getTekst() {
            return new String(deo("Content-Type: text/plain"), StandardCharsets.UTF_8);
        }

        public byte[] getPrilog() {
            return deo("Content-Disposition: attachment");
        }

        public boolean imaPrilog() {
            return sadrzaj.contains("Content-Disposition: attachment");
        }

        public String getZaglavlje(String ime) {
            for (String red : sadrzaj.split("\r\n")) {
                if (red.isEmpty()) {
                    break;
                }
                if (red.startsWith(ime + ": ")) {
                    return red.substring(ime.length() + 2);
                }
            }
            return null;
        }

        private byte[] deo(String zaglavlje) {
            int pocetak = sadrzaj.indexOf(zaglavlje);
            if (pocetak < 0) {
                return new byte[0];
            }
            int telo = sadrzaj.indexOf("\r\n\r\n", pocetak) + 4;
            int kraj = sadrzaj.indexOf("\r\n--", telo);
            return Base64.getMimeDecoder().decode(sadrzaj.substring(telo, kraj));
        }
    }

    private static final String LOZINKA_SKLADISTA = "lozinka123";

    private final ServerSocket server;
    private final Rezim rezim;
    private final List<Poruka> poruke = Collections.synchronizedList(new ArrayList<Poruka>());
    private final List<String> komande = Collections.synchronizedList(new ArrayList<String>());
    private final Set<String> odbijeniPrimaoci = ConcurrentHashMap.newKeySet();
    private volatile String prijavljeniKorisnik;
    private volatile String primljenaLozinka;
    private volatile boolean odbijPrijavu;
    private volatile boolean koriscenTls;
    private volatile long kasnjenjeMs;

    public LazniSmtpServer() throws IOException {
        this(Rezim.OBICAN);
    }

    public LazniSmtpServer(Rezim rezim) throws IOException {
        this.rezim = rezim;
        if (rezim == Rezim.SSL) {
            server = sslKontekst().getServerSocketFactory().createServerSocket(0, 50, InetAddress.getLoopbackAddress());
        } else {
            server = new ServerSocket(0, 50, InetAddress.getLoopbackAddress());
        }
        Thread nit = new Thread(this::prihvataj, "lazni-smtp-server");
        nit.setDaemon(true);
        nit.start();
    }

    public static SSLContext sslKontekst() {
        try (InputStream in = LazniSmtpServer.class.getResourceAsStream("lazni-smtp.p12")) {
            KeyStore skladiste = KeyStore.getInstance("PKCS12");
            skladiste.load(in, LOZINKA_SKLADISTA.toCharArray());
            KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            kmf.init(skladiste, LOZINKA_SKLADISTA.toCharArray());
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(skladiste);
            SSLContext kontekst = SSLContext.getInstance("TLS");
            kontekst.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
            return kontekst;
        } catch (Exception ex) {
            throw new IllegalStateException("Ne mogu da ucitam testni sertifikat", ex);
        }
    }

    public int getPort() {
        return server.getLocalPort();
    }

    public void odbijPrimaoca(String email) {
        odbijeniPrimaoci.add(email.toLowerCase(Locale.ROOT));
    }

    public void odbijPrijavu() {
        odbijPrijavu = true;
    }

    public void kasniSaPozdravom(long ms) {
        kasnjenjeMs = ms;
    }

    public List<Poruka> getPoruke() {
        synchronized (poruke) {
            return new ArrayList<>(poruke);
        }
    }

    public List<String> getKomande() {
        synchronized (komande) {
            return new ArrayList<>(komande);
        }
    }

    public String getPrijavljeniKorisnik() {
        return prijavljeniKorisnik;
    }

    public String getPrimljenaLozinka() {
        return primljenaLozinka;
    }

    public boolean isKoriscenTls() {
        return koriscenTls;
    }

    private void prihvataj() {
        while (!server.isClosed()) {
            try (Socket s = server.accept()) {
                s.setSoTimeout(15000);
                obradi(s);
            } catch (IOException ex) {
                if (server.isClosed()) {
                    return;
                }
            }
        }
    }

    private void obradi(Socket s) throws IOException {
        if (rezim == Rezim.PREKID_ODMAH) {
            return;
        }
        if (rezim == Rezim.SSL) {
            koriscenTls = true;
        }
        if (kasnjenjeMs > 0) {
            Gui.spavaj(kasnjenjeMs);
        }
        BufferedReader in = citac(s);
        OutputStream out = s.getOutputStream();
        odgovori(out, "220 lazni-smtp spreman");
        String posiljalac = null;
        List<String> primaoci = new ArrayList<>();
        String linija;
        while ((linija = in.readLine()) != null) {
            komande.add(linija);
            String komanda = linija.toUpperCase(Locale.ROOT);
            if (komanda.startsWith("EHLO") || komanda.startsWith("HELO")) {
                odgovori(out, rezim == Rezim.STARTTLS && !koriscenTls
                        ? "250-lazni-smtp\r\n250-STARTTLS\r\n250 AUTH LOGIN"
                        : "250-lazni-smtp\r\n250 AUTH LOGIN");
            } else if (komanda.equals("STARTTLS") && rezim == Rezim.STARTTLS) {
                odgovori(out, "220 Pocinjem TLS");
                SSLSocket ssl = (SSLSocket) sslKontekst().getSocketFactory()
                        .createSocket(s, s.getInetAddress().getHostAddress(), s.getPort(), true);
                ssl.setUseClientMode(false);
                ssl.startHandshake();
                koriscenTls = true;
                in = citac(ssl);
                out = ssl.getOutputStream();
            } else if (komanda.equals("AUTH LOGIN")) {
                odgovori(out, "334 VXNlcm5hbWU6");
                prijavljeniKorisnik = new String(Base64.getDecoder().decode(in.readLine()), StandardCharsets.UTF_8);
                odgovori(out, "334 UGFzc3dvcmQ6");
                primljenaLozinka = new String(Base64.getDecoder().decode(in.readLine()), StandardCharsets.UTF_8);
                odgovori(out, odbijPrijavu ? "535 5.7.8 Pogresno korisnicko ime ili lozinka" : "235 2.7.0 Prijava uspesna");
            } else if (komanda.startsWith("MAIL FROM:")) {
                posiljalac = adresa(linija);
                primaoci.clear();
                odgovori(out, "250 OK");
            } else if (komanda.startsWith("RCPT TO:")) {
                String primalac = adresa(linija);
                if (odbijeniPrimaoci.contains(primalac.toLowerCase(Locale.ROOT))) {
                    odgovori(out, "550 5.1.1 Adresa ne postoji");
                } else {
                    primaoci.add(primalac);
                    odgovori(out, "250 OK");
                }
            } else if (komanda.equals("DATA")) {
                odgovori(out, "354 Posaljite sadrzaj");
                StringBuilder sadrzaj = new StringBuilder();
                String red;
                while ((red = in.readLine()) != null && !red.equals(".")) {
                    sadrzaj.append(red.startsWith("..") ? red.substring(1) : red).append("\r\n");
                }
                poruke.add(new Poruka(posiljalac, new ArrayList<>(primaoci), sadrzaj.toString()));
                odgovori(out, "250 OK poruka primljena");
            } else if (komanda.equals("RSET")) {
                posiljalac = null;
                primaoci.clear();
                odgovori(out, "250 OK");
            } else if (komanda.equals("QUIT")) {
                odgovori(out, "221 Dovidjenja");
                return;
            } else {
                odgovori(out, "502 Komanda nije podrzana");
            }
        }
    }

    private static BufferedReader citac(Socket s) throws IOException {
        return new BufferedReader(new InputStreamReader(s.getInputStream(), StandardCharsets.US_ASCII));
    }

    private static String adresa(String linija) {
        int a = linija.indexOf('<');
        int b = linija.indexOf('>');
        return a >= 0 && b > a ? linija.substring(a + 1, b) : linija.substring(linija.indexOf(':') + 1).trim();
    }

    private static void odgovori(OutputStream out, String tekst) throws IOException {
        out.write((tekst + "\r\n").getBytes(StandardCharsets.US_ASCII));
        out.flush();
    }

    @Override
    public void close() {
        try {
            server.close();
        } catch (IOException ignore) {
        }
    }
}
