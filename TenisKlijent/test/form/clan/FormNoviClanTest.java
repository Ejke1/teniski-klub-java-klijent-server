package form.clan;

import domain.Clan;
import domain.Kategorija;
import forme.MainForm;
import javax.swing.JButton;
import javax.swing.JComboBox;
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

public class FormNoviClanTest {

    private LazniServer server;
    private MainForm glavna;
    private FormNoviClan forma;

    @Before
    public void otvori() {
        Gui.proveriEkran();
        server = TestKlijent.pripremi();
        glavna = TestKlijent.glavnaForma();
        forma = Gui.saEdt(() -> new FormNoviClan(glavna, true));
    }

    @After
    public void zatvori() {
        Gui.zatvoriSve();
    }

    private void popuni(String ime, String prezime, String godine, String email, String telefon) {
        Gui.naEdt(() -> {
            Gui.<JTextField>polje(forma, "txtIme").setText(ime);
            Gui.<JTextField>polje(forma, "txtPrezime").setText(prezime);
            Gui.<JFormattedTextField>polje(forma, "txtGodine").setText(godine);
            Gui.<JTextField>polje(forma, "txtEmail").setText(email);
            Gui.<JTextField>polje(forma, "txtTelefon").setText(telefon);
        });
    }

    private String dodaj() {
        Gui.klikni(Gui.<JButton>polje(forma, "btnDodaj"));
        return Gui.zatvoriPoruku();
    }

    @Test
    public void ucitavaKategorije() {
        assertEquals("Unos clana", forma.getTitle());
        JComboBox<?> kategorije = Gui.polje(forma, "cmbKategorija");
        assertEquals(3, kategorije.getItemCount());
        assertTrue(kategorije.getItemAt(0) instanceof Kategorija);
    }

    @Test
    public void svaPoljaSuObavezna() {
        String[][] slucajevi = {
            {"", "Peric", "11", "j@test.rs", "0641234567"},
            {"Jovana", "", "11", "j@test.rs", "0641234567"},
            {"Jovana", "Peric", "", "j@test.rs", "0641234567"},
            {"Jovana", "Peric", "11", "", "0641234567"},
            {"Jovana", "Peric", "11", "j@test.rs", ""}
        };
        for (String[] s : slucajevi) {
            popuni(s[0], s[1], s[2], s[3], s[4]);
            assertEquals("Sva polja moraju biti popunjena!", dodaj());
        }
        assertEquals(0, server.brojZahteva(Operation.ADD_CLAN));
    }

    @Test
    public void uspesnoDodavanjeClana() {
        popuni("Jovana", "Peric", "11", "jovana@test.rs", "0641234567");
        Gui.naEdt(() -> Gui.<JComboBox<?>>polje(forma, "cmbKategorija").setSelectedIndex(1));
        int ucitavanjaKategorija = server.brojZahteva(Operation.GET_ALL_KATEGORIJA);

        assertEquals("Uspesno dodat clan.", dodaj());
        Gui.sacekajEdt();

        Clan poslat = (Clan) server.poslednji(Operation.ADD_CLAN);
        assertEquals("Jovana", poslat.getIme());
        assertEquals("Peric", poslat.getPrezime());
        assertEquals(11, poslat.getGodine());
        assertEquals("jovana@test.rs", poslat.getEmail());
        assertEquals("0641234567", poslat.getTelefon());
        assertEquals("Srednji", poslat.getKategorija().getNaziv());
        assertEquals(ucitavanjaKategorija + 1, server.brojZahteva(Operation.GET_ALL_KATEGORIJA));
        assertFalse(forma.isDisplayable());
    }

    @Test
    public void greskaSaServeraSePrikazuje() {
        popuni("Jovana", "Peric", "11", "jovana@test.rs", "0641234567");
        server.greska(Operation.ADD_CLAN, "Clan sa tim emailom vec postoji!");

        Gui.klikni(Gui.<JButton>polje(forma, "btnDodaj"));

        Gui.Poruka p = Gui.cekajPoruku();
        assertEquals("Clan sa tim emailom vec postoji!", p.getTekst());
        assertEquals("Greska", p.getNaslov());
        assertEquals(JOptionPane.ERROR_MESSAGE, p.getTip());
        p.odgovori(JOptionPane.OK_OPTION);
        Gui.sacekajEdt();
        assertTrue(forma.isDisplayable());
    }

    @Test
    public void neispravneGodine() {
        popuni("Jovana", "Peric", "jedanaest", "jovana@test.rs", "0641234567");

        assertEquals("For input string: \"jedanaest\"", dodaj());
        assertEquals(0, server.brojZahteva(Operation.ADD_CLAN));
    }

    @Test
    public void zatvaranjeForme() {
        Gui.klikniISacekaj(Gui.<JButton>polje(forma, "btnZatvori"));

        assertFalse(forma.isDisplayable());
    }

    @Test
    public void greskaPriUcitavanjuKategorijaNeObaraFormu() {
        server.greska(Operation.GET_ALL_KATEGORIJA, "Server ne radi");

        FormNoviClan f = Gui.saEdt(() -> new FormNoviClan(glavna, true));

        assertEquals("Unos clana", f.getTitle());
    }
}
