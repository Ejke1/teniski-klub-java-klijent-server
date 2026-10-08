package forme;

import domain.Administrator;
import domain.ClanGrupe;
import domain.Grupa;
import domain.Kategorija;
import domain.Trener;
import form.clan.FormNoviClan;
import form.clan.FormPretragaClana;
import form.grupa.FormPretragaGrupe;
import form.trener.FormNoviTrener;
import form.trener.FormPretragaTrenera;
import java.awt.Window;
import java.awt.event.WindowEvent;
import javax.swing.AbstractButton;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import models.TableModelClanoviGrupe;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import session.Session;
import testutil.Gui;
import testutil.IzborClana;
import testutil.LazniServer;
import testutil.TestKlijent;
import transfer.util.Operation;

public class MainFormTest {

    private LazniServer server;
    private MainForm forma;
    private boolean zatvoren;

    @Before
    public void otvori() {
        Gui.proveriEkran();
        server = TestKlijent.pripremi();
        zatvoren = false;
        forma = Gui.saEdt(() -> new MainForm() {
            @Override
            protected void zatvoriProgram() {
                zatvoren = true;
            }
        });
    }

    @After
    public void zatvori() {
        Gui.zatvoriSve();
    }

    private <T> T k(String ime) {
        return Gui.polje(forma, ime);
    }

    private TableModelClanoviGrupe tabela() {
        return (TableModelClanoviGrupe) Gui.<JTable>polje(forma, "tblClanovi").getModel();
    }

    private void klikni(String dugme) {
        Gui.klikni(Gui.<AbstractButton>polje(forma, dugme));
    }

    private String klikniIPorukom(String dugme) {
        klikni(dugme);
        return Gui.zatvoriPoruku();
    }

    private void izaberiPrekoLupe(int red) {
        klikni("btnPretraga");
        IzborClana.izaberi(red);
        Gui.sacekajEdt();
    }

    private void dodajClana(int red, String napomena) {
        izaberiPrekoLupe(red);
        Gui.naEdt(() -> Gui.<JTextArea>polje(forma, "txtNapomena").setText(napomena));
        Gui.klikniISacekaj(k("btnDodajClana"));
    }

    private void oznaciRed(int red) {
        JTable t = k("tblClanovi");
        Gui.naEdt(() -> t.setRowSelectionInterval(red, red));
    }

    private String txtClan() {
        return Gui.<JTextField>polje(forma, "txtClan").getText();
    }

    @Test
    public void pocetnoStanjeForme() {
        assertEquals("Klijentska forma", forma.getTitle());
        assertEquals("Ulogovani administrator: Aleksandar Eic", Gui.<JLabel>polje(forma, "lblUlogovani").getText());
        assertEquals(2, Gui.<JComboBox<?>>polje(forma, "cmbTrener").getItemCount());
        assertEquals(3, Gui.<JComboBox<?>>polje(forma, "cmbKategorija").getItemCount());
        assertEquals(0, tabela().getRowCount());
        assertSame(forma, Session.getInstance().getMf());
    }

    @Test
    public void lupaNudiSamoClanoveIzabraneKategorije() {
        klikni("btnPretraga");

        assertEquals(3, IzborClana.brojPonudjenih());
        IzborClana.izaberi(1);
        Gui.sacekajEdt();

        assertEquals("Uros Milosevic (uros@test.rs)", txtClan());
    }

    @Test
    public void otkazanIzborNeMenjaClana() {
        klikni("btnPretraga");
        IzborClana.otkazi();
        Gui.sacekajEdt();

        assertEquals("", txtClan());
    }

    @Test
    public void lupaBezKategorijePrijavljujeGresku() {
        Gui.naEdt(() -> Gui.<JComboBox<?>>polje(forma, "cmbKategorija").setSelectedIndex(-1));

        assertEquals("Prvo izaberite kategoriju grupe!", klikniIPorukom("btnPretraga"));
    }

    @Test
    public void dodavanjeBezIzabranogClana() {
        assertEquals("Izaberite clana klikom na lupu!", klikniIPorukom("btnDodajClana"));
    }

    @Test
    public void dodavanjeClanaUGrupuINapomena() {
        dodajClana(1, "Ne moze petkom.");

        assertEquals(1, tabela().getRowCount());
        assertEquals("Uros Milosevic", tabela().getValueAt(0, 1));
        assertEquals("Ne moze petkom.", tabela().getStavka(0).getNapomena());
        assertEquals("", txtClan());

        dodajClana(0, "");
        assertEquals("/", tabela().getStavka(1).getNapomena());
    }

    @Test
    public void istiClanSeNeMozeDodatiDvaPuta() {
        dodajClana(0, "");
        izaberiPrekoLupe(0);

        assertEquals("Vec ste uneli ovog clana!", klikniIPorukom("btnDodajClana"));
        assertEquals(1, tabela().getRowCount());
    }

    @Test
    public void oznacavanjeRedaUcitavaClanaINapomenu() {
        dodajClana(0, "");
        dodajClana(1, "Ne moze petkom.");

        oznaciRed(0);
        assertEquals("Lana Stojanovic (lana@test.rs)", txtClan());
        assertEquals("", Gui.<JTextArea>polje(forma, "txtNapomena").getText());

        oznaciRed(1);
        assertEquals("Ne moze petkom.", Gui.<JTextArea>polje(forma, "txtNapomena").getText());
    }

    @Test
    public void zamenaBezOznacenogReda() {
        assertEquals("Oznacite u tabeli clana kojeg menjate!", klikniIPorukom("btnIzmeniClana"));
    }

    @Test
    public void zamenaBezKategorije() {
        dodajClana(0, "");
        oznaciRed(0);
        Gui.naEdt(() -> Gui.<JComboBox<?>>polje(forma, "cmbKategorija").setSelectedIndex(-1));

        assertEquals("Prvo izaberite kategoriju grupe!", klikniIPorukom("btnIzmeniClana"));
    }

    @Test
    public void zamenaClanaUOznacenomRedu() {
        dodajClana(0, "");
        oznaciRed(0);

        klikni("btnIzmeniClana");
        IzborClana.izaberi(2);

        assertEquals("Clan u stavci 1 je izmenjen.", Gui.zatvoriPoruku());
        assertEquals("Petar Vasic", tabela().getValueAt(0, 1));
        assertEquals(1, tabela().getValueAt(0, 0));
    }

    @Test
    public void zamenaClanomKojiJeVecUGrupi() {
        dodajClana(0, "");
        dodajClana(1, "");
        oznaciRed(0);

        klikni("btnIzmeniClana");
        IzborClana.izaberi(1);

        assertEquals("Ovaj clan je vec u grupi!", Gui.zatvoriPoruku());
        assertEquals("Lana Stojanovic", tabela().getValueAt(0, 1));
    }

    @Test
    public void otkazanaZamenaNeMenjaTabelu() {
        dodajClana(0, "");
        oznaciRed(0);

        klikni("btnIzmeniClana");
        IzborClana.otkazi();
        Gui.sacekajEdt();

        assertEquals("Lana Stojanovic", tabela().getValueAt(0, 1));
    }

    @Test
    public void brisanjeBezOznacenogReda() {
        assertEquals("Izaberite stavku u tabeli koju brisete!", klikniIPorukom("btnObrisiClana"));
    }

    @Test
    public void brisanjeStavkePrenumerisuTabelu() {
        dodajClana(0, "");
        dodajClana(1, "");
        oznaciRed(0);

        Gui.klikniISacekaj(k("btnObrisiClana"));

        assertEquals(1, tabela().getRowCount());
        assertEquals("Uros Milosevic", tabela().getValueAt(0, 1));
        assertEquals(1, tabela().getValueAt(0, 0));
        assertEquals("", txtClan());
    }

    private void popuniGrupu(String naziv, String opis, String kapacitet) {
        Gui.naEdt(() -> {
            Gui.<JTextField>polje(forma, "txtNaziv").setText(naziv);
            Gui.<JTextArea>polje(forma, "txtOpis").setText(opis);
            Gui.<JFormattedTextField>polje(forma, "txtMaxKapacitet").setText(kapacitet);
        });
    }

    @Test
    public void cuvanjeBezObaveznihPolja() {
        popuniGrupu("", "Opis", "8");
        assertEquals("Sva polja moraju biti popunjena!", klikniIPorukom("btnSacuvaj"));

        popuniGrupu("Grupa", "", "8");
        assertEquals("Sva polja moraju biti popunjena!", klikniIPorukom("btnSacuvaj"));

        popuniGrupu("Grupa", "Opis", "");
        assertEquals("Sva polja moraju biti popunjena!", klikniIPorukom("btnSacuvaj"));

        assertEquals(0, server.brojZahteva(Operation.ADD_GRUPA));
    }

    @Test
    public void uspesnoCuvanjeGrupe() {
        dodajClana(0, "");
        dodajClana(1, "Ne moze petkom.");
        popuniGrupu("Nova grupa", "Opis nove grupe", "8");
        Gui.naEdt(() -> Gui.<JComboBox<?>>polje(forma, "cmbTrener").setSelectedIndex(1));

        assertEquals("Uspesno sacuvana grupa!\n\nMail sa PDF spiskom clanova je poslat na 2 od 2 clanova grupe.",
                klikniIPorukom("btnSacuvaj"));
        Gui.sacekajEdt();

        Grupa poslata = (Grupa) server.poslednji(Operation.ADD_GRUPA);
        assertEquals("Nova grupa", poslata.getNaziv());
        assertEquals("Opis nove grupe", poslata.getOpis());
        assertEquals(8, poslata.getMaxKapacitet());
        assertEquals(2, poslata.getBrojClanova());
        assertEquals("Tamara", poslata.getTrener().getIme());
        assertEquals(Long.valueOf(1), poslata.getKategorija().getKategorijaID());
        assertEquals("coa", poslata.getAdministrator().getUsername());
        assertEquals(2, poslata.getClanoviGrupe().size());
        ClanGrupe druga = poslata.getClanoviGrupe().get(1);
        assertEquals(2, druga.getRb());
        assertEquals("Ne moze petkom.", druga.getNapomena());
        assertEquals("", Gui.<JTextField>polje(forma, "txtNaziv").getText());
        assertEquals("", Gui.<JTextArea>polje(forma, "txtOpis").getText());
        assertEquals(0, tabela().getRowCount());
    }

    @Test
    public void greskaPriCuvanjuGrupe() {
        dodajClana(0, "");
        popuniGrupu("Nova grupa", "Opis", "8");
        server.greska(Operation.ADD_GRUPA, "Morate uneti barem 2 clana!");

        klikni("btnSacuvaj");

        Gui.Poruka prva = Gui.cekajPoruku();
        assertEquals("Sistem ne moze da kreira grupu", prva.getTekst());
        assertEquals("Greska", prva.getNaslov());
        prva.odgovori(JOptionPane.OK_OPTION);
        assertEquals("Morate uneti barem 2 clana!", Gui.zatvoriPoruku());
        Gui.sacekajEdt();
        assertEquals("Nova grupa", Gui.<JTextField>polje(forma, "txtNaziv").getText());
        assertEquals(1, tabela().getRowCount());
    }

    @Test
    public void neispravanKapacitet() {
        popuniGrupu("Nova grupa", "Opis", "abc");

        klikni("btnSacuvaj");

        assertEquals("Sistem ne moze da kreira grupu", Gui.zatvoriPoruku());
        assertEquals("For input string: \"abc\"", Gui.zatvoriPoruku());
    }

    @Test
    public void promenaKategorijeBriseIzabraneClanove() {
        dodajClana(0, "");

        Gui.naEdt(() -> Gui.<JComboBox<?>>polje(forma, "cmbKategorija").setSelectedIndex(1));

        assertEquals(0, tabela().getRowCount());
        assertEquals("", txtClan());
    }

    @Test
    public void ponovnoUcitavanjeKategorijaZadrzavaIzbor() {
        Gui.naEdt(() -> Gui.<JComboBox<?>>polje(forma, "cmbKategorija").setSelectedIndex(1));
        dodajClana(0, "");

        Gui.naEdt(forma::popuniKategorije);

        Kategorija izabrana = (Kategorija) Gui.<JComboBox<?>>polje(forma, "cmbKategorija").getSelectedItem();
        assertEquals("Srednji", izabrana.getNaziv());
        assertEquals(1, tabela().getRowCount());
    }

    @Test
    public void greskaPriUcitavanjuKategorijaPrazniTabelu() {
        dodajClana(0, "");
        server.greska(Operation.GET_ALL_KATEGORIJA, "Server ne radi");

        Gui.naEdt(forma::popuniKategorije);

        assertEquals(0, tabela().getRowCount());
    }

    @Test
    public void greskaPriUcitavanjuTreneraZadrzavaListu() {
        server.greska(Operation.GET_ALL_TRENER, "Server ne radi");

        Gui.naEdt(forma::popuniTrenere);

        assertEquals(2, Gui.<JComboBox<?>>polje(forma, "cmbTrener").getItemCount());
    }

    @Test
    public void osvezavanjeClanovaGrupeSaServera() {
        dodajClana(0, "");
        dodajClana(1, "");
        oznaciRed(0);
        server.izmeniClana(1, 11, 1);
        server.ukloniClana(2);

        Gui.kasnije(forma::osveziClanoveGrupe);

        Gui.Poruka p = Gui.cekajPoruku();
        assertEquals("Sledeci clanovi su uklonjeni iz grupe jer su obrisani ili vise ne pripadaju kategoriji grupe:\n"
                + "Uros Milosevic", p.getTekst());
        assertEquals("Obavestenje", p.getNaslov());
        p.odgovori(JOptionPane.OK_OPTION);
        Gui.sacekajEdt();
        assertEquals(1, tabela().getRowCount());
        assertEquals(11, tabela().getValueAt(0, 2));
        assertEquals("Lana Stojanovic (lana@test.rs)", txtClan());
    }

    @Test
    public void osvezavanjeBezPromenaNePrikazujePoruku() {
        dodajClana(0, "");
        izaberiPrekoLupe(2);

        Gui.naEdt(forma::osveziClanoveGrupe);

        assertFalse(Gui.imaOtvorenihPoruka());
        assertEquals("Petar Vasic (petar@test.rs)", txtClan());
    }

    @Test
    public void osvezavanjePrazneTabeleNeKontaktiraServer() {
        int pre = server.brojZahteva(Operation.GET_ALL_CLAN);

        Gui.naEdt(forma::osveziClanoveGrupe);

        assertEquals(pre, server.brojZahteva(Operation.GET_ALL_CLAN));
    }

    @Test
    public void greskaPriOsvezavanjuSeSamoBelezi() {
        dodajClana(0, "");
        server.greska(Operation.GET_ALL_CLAN, "Server ne radi");

        Gui.naEdt(forma::osveziClanoveGrupe);

        assertFalse(Gui.imaOtvorenihPoruka());
        assertEquals(1, tabela().getRowCount());
    }

    private void proveriMeni(String stavka, Class<? extends Window> forma) {
        klikni(stavka);
        Window otvorena = Gui.cekajProzor(forma);
        Gui.naEdt(otvorena::dispose);
        Gui.sacekajEdt();
    }

    @Test
    public void meniOtvaraSveForme() {
        proveriMeni("miNoviClan", FormNoviClan.class);
        proveriMeni("miPretragaClana", FormPretragaClana.class);
        proveriMeni("miNoviTrener", FormNoviTrener.class);
        proveriMeni("miPretragaTrenera", FormPretragaTrenera.class);
        proveriMeni("miPretragaGrupe", FormPretragaGrupe.class);
    }

    @Test
    public void odjavaSeMozeOtkazati() {
        klikni("miOdjava");
        Gui.odgovoriNaPitanje(JOptionPane.NO_OPTION);
        Gui.sacekajEdt();

        assertTrue(forma.isDisplayable());
        assertEquals(0, server.brojZahteva(Operation.LOGOUT));
    }

    @Test
    public void zatvaranjePitanjaNeOdjavljuje() {
        klikni("miOdjava");
        Gui.odgovoriNaPitanje(JOptionPane.CLOSED_OPTION);
        Gui.sacekajEdt();

        assertTrue(forma.isDisplayable());
        assertEquals(0, server.brojZahteva(Operation.LOGOUT));
    }

    @Test
    public void odjavaVracaNaLoginFormu() {
        klikni("miOdjava");
        Gui.odgovoriNaPitanje(JOptionPane.YES_OPTION);

        LoginForma login = Gui.cekajProzor(LoginForma.class);
        Gui.sacekajEdt();
        assertEquals("Login forma", login.getTitle());
        assertFalse(forma.isDisplayable());
        assertNull(Session.getInstance().getUlogovani());
        assertEquals(Long.valueOf(1), ((Administrator) server.poslednji(Operation.LOGOUT)).getAdministratorID());
    }

    @Test
    public void greskaPriOdjaviOstavljaFormuOtvorenu() {
        server.greska(Operation.LOGOUT, "Server ne radi");

        klikni("miOdjava");
        Gui.odgovoriNaPitanje(JOptionPane.YES_OPTION);
        Gui.sacekajEdt();

        assertTrue(forma.isDisplayable());
        assertFalse(Gui.otvoren(LoginForma.class));
    }

    private void zatvoriProzor() {
        Gui.kasnije(() -> forma.dispatchEvent(new WindowEvent(forma, WindowEvent.WINDOW_CLOSING)));
    }

    @Test
    public void zatvaranjeProzoraPitaZaOdjavu() {
        zatvoriProzor();
        Gui.Poruka pitanje = Gui.cekajPoruku();
        assertEquals("Da li ste sigurni da zelite da se odjavite?", pitanje.getTekst());
        pitanje.odgovori(JOptionPane.NO_OPTION);
        Gui.sacekajEdt();
        assertFalse(zatvoren);

        zatvoriProzor();
        Gui.odgovoriNaPitanje(JOptionPane.YES_OPTION);
        Gui.sacekajEdt();
        assertTrue(zatvoren);
        assertEquals(1, server.brojZahteva(Operation.LOGOUT));
    }

    @Test
    public void greskaPriOdjaviPrilikomZatvaranjaNeGasiProgram() {
        server.greska(Operation.LOGOUT, "Server ne radi");

        zatvoriProzor();
        Gui.odgovoriNaPitanje(JOptionPane.YES_OPTION);
        Gui.sacekajEdt();

        assertFalse(zatvoren);
    }

    @Test
    public void kombinacijeSadrzeObjekteSaServera() {
        Object trener = Gui.<JComboBox<?>>polje(forma, "cmbTrener").getItemAt(0);
        assertTrue(trener instanceof Trener);
        assertEquals("Ivan Ristic", trener.toString());
    }

    @Test
    public void osvezavanjeBezIzabraneKategorijeNeUklanjaClanove() {
        dodajClana(0, "");
        Gui.naEdt(() -> Gui.<JComboBox<?>>polje(forma, "cmbKategorija").setSelectedIndex(-1));
        server.izmeniClana(1, 9, 2);

        Gui.naEdt(forma::osveziClanoveGrupe);

        assertFalse(Gui.imaOtvorenihPoruka());
        assertEquals(1, tabela().getRowCount());
    }
}
