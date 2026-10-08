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

public class FormDetaljiTreneraTest {

    private LazniServer server;
    private MainForm glavna;
    private FormPretragaTrenera pretraga;

    @Before
    public void otvori() {
        Gui.proveriEkran();
        server = TestKlijent.pripremi();
        glavna = TestKlijent.glavnaForma();
        pretraga = Gui.saEdt(() -> new FormPretragaTrenera(glavna, true));
    }

    @After
    public void zatvori() {
        Gui.zatvoriSve();
    }

    private FormDetaljiTrenera detalji(Trener t) {
        return Gui.saEdt(() -> new FormDetaljiTrenera(pretraga, true, t));
    }

    private void popuni(FormDetaljiTrenera f, String telefon, String godine) {
        Gui.naEdt(() -> {
            Gui.<JTextField>polje(f, "txtTelefon").setText(telefon);
            Gui.<JFormattedTextField>polje(f, "txtGodine").setText(godine);
        });
    }

    @Test
    public void prikazujePodatkeTrenera() {
        FormDetaljiTrenera f = detalji(server.trener(1));

        assertEquals("Detalji trenera:", f.getTitle());
        assertEquals("Ivan", Gui.<JTextField>polje(f, "txtIme").getText());
        assertEquals("Ristic", Gui.<JTextField>polje(f, "txtPrezime").getText());
        assertEquals("0601010101", Gui.<JTextField>polje(f, "txtTelefon").getText());
        assertEquals("8", Gui.<JFormattedTextField>polje(f, "txtGodine").getText());
        assertFalse(Gui.<JTextField>polje(f, "txtIme").isEditable());
        assertFalse(Gui.<JTextField>polje(f, "txtPrezime").isEditable());
    }

    @Test
    public void izmenaZahtevaSvaPolja() {
        FormDetaljiTrenera f = detalji(server.trener(1));
        String[][] slucajevi = {{"", "8"}, {"0601010101", ""}};
        for (String[] s : slucajevi) {
            popuni(f, s[0], s[1]);
            Gui.klikni(Gui.<JButton>polje(f, "btnIzmeni"));
            assertEquals("Sva polja moraju biti popunjena!", Gui.zatvoriPoruku());
        }
        assertEquals(0, server.brojZahteva(Operation.UPDATE_TRENER));
    }

    @Test
    public void uspesnaIzmenaTrenera() {
        FormDetaljiTrenera f = detalji(server.trener(1));
        popuni(f, "0601010199", "9");
        int ucitavanja = server.brojZahteva(Operation.GET_ALL_TRENER);

        Gui.klikni(Gui.<JButton>polje(f, "btnIzmeni"));

        assertEquals("Uspesno izmenjen trener.", Gui.zatvoriPoruku());
        Gui.sacekajEdt();
        Trener poslat = (Trener) server.poslednji(Operation.UPDATE_TRENER);
        assertEquals(Long.valueOf(1), poslat.getTrenerID());
        assertEquals("0601010199", poslat.getBrojTelefona());
        assertEquals(9, poslat.getGodineIskustva());
        assertTrue(server.brojZahteva(Operation.GET_ALL_TRENER) >= ucitavanja + 2);
        assertFalse(f.isDisplayable());
    }

    @Test
    public void greskaPriIzmeni() {
        FormDetaljiTrenera f = detalji(server.trener(1));
        server.greska(Operation.UPDATE_TRENER, "Trener sa tim brojem telefona vec postoji!");

        Gui.klikni(Gui.<JButton>polje(f, "btnIzmeni"));

        Gui.Poruka p = Gui.cekajPoruku();
        assertEquals("Trener sa tim brojem telefona vec postoji!", p.getTekst());
        assertEquals("Greska", p.getNaslov());
        p.odgovori(JOptionPane.OK_OPTION);
        Gui.sacekajEdt();
        assertTrue(f.isDisplayable());
    }

    @Test
    public void neispravneGodine() {
        FormDetaljiTrenera f = detalji(server.trener(1));
        popuni(f, "0601010101", "osam");

        Gui.klikni(Gui.<JButton>polje(f, "btnIzmeni"));

        assertEquals("For input string: \"osam\"", Gui.zatvoriPoruku());
    }

    private void obrisi(FormDetaljiTrenera f, int odgovor) {
        Gui.klikni(Gui.<JButton>polje(f, "btnObrisi"));
        Gui.Poruka pitanje = Gui.cekajPoruku();
        assertEquals("Da li ste sigurni da zelite da obrisete ovog trenera?", pitanje.getTekst());
        pitanje.odgovori(odgovor);
    }

    @Test
    public void brisanjeSeMozeOtkazatiIliZatvoriti() {
        FormDetaljiTrenera f = detalji(server.trener(1));

        obrisi(f, JOptionPane.NO_OPTION);
        Gui.sacekajEdt();
        obrisi(f, JOptionPane.CLOSED_OPTION);
        Gui.sacekajEdt();

        assertTrue(f.isDisplayable());
        assertEquals(0, server.brojZahteva(Operation.DELETE_TRENER));
    }

    @Test
    public void uspesnoBrisanjeTrenera() {
        Trener milan = server.dodajTrenera("Milan", "Jovic", "0603030303", 7);
        FormDetaljiTrenera f = detalji(milan);

        obrisi(f, JOptionPane.YES_OPTION);

        assertEquals("Uspesno obrisan trener.", Gui.zatvoriPoruku());
        Gui.sacekajEdt();
        assertEquals(milan.getTrenerID(), ((Trener) server.poslednji(Operation.DELETE_TRENER)).getTrenerID());
        assertEquals(2, server.treneri().size());
        assertFalse(f.isDisplayable());
    }

    @Test
    public void trenerKojiVodiGrupuSeNeMozeObrisati() {
        FormDetaljiTrenera f = detalji(server.trener(1));

        obrisi(f, JOptionPane.YES_OPTION);

        Gui.Poruka p = Gui.cekajPoruku();
        assertEquals("Ne mozete da obrisete ovog trenera jer je u nekoj grupi!", p.getTekst());
        assertEquals("Obavestenje", p.getNaslov());
        p.odgovori(JOptionPane.OK_OPTION);
        Gui.sacekajEdt();
        assertTrue(f.isDisplayable());
    }

    @Test
    public void zatvaranjeForme() {
        FormDetaljiTrenera f = detalji(server.trener(2));

        Gui.klikniISacekaj(Gui.<JButton>polje(f, "btnZatvori"));

        assertFalse(f.isDisplayable());
    }
}
