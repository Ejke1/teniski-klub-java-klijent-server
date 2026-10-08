package forms;

import java.awt.Color;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPasswordField;
import javax.swing.JProgressBar;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import testutil.Gui;
import thread.ThreadServer;

public class ServerFormTest {

    private ServerForm forma;
    private boolean zatvoren;
    private String prethodniPort;

    @Before
    public void otvori() {
        Gui.proveriEkran();
        prethodniPort = System.getProperty("server.port");
        System.setProperty("server.port", "0");
        zatvoren = false;
        forma = Gui.saEdt(() -> new ServerForm() {
            @Override
            protected void zatvoriProgram() {
                zatvoren = true;
            }
        });
    }

    @After
    public void zatvori() throws Exception {
        if (forma != null) {
            ThreadServer server = Gui.polje(forma, "threadServer");
            if (server != null && server.getServerSocket() != null && !server.getServerSocket().isClosed()) {
                server.getServerSocket().close();
            }
        }
        Gui.zatvoriSve();
        if (prethodniPort == null) {
            System.clearProperty("server.port");
        } else {
            System.setProperty("server.port", prethodniPort);
        }
    }

    private <T> T k(String ime) {
        return Gui.polje(forma, ime);
    }

    private void cekiraj(String ime) {
        JCheckBox cb = k(ime);
        Gui.klikniISacekaj(cb);
    }

    private void pripremiZaPokretanje() {
        cekiraj("cbVerifikacija");
        cekiraj("cbSinhronizacija");
        cekiraj("cbEvidencija");
    }

    @Test
    public void pocetnoStanjeForme() {
        assertEquals("Serverska forma", forma.getTitle());
        assertEquals("Neaktivan", Gui.<JLabel>polje(forma, "lblServerStatus").getText());
        assertEquals(Color.red, Gui.<JLabel>polje(forma, "lblLampa").getBackground());
        assertFalse(Gui.<JButton>polje(forma, "btnPokreniServer").isEnabled());
        assertFalse(Gui.<JButton>polje(forma, "btnUgasiServer").isEnabled());
        assertEquals(0, Gui.<JProgressBar>polje(forma, "progressBar").getValue());
    }

    @Test
    public void svakaStavkaProvereDodaje25Posto() {
        JProgressBar traka = k("progressBar");

        cekiraj("cbVerifikacija");
        assertEquals(50, traka.getValue());
        cekiraj("cbSinhronizacija");
        assertEquals(75, traka.getValue());
        assertFalse(Gui.<JButton>polje(forma, "btnPokreniServer").isEnabled());
        cekiraj("cbEvidencija");
        assertEquals(100, traka.getValue());
        assertTrue(Gui.<JButton>polje(forma, "btnPokreniServer").isEnabled());

        cekiraj("cbEvidencija");
        assertEquals(75, traka.getValue());
        assertFalse(Gui.<JButton>polje(forma, "btnPokreniServer").isEnabled());
    }

    @Test
    public void pogresanPinNeDozvoljavaPokretanje() {
        pripremiZaPokretanje();
        JPasswordField pin = k("txtPIN");

        Gui.naEdt(() -> pin.setText("1111"));

        assertEquals(75, Gui.<JProgressBar>polje(forma, "progressBar").getValue());
        assertFalse(Gui.<JButton>polje(forma, "btnPokreniServer").isEnabled());

        Gui.naEdt(() -> pin.setText("2002"));
        assertEquals(100, Gui.<JProgressBar>polje(forma, "progressBar").getValue());
    }

    @Test
    public void pokretanjeServera() {
        pripremiZaPokretanje();

        Gui.klikniISacekaj(k("btnPokreniServer"));

        ThreadServer server = k("threadServer");
        assertTrue(server.isAlive());
        assertTrue(server.getServerSocket().isBound());
        assertEquals("Aktivan", Gui.<JLabel>polje(forma, "lblServerStatus").getText());
        assertEquals(Color.GREEN, Gui.<JLabel>polje(forma, "lblLampa").getBackground());
        assertTrue(Gui.<JButton>polje(forma, "btnUgasiServer").isEnabled());
        assertFalse(Gui.<JButton>polje(forma, "btnPokreniServer").isEnabled());
        assertFalse(Gui.<JCheckBox>polje(forma, "cbVerifikacija").isEnabled());
        assertFalse(Gui.<JPasswordField>polje(forma, "txtPIN").isEnabled());
    }

    @Test
    public void ponovnoPokretanjeNePraviNoviServer() {
        pripremiZaPokretanje();
        Gui.klikniISacekaj(k("btnPokreniServer"));
        ThreadServer prvi = k("threadServer");

        Gui.naEdt(() -> Gui.<JButton>polje(forma, "btnPokreniServer").setEnabled(true));
        Gui.klikniISacekaj(k("btnPokreniServer"));

        assertTrue(prvi == Gui.polje(forma, "threadServer"));
    }

    @Test
    public void gasenjeServeraZatvaraProgram() throws Exception {
        pripremiZaPokretanje();
        Gui.klikniISacekaj(k("btnPokreniServer"));
        ThreadServer server = k("threadServer");

        Gui.klikni(k("btnUgasiServer"));

        assertEquals("Server je ugasen, gasenje programa...", Gui.zatvoriPoruku());
        Gui.sacekajEdt();
        assertTrue(server.getServerSocket().isClosed());
        server.join(5000);
        assertFalse(server.isAlive());
        assertTrue(zatvoren);
        assertFalse(forma.isDisplayable());
    }

    @Test
    public void menijOtvaraKonfiguracijuBaze() {
        Gui.klikni(k("miKonfiguracija"));

        KonfiguracijaBaze konfiguracija = Gui.cekajProzor(KonfiguracijaBaze.class);
        assertEquals("Konfiguracija baze", konfiguracija.getTitle());
        Gui.naEdt(konfiguracija::dispose);
        Gui.sacekajEdt();
    }

    @Test
    public void pokretanjeProgramaOtvaraServerskuFormu() {
        ServerForm.main(new String[0]);

        ServerForm otvorena = Gui.cekajProzor(ServerForm.class);
        assertEquals("Serverska forma", otvorena.getTitle());
        Gui.naEdt(otvorena::dispose);
    }

    @Test
    public void meniStavkaImaTekst() {
        assertEquals("Izmeni konfiguraciju", Gui.<JMenuItem>polje(forma, "miKonfiguracija").getText());
    }

    @Test
    public void enterUPinPoljuNeMenjaStanje() {
        JPasswordField pin = k("txtPIN");

        Gui.naEdt(pin::postActionEvent);

        assertEquals(0, Gui.<JProgressBar>polje(forma, "progressBar").getValue());
    }

    @Test
    public void iskljucivanjeVerifikacijeSmanjujeSpremnost() {
        pripremiZaPokretanje();

        cekiraj("cbVerifikacija");

        assertEquals(75, Gui.<JProgressBar>polje(forma, "progressBar").getValue());
        assertFalse(Gui.<JButton>polje(forma, "btnPokreniServer").isEnabled());
    }

    @Test
    public void serverSeMozePonovoPokrenutiAkoJeStaraNitZavrsena() throws Exception {
        pripremiZaPokretanje();
        Gui.klikniISacekaj(k("btnPokreniServer"));
        ThreadServer prvi = k("threadServer");
        prvi.getServerSocket().close();
        prvi.join(5000);

        Gui.naEdt(() -> Gui.<JButton>polje(forma, "btnPokreniServer").setEnabled(true));
        Gui.klikniISacekaj(k("btnPokreniServer"));

        ThreadServer drugi = k("threadServer");
        assertTrue(prvi != drugi);
        assertTrue(drugi.isAlive());
    }

    @Test
    public void zauzetPortSePrijavljujePorukomIServerOstajeNeaktivan() throws Exception {
        try (java.net.ServerSocket zauzet = new java.net.ServerSocket(0)) {
            System.setProperty("server.port", String.valueOf(zauzet.getLocalPort()));
            pripremiZaPokretanje();

            Gui.klikni(k("btnPokreniServer"));

            Gui.Poruka poruka = Gui.cekajPoruku();
            assertEquals("Server nije pokrenut jer je port zauzet. Proverite da li je server vec pokrenut.", poruka.getTekst());
            assertEquals("Greska", poruka.getNaslov());
            poruka.odgovori(javax.swing.JOptionPane.OK_OPTION);
            Gui.sacekajEdt();
            ThreadServer server = k("threadServer");
            assertTrue(server.getServerSocket() == null);
            assertFalse(server.isAlive());
            assertEquals("Neaktivan", Gui.<JLabel>polje(forma, "lblServerStatus").getText());
            assertEquals(Color.red, Gui.<JLabel>polje(forma, "lblLampa").getBackground());
            assertTrue(Gui.<JButton>polje(forma, "btnPokreniServer").isEnabled());
            assertFalse(Gui.<JButton>polje(forma, "btnUgasiServer").isEnabled());
        }
    }

    @Test
    public void gasenjeNeradnogServeraNistaNeRadi() throws Exception {
        try (java.net.ServerSocket zauzet = new java.net.ServerSocket(0)) {
            System.setProperty("server.port", String.valueOf(zauzet.getLocalPort()));
            pripremiZaPokretanje();
            Gui.klikni(k("btnPokreniServer"));
            Gui.zatvoriPoruku();
            Gui.sacekajEdt();

            Gui.naEdt(() -> Gui.<JButton>polje(forma, "btnUgasiServer").setEnabled(true));
            Gui.klikniISacekaj(k("btnUgasiServer"));

            assertFalse(Gui.imaOtvorenihPoruka());
            assertFalse(zatvoren);
            assertTrue(forma.isDisplayable());
        }
    }
}
