package mail;

import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MailLog {

    public static final String FAJL = "mail.log";

    private MailLog() {
    }

    public static synchronized void upisi(String poruka) {
        String red = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date())
                + " [" + Thread.currentThread().getName() + "] " + poruka;
        System.out.println("[MAIL] " + red);
        try (Writer w = new OutputStreamWriter(new FileOutputStream(System.getProperty("maillog", FAJL), true), StandardCharsets.UTF_8)) {
            w.write(red + System.lineSeparator());
        } catch (Exception ignore) {
        }
    }
}
