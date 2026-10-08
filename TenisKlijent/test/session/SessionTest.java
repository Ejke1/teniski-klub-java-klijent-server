package session;

import domain.Administrator;
import forme.MainForm;
import java.lang.reflect.Constructor;
import java.net.ServerSocket;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import org.junit.BeforeClass;
import org.junit.Test;
import testutil.LazniServer;

public class SessionTest {

    @BeforeClass
    public static void pokreniServer() {
        LazniServer.pokreni();
    }

    @Test
    public void uvekVracaIstuSesiju() {
        assertSame(Session.getInstance(), Session.getInstance());
    }

    @Test
    public void povezanaJeSaServerom() {
        assertTrue(Session.getInstance().getSocket().isConnected());
        assertEquals(LazniServer.pokreni().getPort(), Session.getInstance().getSocket().getPort());
    }

    @Test
    public void pamtiUlogovanogAdministratora() {
        Administrator prethodni = Session.getInstance().getUlogovani();
        Administrator a = new Administrator(2L, "Stefan", "Markovic", "stefan", "stefan123");

        Session.getInstance().setUlogovani(a);
        assertSame(a, Session.getInstance().getUlogovani());

        Session.getInstance().setUlogovani(prethodni);
    }

    @Test
    public void pamtiGlavnuFormu() {
        MainForm prethodna = Session.getInstance().getMf();

        Session.getInstance().setMf(null);
        assertNull(Session.getInstance().getMf());

        Session.getInstance().setMf(prethodna);
    }

    @Test
    public void bezServeraNemaVeze() throws Exception {
        int slobodanPort;
        try (ServerSocket s = new ServerSocket(0)) {
            slobodanPort = s.getLocalPort();
        }
        String prethodni = System.getProperty("server.port");
        System.setProperty("server.port", String.valueOf(slobodanPort));
        try {
            Constructor<Session> konstruktor = Session.class.getDeclaredConstructor();
            konstruktor.setAccessible(true);

            Session bezServera = konstruktor.newInstance();

            assertNull(bezServera.getSocket());
        } finally {
            System.setProperty("server.port", prethodni);
        }
    }
}
