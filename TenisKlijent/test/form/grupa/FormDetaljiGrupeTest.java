package form.grupa;

import domain.Grupa;
import forme.MainForm;
import javax.swing.AbstractButton;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import models.TableModelClanoviGrupe;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import testutil.Gui;
import testutil.IzborClana;
import testutil.LazniServer;
import testutil.TestKlijent;
import transfer.util.Operation;

public class FormDetaljiGrupeTest {

    private LazniServer server;
    private MainForm glavna;
    private FormPretragaGrupe pretraga;
    private FormDetaljiGrupe forma;

    @Before
    public void otvori() {
        Gui.proveriEkran();
        server = TestKlijent.pripremi();
        glavna = TestKlijent.glavnaForma();
        pretraga = Gui.saEdt(() -> new FormPretragaGrupe(glavna, true));
        forma = detalji(server.grupa(1));
    }

    @After
    public void zatvori() {
        Gui.zatvoriSve();
    }

    private FormDetaljiGrupe detalji(Grupa g) {
        return Gui.saEdt(() -> new FormDetaljiGrupe(pretraga, true, g));
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

    private void oznaciRed(int red) {
        JTable t = k("tblClanovi");
        Gui.naEdt(() -> t.setRowSelectionInterval(red, red));
    }

    private void izaberiPrekoLupe(int red) {
        klikni("btnPretraga");
        IzborClana.izaberi(red);
        Gui.sacekajEdt();
    }

    private String txtClan() {
        return Gui.<JTextField>polje(forma, "txtClan").getText();
    }

    @Test
    public void prikazujePodatkeGrupe() {
        assertEquals("Detalji grupe", forma.getTitle());
        assertEquals("Mini Tenis A", Gui.<JTextField>polje(forma, "txtNaziv").getText());
        assertFalse(Gui.<JTextField>polje(forma, "txtNaziv").isEditable());
        assertFalse(Gui.<JComboBox<?>>polje(forma, "cmbKategorija").isEnabled());
        assertEquals("Pocetni", Gui.<JComboBox<?>>polje(forma, "cmbKategorija").getSelectedItem().toString());
        assertEquals("10", Gui.<JFormattedTextField>polje(forma, "txtMaxKapacitet").getText());
        assertEquals("Pocetnici.", Gui.<JTextArea>polje(forma, "txtOpis").getText());
        assertEquals("Ivan Ristic", Gui.<JComboBox<?>>polje(forma, "cmbTrener").getSelectedItem().toString());
        assertEquals(2, Gui.<JComboBox<?>>polje(forma, "cmbTrener").getItemCount());
        assertEquals(2, tabela().getRowCount());
        assertEquals("Uros Milosevic", tabela().getValueAt(1, 1));
    }

    @Test
    public void grupaBezClanovaImaPraznuTabelu() {
        Grupa g = server.grupa(1);
        g.setClanoviGrupe(null);

        FormDetaljiGrupe f = detalji(g);

        assertEquals(0, ((TableModelClanoviGrupe) Gui.<JTable>polje(f, "tblClanovi").getModel()).getRowCount());
    }

    @Test
    public void lupaNudiClanoveKategorijeGrupe() {
        klikni("btnPretraga");

        assertEquals(3, IzborClana.brojPonudjenih());
        IzborClana.izaberi(2);
        Gui.sacekajEdt();

        assertEquals("Petar Vasic (petar@test.rs)", txtClan());
    }

    @Test
    public void otkazanIzborNeMenjaClana() {
        klikni("btnPretraga");
        IzborClana.otkazi();
        Gui.sacekajEdt();

        assertEquals("", txtClan());
    }

    @Test
    public void dodavanjeClana() {
        assertEquals("Izaberite clana klikom na lupu!", klikniIPorukom("btnDodajClana"));

        izaberiPrekoLupe(2);
        Gui.naEdt(() -> Gui.<JTextArea>polje(forma, "txtNapomena").setText("Novi clan"));
        Gui.klikniISacekaj(k("btnDodajClana"));

        assertEquals(3, tabela().getRowCount());
        assertEquals(3, tabela().getValueAt(2, 0));
        assertEquals("Novi clan", tabela().getStavka(2).getNapomena());
        assertEquals("", txtClan());

        izaberiPrekoLupe(0);
        assertEquals("Vec ste uneli ovog clana!", klikniIPorukom("btnDodajClana"));
    }

    @Test
    public void oznacavanjeRedaUcitavaClanaINapomenu() {
        oznaciRed(1);

        assertEquals("Uros Milosevic (uros@test.rs)", txtClan());
        assertEquals("Ne moze petkom.", Gui.<JTextArea>polje(forma, "txtNapomena").getText());

        oznaciRed(0);
        assertEquals("", Gui.<JTextArea>polje(forma, "txtNapomena").getText());
    }

    @Test
    public void zamenaClana() {
        assertEquals("Oznacite u tabeli clana kojeg menjate!", klikniIPorukom("btnIzmeniClana"));

        oznaciRed(0);
        klikni("btnIzmeniClana");
        IzborClana.izaberi(2);
        assertEquals("Clan u stavci 1 je izmenjen.", Gui.zatvoriPoruku());
        assertEquals("Petar Vasic", tabela().getValueAt(0, 1));

        oznaciRed(0);
        klikni("btnIzmeniClana");
        IzborClana.izaberi(1);
        assertEquals("Ovaj clan je vec u grupi!", Gui.zatvoriPoruku());

        oznaciRed(0);
        klikni("btnIzmeniClana");
        IzborClana.otkazi();
        Gui.sacekajEdt();
        assertEquals("Petar Vasic", tabela().getValueAt(0, 1));
    }

    @Test
    public void brisanjeStavke() {
        assertEquals("Izaberite stavku u tabeli koju brisete!", klikniIPorukom("btnObrisiClana"));

        oznaciRed(0);
        Gui.klikniISacekaj(k("btnObrisiClana"));

        assertEquals(1, tabela().getRowCount());
        assertEquals("Uros Milosevic", tabela().getValueAt(0, 1));
        assertEquals(1, tabela().getValueAt(0, 0));
    }

    @Test
    public void izmenaGrupeSaljeSvePodatke() {
        izaberiPrekoLupe(2);
        Gui.klikniISacekaj(k("btnDodajClana"));
        Gui.naEdt(() -> {
            Gui.<JComboBox<?>>polje(forma, "cmbTrener").setSelectedIndex(1);
            Gui.<JTextArea>polje(forma, "txtOpis").setText("Novi opis");
            Gui.<JFormattedTextField>polje(forma, "txtMaxKapacitet").setText("8");
        });
        int ucitavanja = server.brojZahteva(Operation.GET_ALL_GRUPA);

        klikni("btnIzmeni");

        assertEquals("Sistem je izmenio grupu\n\nMail sa PDF spiskom clanova je poslat na 3 od 3 clanova grupe.",
                Gui.zatvoriPoruku());
        Gui.sacekajEdt();
        Grupa poslata = (Grupa) server.poslednji(Operation.UPDATE_GRUPA);
        assertEquals(Long.valueOf(1), poslata.getGrupaID());
        assertEquals("Novi opis", poslata.getOpis());
        assertEquals(8, poslata.getMaxKapacitet());
        assertEquals(3, poslata.getBrojClanova());
        assertEquals("Tamara", poslata.getTrener().getIme());
        assertEquals(3, poslata.getClanoviGrupe().size());
        assertEquals("Petar", poslata.getClanoviGrupe().get(2).getClan().getIme());
        assertTrue(server.brojZahteva(Operation.GET_ALL_GRUPA) > ucitavanja);
        assertFalse(forma.isDisplayable());
    }

    @Test
    public void greskaPriIzmeniGrupe() {
        server.greska(Operation.UPDATE_GRUPA, "Morate uneti barem 2 clana!");

        klikni("btnIzmeni");

        Gui.Poruka p = Gui.cekajPoruku();
        assertEquals("Morate uneti barem 2 clana!", p.getTekst());
        assertEquals("Sistem ne moze da izmeni grupu", p.getNaslov());
        p.odgovori(JOptionPane.OK_OPTION);
        Gui.sacekajEdt();
        assertTrue(forma.isDisplayable());
    }

    @Test
    public void neispravanKapacitet() {
        Gui.naEdt(() -> Gui.<JFormattedTextField>polje(forma, "txtMaxKapacitet").setText(""));

        klikni("btnIzmeni");

        Gui.Poruka p = Gui.cekajPoruku();
        assertEquals("For input string: \"\"", p.getTekst());
        assertEquals("Sistem ne moze da izmeni grupu", p.getNaslov());
        p.odgovori(JOptionPane.OK_OPTION);
        assertEquals(0, server.brojZahteva(Operation.UPDATE_GRUPA));
    }

    private void obrisiGrupu(int odgovor) {
        klikni("btnObrisi");
        Gui.Poruka pitanje = Gui.cekajPoruku();
        assertEquals("Da li ste sigurni da zelite da obrisete ovu grupu?", pitanje.getTekst());
        pitanje.odgovori(odgovor);
    }

    @Test
    public void brisanjeGrupeSeMozeOtkazati() {
        obrisiGrupu(JOptionPane.NO_OPTION);
        Gui.sacekajEdt();
        obrisiGrupu(JOptionPane.CLOSED_OPTION);
        Gui.sacekajEdt();

        assertTrue(forma.isDisplayable());
        assertEquals(0, server.brojZahteva(Operation.DELETE_GRUPA));
    }

    @Test
    public void uspesnoBrisanjeGrupe() {
        obrisiGrupu(JOptionPane.YES_OPTION);

        assertEquals("Sistem je obrisao grupu", Gui.zatvoriPoruku());
        Gui.sacekajEdt();
        assertEquals(1, server.brojGrupa());
        assertFalse(forma.isDisplayable());
    }

    @Test
    public void greskaPriBrisanjuGrupe() {
        server.greska(Operation.DELETE_GRUPA, "Server ne radi");

        obrisiGrupu(JOptionPane.YES_OPTION);

        Gui.Poruka p = Gui.cekajPoruku();
        assertEquals("Server ne radi", p.getTekst());
        assertEquals("Sistem ne moze da obrise grupu", p.getNaslov());
        p.odgovori(JOptionPane.OK_OPTION);
        Gui.sacekajEdt();
        assertEquals(2, server.brojGrupa());
    }

    @Test
    public void osvezavanjeUklanjaClanaKojiViseNijeUKategoriji() {
        oznaciRed(0);
        server.izmeniClana(2, 10, 2);
        server.izmeniClana(1, 12, 1);

        Gui.kasnije(forma::osveziClanoveGrupe);

        Gui.Poruka p = Gui.cekajPoruku();
        assertEquals("Sledeci clanovi su uklonjeni iz grupe jer su obrisani ili vise ne pripadaju kategoriji grupe:\n"
                + "Uros Milosevic", p.getTekst());
        p.odgovori(JOptionPane.OK_OPTION);
        Gui.sacekajEdt();
        assertEquals(1, tabela().getRowCount());
        assertEquals(12, tabela().getValueAt(0, 2));
        assertEquals("Lana Stojanovic (lana@test.rs)", txtClan());
    }

    @Test
    public void osvezavanjeBezPromenaIPrazneTabele() {
        izaberiPrekoLupe(2);
        Gui.naEdt(forma::osveziClanoveGrupe);
        assertFalse(Gui.imaOtvorenihPoruka());
        assertEquals("Petar Vasic (petar@test.rs)", txtClan());

        Gui.naEdt(() -> tabela().ocisti());
        int pre = server.brojZahteva(Operation.GET_ALL_CLAN);
        Gui.naEdt(forma::osveziClanoveGrupe);
        assertEquals(pre, server.brojZahteva(Operation.GET_ALL_CLAN));
    }

    @Test
    public void greskeSaServeraPriOsvezavanjuSeSamoBeleze() {
        server.greska(Operation.GET_ALL_CLAN, "Server ne radi");
        Gui.naEdt(forma::osveziClanoveGrupe);

        server.greska(Operation.GET_ALL_TRENER, "Server ne radi");
        Gui.naEdt(forma::popuniTrenere);

        assertFalse(Gui.imaOtvorenihPoruka());
        assertEquals(2, tabela().getRowCount());
        assertEquals(2, Gui.<JComboBox<?>>polje(forma, "cmbTrener").getItemCount());
    }

    @Test
    public void zatvaranjeForme() {
        Gui.klikniISacekaj(k("btnZatvori"));

        assertFalse(forma.isDisplayable());
    }
}
