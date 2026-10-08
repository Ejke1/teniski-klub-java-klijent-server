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

public class FormDetaljiClanaTest {

    private LazniServer server;
    private MainForm glavna;
    private FormPretragaClana pretraga;

    @Before
    public void otvori() {
        Gui.proveriEkran();
        server = TestKlijent.pripremi();
        glavna = TestKlijent.glavnaForma();
        pretraga = Gui.saEdt(() -> new FormPretragaClana(glavna, true));
    }

    @After
    public void zatvori() {
        Gui.zatvoriSve();
    }

    private FormDetaljiClana detalji(long idClana) {
        Clan c = server.clan(idClana);
        return Gui.saEdt(() -> new FormDetaljiClana(pretraga, true, c));
    }

    private void popuni(FormDetaljiClana f, String email, String telefon, String godine) {
        Gui.naEdt(() -> {
            Gui.<JTextField>polje(f, "txtEmail").setText(email);
            Gui.<JTextField>polje(f, "txtTelefon").setText(telefon);
            Gui.<JFormattedTextField>polje(f, "txtGodine").setText(godine);
        });
    }

    @Test
    public void prikazujePodatkeClana() {
        FormDetaljiClana f = detalji(3);

        assertEquals("Detalji clana", f.getTitle());
        assertEquals("Petar", Gui.<JTextField>polje(f, "txtIme").getText());
        assertEquals("Vasic", Gui.<JTextField>polje(f, "txtPrezime").getText());
        assertEquals("petar@test.rs", Gui.<JTextField>polje(f, "txtEmail").getText());
        assertEquals("0645552222", Gui.<JTextField>polje(f, "txtTelefon").getText());
        assertEquals("7", Gui.<JFormattedTextField>polje(f, "txtGodine").getText());
        assertFalse(Gui.<JTextField>polje(f, "txtIme").isEditable());
        assertFalse(Gui.<JTextField>polje(f, "txtPrezime").isEditable());
        assertEquals("Pocetni", Gui.<JComboBox<?>>polje(f, "cmbKategorija").getSelectedItem().toString());
        assertEquals(3, Gui.<JComboBox<?>>polje(f, "cmbKategorija").getItemCount());
    }

    @Test
    public void izmenaZahtevaSvaPolja() {
        FormDetaljiClana f = detalji(3);
        String[][] slucajevi = {{"", "0645552222", "7"}, {"petar@test.rs", "", "7"}, {"petar@test.rs", "0645552222", ""}};
        for (String[] s : slucajevi) {
            popuni(f, s[0], s[1], s[2]);
            Gui.klikni(Gui.<JButton>polje(f, "btnIzmeni"));
            assertEquals("Sva polja moraju biti popunjena!", Gui.zatvoriPoruku());
        }
        assertEquals(0, server.brojZahteva(Operation.UPDATE_CLAN));
    }

    @Test
    public void uspesnaIzmenaClana() {
        FormDetaljiClana f = detalji(3);
        popuni(f, "petar.v@test.rs", "0645553333", "8");
        Gui.naEdt(() -> Gui.<JComboBox<?>>polje(f, "cmbKategorija").setSelectedIndex(1));
        int ucitavanja = server.brojZahteva(Operation.GET_ALL_CLAN);

        Gui.klikni(Gui.<JButton>polje(f, "btnIzmeni"));

        assertEquals("Uspesno izmenjen clan.", Gui.zatvoriPoruku());
        Gui.sacekajEdt();
        Clan poslat = (Clan) server.poslednji(Operation.UPDATE_CLAN);
        assertEquals(Long.valueOf(3), poslat.getClanID());
        assertEquals("petar.v@test.rs", poslat.getEmail());
        assertEquals("0645553333", poslat.getTelefon());
        assertEquals(8, poslat.getGodine());
        assertEquals("Srednji", ((Kategorija) poslat.getKategorija()).getNaziv());
        assertTrue(server.brojZahteva(Operation.GET_ALL_CLAN) > ucitavanja);
        assertFalse(f.isDisplayable());
    }

    @Test
    public void greskaPriIzmeni() {
        FormDetaljiClana f = detalji(3);
        server.greska(Operation.UPDATE_CLAN, "Clan sa tim telefonom vec postoji!");

        Gui.klikni(Gui.<JButton>polje(f, "btnIzmeni"));

        Gui.Poruka p = Gui.cekajPoruku();
        assertEquals("Clan sa tim telefonom vec postoji!", p.getTekst());
        assertEquals("Greska", p.getNaslov());
        p.odgovori(JOptionPane.OK_OPTION);
        Gui.sacekajEdt();
        assertTrue(f.isDisplayable());
    }

    private void obrisi(FormDetaljiClana f, int odgovor) {
        Gui.klikni(Gui.<JButton>polje(f, "btnObrisi"));
        Gui.Poruka pitanje = Gui.cekajPoruku();
        assertEquals("Da li ste sigurni da zelite da obrisete ovog clana?", pitanje.getTekst());
        pitanje.odgovori(odgovor);
    }

    @Test
    public void brisanjeSeMozeOtkazati() {
        FormDetaljiClana f = detalji(3);

        obrisi(f, JOptionPane.NO_OPTION);
        Gui.sacekajEdt();

        assertTrue(f.isDisplayable());
        assertEquals(0, server.brojZahteva(Operation.DELETE_CLAN));
    }

    @Test
    public void zatvaranjePitanjaNeBriseClana() {
        FormDetaljiClana f = detalji(3);

        obrisi(f, JOptionPane.CLOSED_OPTION);
        Gui.sacekajEdt();

        assertTrue(f.isDisplayable());
        assertEquals(0, server.brojZahteva(Operation.DELETE_CLAN));
    }

    @Test
    public void uspesnoBrisanjeClana() {
        FormDetaljiClana f = detalji(3);

        obrisi(f, JOptionPane.YES_OPTION);

        assertEquals("Uspesno obrisan clan.", Gui.zatvoriPoruku());
        Gui.sacekajEdt();
        assertEquals(Long.valueOf(3), ((Clan) server.poslednji(Operation.DELETE_CLAN)).getClanID());
        assertEquals(4, server.clanovi().size());
        assertFalse(f.isDisplayable());
    }

    @Test
    public void clanKojiJeUGrupiSeNeMozeObrisati() {
        FormDetaljiClana f = detalji(1);

        obrisi(f, JOptionPane.YES_OPTION);

        Gui.Poruka p = Gui.cekajPoruku();
        assertEquals("Ne mozete da obrisete ovog clana jer je u nekoj grupi!", p.getTekst());
        assertEquals("Obavestenje", p.getNaslov());
        p.odgovori(JOptionPane.OK_OPTION);
        Gui.sacekajEdt();
        assertEquals(5, server.clanovi().size());
        assertTrue(f.isDisplayable());
    }

    @Test
    public void zatvaranjeForme() {
        FormDetaljiClana f = detalji(3);

        Gui.klikniISacekaj(Gui.<JButton>polje(f, "btnZatvori"));

        assertFalse(f.isDisplayable());
    }

    @Test
    public void greskaPriUcitavanjuKategorijaNeObaraFormu() {
        server.greska(Operation.GET_ALL_KATEGORIJA, "Server ne radi");

        FormDetaljiClana f = detalji(3);

        assertEquals("Petar", Gui.<JTextField>polje(f, "txtIme").getText());
    }
}
