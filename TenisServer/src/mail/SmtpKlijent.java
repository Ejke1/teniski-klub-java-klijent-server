package mail;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

public class SmtpKlijent implements AutoCloseable {

    private static final int TIMEOUT_MS = 20000;

    private final MailKonfiguracija konf;
    private Socket socket;
    private BufferedReader in;
    private OutputStream out;

    public SmtpKlijent(MailKonfiguracija konf) {
        this.konf = konf;
    }

    public void otvori() throws IOException {
        String host = konf.getHost();
        int port = konf.getPort();

        MailLog.upisi("Povezujem se na " + host + ":" + port + " ...");
        if (konf.isSsl()) {
            socket = SSLSocketFactory.getDefault().createSocket();
            socket.connect(new InetSocketAddress(host, port), TIMEOUT_MS);
        } else {
            socket = new Socket();
            socket.connect(new InetSocketAddress(host, port), TIMEOUT_MS);
        }
        socket.setSoTimeout(TIMEOUT_MS);
        tokovi();
        ocekuj(220);
        komanda("EHLO " + lokalnoIme(), 250);

        if (!konf.isSsl() && konf.isStarttls()) {
            komanda("STARTTLS", 220);
            SSLSocket ssl = (SSLSocket) ((SSLSocketFactory) SSLSocketFactory.getDefault())
                    .createSocket(socket, host, port, true);
            ssl.setSoTimeout(TIMEOUT_MS);
            ssl.startHandshake();
            MailLog.upisi("TLS uspostavljen (" + ssl.getSession().getProtocol() + ")");
            socket = ssl;
            tokovi();
            komanda("EHLO " + lokalnoIme(), 250);
        }

        if (konf.getKorisnik() != null && !konf.getKorisnik().isEmpty()) {
            komanda("AUTH LOGIN", 334);
            komanda(b64(konf.getKorisnik()), 334);
            komanda(b64(konf.getLozinka()), 235, true);
        }
    }

    public void posalji(String primalac, String primalacIme, String naslov, String tekst,
            String nazivPriloga, byte[] prilog, String tipPriloga) throws IOException {

        komanda("MAIL FROM:<" + konf.getKorisnik() + ">", 250);
        komanda("RCPT TO:<" + primalac + ">", 250, 251);
        komanda("DATA", 354);

        String granica = "----=_Tenis_" + UUID.randomUUID().toString().replace("-", "");
        StringBuilder m = new StringBuilder();
        m.append("From: ").append(adresa(konf.getImePosiljaoca(), konf.getKorisnik())).append("\r\n");
        m.append("To: ").append(adresa(primalacIme, primalac)).append("\r\n");
        m.append("Subject: ").append(zaglavljeUtf8(naslov)).append("\r\n");
        m.append("Date: ").append(new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z", Locale.US)
                .format(new Date())).append("\r\n");
        m.append("Message-ID: <").append(UUID.randomUUID()).append("@teniski-klub>\r\n");
        m.append("MIME-Version: 1.0\r\n");
        m.append("Content-Type: multipart/mixed; boundary=\"").append(granica).append("\"\r\n\r\n");

        m.append("--").append(granica).append("\r\n");
        m.append("Content-Type: text/plain; charset=UTF-8\r\n");
        m.append("Content-Transfer-Encoding: base64\r\n\r\n");
        m.append(b64Mime(tekst.getBytes(StandardCharsets.UTF_8))).append("\r\n");

        if (prilog != null) {
            m.append("--").append(granica).append("\r\n");
            m.append("Content-Type: ").append(tipPriloga).append("; name=\"").append(nazivPriloga).append("\"\r\n");
            m.append("Content-Disposition: attachment; filename=\"").append(nazivPriloga).append("\"\r\n");
            m.append("Content-Transfer-Encoding: base64\r\n\r\n");
            m.append(b64Mime(prilog)).append("\r\n");
        }
        m.append("--").append(granica).append("--\r\n");

        String poruka = m.toString().replace("\r\n.", "\r\n..");
        MailLog.upisi("Saljem sadrzaj poruke (" + poruka.length() + " znakova) za " + primalac);
        out.write(poruka.getBytes(StandardCharsets.US_ASCII));
        komanda(".", 250);
    }

    public void reset() {
        try {
            komanda("RSET", 250);
        } catch (IOException ignore) {
        }
    }

    @Override
    public void close() {
        try {
            if (socket != null && !socket.isClosed()) {
                komanda("QUIT", 221);
            }
        } catch (IOException ignore) {
        }
        try {
            if (socket != null) {
                socket.close();
            }
        } catch (IOException ignore) {
        }
    }


    private void tokovi() throws IOException {
        in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.US_ASCII));
        out = socket.getOutputStream();
    }

    private void komanda(String cmd, int... ocekivani) throws IOException {
        komanda(cmd, false, ocekivani);
    }

    private void komanda(String cmd, int ocekivan, boolean tajna) throws IOException {
        komanda(cmd, tajna, ocekivan);
    }

    private void komanda(String cmd, boolean tajna, int... ocekivani) throws IOException {
        MailLog.upisi("C: " + (tajna ? "(lozinka - skrivena)" : cmd));
        out.write((cmd + "\r\n").getBytes(StandardCharsets.US_ASCII));
        out.flush();
        try {
            ocekuj(ocekivani);
        } catch (IOException e) {
            String prikaz = tajna ? "(lozinka)" : cmd;
            throw new IOException("SMTP komanda '" + prikaz + "' nije uspela: " + e.getMessage());
        }
    }

    private void ocekuj(int... ocekivani) throws IOException {
        String linija;
        String poslednja;
        do {
            linija = in.readLine();
            if (linija == null) {
                throw new IOException("server je prekinuo vezu");
            }
            poslednja = linija;
        } while (linija.length() > 3 && linija.charAt(3) == '-');

        MailLog.upisi("S: " + poslednja);
        int kod = Integer.parseInt(poslednja.substring(0, 3));
        for (int o : ocekivani) {
            if (kod == o) {
                return;
            }
        }
        throw new IOException(poslednja);
    }

    private static String lokalnoIme() {
        return "teniski-klub.local";
    }

    private static String b64(String s) {
        return Base64.getEncoder().encodeToString(s.getBytes(StandardCharsets.UTF_8));
    }

    private static String b64Mime(byte[] b) {
        return Base64.getMimeEncoder(76, "\r\n".getBytes(StandardCharsets.US_ASCII)).encodeToString(b);
    }

    private static String zaglavljeUtf8(String s) {
        return "=?UTF-8?B?" + b64(s) + "?=";
    }

    private static String adresa(String ime, String email) {
        if (ime == null || ime.trim().isEmpty()) {
            return "<" + email + ">";
        }
        return zaglavljeUtf8(ime) + " <" + email + ">";
    }
}
