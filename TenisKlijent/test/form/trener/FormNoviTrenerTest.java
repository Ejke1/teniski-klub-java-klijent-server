package form.trener;

import domain.Trener;
import forme.MainForm;
import javax.swing.JButton;
import javax.swing.JFormattedTextField;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import testutil.Gui;
import testutil.LazniServer;
import testutil.TestKlijent;
import transfer.util.Operation;

public class FormNoviTrenerTest {

    private LazniServer server;
    private MainForm glavna;
    private FormNoviTrener forma;

    @Before
    public void otvori() {
        Gui.proveriEkran();
        server = TestKlijent.pripremi();
        glavna = TestKlijent.glavnaForma();
        forma = Gui.saEdt(() -> new FormNoviTrener(glavna, true));
    }

    @After
    public void zatvori() {
        Gui.zatvoriSve();
    }

    private void popuni(String ime, String prezime, String telefon, String godine) {
        Gui.naEdt(() -> {
            Gui.<JTextField>polje(forma, "txtIme").setText(ime);
            Gui.<JTextField>polje(forma, "txtPrezime").setText(prezime);
            Gui.<JTextField>polje(forma, "txtTelefon").setText(telefon);
            Gui.<JFormattedTextField>polje(forma, "txtGodine").setText(godine);
        });
    }

    private String dodaj() {
        Gui.klikni(Gui.<JButton>polje(forma, "btnDodaj"));
        return Gui.zatvoriPoruku();
    }

    @Test
    public void naslovForme() {
        assertEquals("Unos trenera", forma.getTitle());
    }

    @Test
    public void svaPoljaSuObavezna() {
        String[][] slucajevi = {
            {"", "Jovic", "0603030303", "7"},
            {"Milan", "", "0603030303", "7"},
            {"Milan", "Jovic", "", "7"},
            {"Milan", "Jovic", "0603030303", ""}
        };
        for (String[] s : slucajevi) {
            popuni(s[0], s[1], s[2], s[3]);
            assertEquals("Sva polja moraju biti popunjena!", dodaj());
        }
        assertEquals(0, server.brojZahteva(Operation.ADD_TRENER));
    }

    @Test
    public void uspesnoDodavanjeTrenera() {
        popuni("Milan", "Jovic", "0603030303", "7");
        int ucitavanja = server.brojZahteva(Operation.GET_ALL_TRENER);

        assertEquals("Uspesno dodat trener.", dodaj());
        Gui.sacekajEdt();

        Trener poslat = (Trener) server.poslednji(Operation.ADD_TRENER);
        assertEquals("Milan", poslat.getIme());
        assertEquals("Jovic", poslat.getPrezime());
        assertEquals("0603030303", poslat.getBrojTelefona());
        assertEquals(7, poslat.getGodineIskustva());
        assertEquals(ucitavanja + 1, server.brojZahteva(Operation.GET_ALL_TRENER));
        assertFalse(forma.isDisplayable());
    }

    @Test
    public void greskaSaServeraSePrikazuje() {
        popuni("Milan", "Jovic", "0603030303", "4");
        server.greska(Operation.ADD_TRENER, "Godine moraju biti izmedju 5 i 70!");

        Gui.klikni(Gui.<JButton>polje(forma, "btnDodaj"));

        Gui.Poruka p = Gui.cekajPoruku();
        assertEquals("Godine moraju biti izmedju 5 i 70!", p.getTekst());
        assertEquals("Greska", p.getNaslov());
        p.odgovori(JOptionPane.OK_OPTION);
        Gui.sacekajEdt();
        assertTrue(forma.isDisplayable());
    }

    @Test
    public void neispravneGodineIskustva() {
        popuni("Milan", "Jovic", "0603030303", "sedam");

        assertEquals("For input string: \"sedam\"", dodaj());
    }

    @Test
    public void enterUPoljuPrezimeNistaNeRadi() {
        JTextField prezime = Gui.polje(forma, "txtPrezime");

        Gui.naEdt(prezime::postActionEvent);

        assertEquals(0, server.brojZahteva(Operation.ADD_TRENER));
    }

    @Test
    public void zatvaranjeForme() {
        Gui.klikniISacekaj(Gui.<JButton>polje(forma, "btnZatvori"));

        assertFalse(forma.isDisplayable());
    }
}
