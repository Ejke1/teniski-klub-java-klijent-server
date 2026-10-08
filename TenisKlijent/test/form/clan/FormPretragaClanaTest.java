package form.clan;

import forme.MainForm;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import static org.junit.Assert.assertEquals;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import testutil.Gui;
import testutil.LazniServer;
import testutil.TestKlijent;
import transfer.util.Operation;

public class FormPretragaClanaTest {

    private LazniServer server;
    private MainForm glavna;
    private FormPretragaClana forma;

    @Before
    public void otvori() {
        Gui.proveriEkran();
        server = TestKlijent.pripremi();
        glavna = TestKlijent.glavnaForma();
        forma = Gui.saEdt(() -> new FormPretragaClana(glavna, true));
    }

    @After
    public void zatvori() {
        Gui.zatvoriSve();
    }

    private JTable tabela() {
        return Gui.polje(forma, "tblClanovi");
    }

    private void kucaj(String tekst) {
        JTextField polje = Gui.polje(forma, "txtIme");
        Gui.naEdt(() -> polje.setText(tekst));
        Gui.kasnije(() -> Gui.pustiTaster(polje));
    }

    @Test
    public void prikazujeSveClanove() {
        assertEquals("Pretraga clana", forma.getTitle());
        assertEquals(5, tabela().getRowCount());
    }

    @Test
    public void pretragaDokKorisnikKuca() {
        kucaj("Mina");

        assertEquals("Sistem je nasao clanove po zadatoj vrednosti!", Gui.zatvoriPoruku());
        Gui.sacekajEdt();
        assertEquals(1, tabela().getRowCount());
        assertEquals("Popovic", tabela().getValueAt(0, 3));
    }

    @Test
    public void pretragaBezRezultata() {
        kucaj("nepostojeci");

        Gui.Poruka p = Gui.cekajPoruku();
        assertEquals("Sistem ne moze da nadje clana po zadatoj vrednosti!", p.getTekst());
        assertEquals("Greska", p.getNaslov());
        p.odgovori(JOptionPane.OK_OPTION);
    }

    @Test
    public void detaljiBezOznacenogClana() {
        Gui.klikni(Gui.<JButton>polje(forma, "btnDetalji"));

        Gui.Poruka p = Gui.cekajPoruku();
        assertEquals("Sistem ne moze da ucita clana", p.getTekst());
        assertEquals("Greska", p.getNaslov());
        p.odgovori(JOptionPane.OK_OPTION);
    }

    @Test
    public void detaljiOtvarajuFormuSaPodacimaClana() {
        Gui.naEdt(() -> tabela().setRowSelectionInterval(2, 2));

        Gui.klikni(Gui.<JButton>polje(forma, "btnDetalji"));

        assertEquals("Sistem je ucitao clana!", Gui.zatvoriPoruku());
        FormDetaljiClana detalji = Gui.cekajProzor(FormDetaljiClana.class);
        assertEquals("Petar", Gui.<JTextField>polje(detalji, "txtIme").getText());
        Gui.naEdt(detalji::dispose);
        Gui.sacekajEdt();
    }

    @Test
    public void osvezavanjeTabeleUcitavaNovePodatke() {
        server.izmeniClana(1, 15, 1);
        int pre = server.brojZahteva(Operation.GET_ALL_CLAN);

        Gui.naEdt(forma::refreshTable);

        assertEquals(pre + 1, server.brojZahteva(Operation.GET_ALL_CLAN));
        assertEquals(15, tabela().getValueAt(0, 6));
    }
}
