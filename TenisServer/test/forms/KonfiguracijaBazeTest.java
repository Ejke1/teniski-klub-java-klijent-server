package forms;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Properties;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import testutil.Gui;

public class KonfiguracijaBazeTest {

    private String prethodna;
    private File fajl;
    private KonfiguracijaBaze forma;

    @Before
    public void pripremi() throws Exception {
        Gui.proveriEkran();
        prethodna = System.getProperty("dbconfig");
        fajl = File.createTempFile("dbconfig-forma-", ".properties");
        fajl.deleteOnExit();
        Properties p = new Properties();
        p.setProperty("url", "jdbc:mysql://localhost:3306/teniskiklub");
        p.setProperty("username", "root");
        p.setProperty("password", "tajna");
        try (OutputStream out = new FileOutputStream(fajl)) {
            p.store(out, null);
        }
        System.setProperty("dbconfig", fajl.getPath());
        forma = Gui.saEdt(() -> new KonfiguracijaBaze(null, true));
    }

    @After
    public void vrati() {
        Gui.zatvoriSve();
        if (prethodna == null) {
            System.clearProperty("dbconfig");
        } else {
            System.setProperty("dbconfig", prethodna);
        }
    }

    private Properties procitaj() throws Exception {
        Properties p = new Properties();
        try (FileInputStream in = new FileInputStream(fajl)) {
            p.load(in);
        }
        return p;
    }

    @Test
    public void popunjavaPoljaIzPostojeceKonfiguracije() {
        assertEquals("Konfiguracija baze", forma.getTitle());
        assertEquals("teniskiklub", Gui.<JTextField>polje(forma, "txtNazivBaze").getText());
        assertEquals("root", Gui.<JTextField>polje(forma, "txtUsername").getText());
        assertEquals("tajna", new String(Gui.<JPasswordField>polje(forma, "txtPassword").getPassword()));
    }

    @Test
    public void cuvaNovuKonfiguraciju() throws Exception {
        Gui.naEdt(() -> {
            Gui.<JTextField>polje(forma, "txtNazivBaze").setText("nova_baza");
            Gui.<JTextField>polje(forma, "txtUsername").setText("admin");
            Gui.<JPasswordField>polje(forma, "txtPassword").setText("lozinka");
        });

        Gui.klikni(Gui.polje(forma, "btnSacuvaj"));

        assertEquals("Uspesno sacuvana konfiguracija.", Gui.zatvoriPoruku());
        Gui.sacekajEdt();
        Properties p = procitaj();
        assertEquals("jdbc:mysql://localhost:3306/nova_baza", p.getProperty("url"));
        assertEquals("admin", p.getProperty("username"));
        assertEquals("lozinka", p.getProperty("password"));
        assertFalse(forma.isDisplayable());
    }

    @Test
    public void nazivBazeJeObavezan() throws Exception {
        Gui.naEdt(() -> Gui.<JTextField>polje(forma, "txtNazivBaze").setText(""));

        Gui.klikni(Gui.polje(forma, "btnSacuvaj"));

        assertEquals("Morate uneti naziv baze!", Gui.zatvoriPoruku());
        assertEquals("jdbc:mysql://localhost:3306/teniskiklub", procitaj().getProperty("url"));
    }

    @Test
    public void bezKonfiguracijeSuPoljaPrazna() throws Exception {
        assertTrue(fajl.delete());

        KonfiguracijaBaze prazna = Gui.saEdt(() -> new KonfiguracijaBaze(null, true));

        assertEquals("", Gui.<JTextField>polje(prazna, "txtNazivBaze").getText());
    }

    @Test
    public void neispravanPutDoFajlaNeObaraFormu() throws Exception {
        File folder = java.nio.file.Files.createTempDirectory("dbconfig-folder").toFile();
        System.setProperty("dbconfig", folder.getPath());
        try {
            KonfiguracijaBaze f = Gui.saEdt(() -> new KonfiguracijaBaze(null, true));
            Gui.naEdt(() -> Gui.<JTextField>polje(f, "txtNazivBaze").setText("baza"));

            Gui.klikniISacekaj(Gui.polje(f, "btnSacuvaj"));

            assertFalse(Gui.imaOtvorenihPoruka());
        } finally {
            folder.delete();
        }
    }
}
