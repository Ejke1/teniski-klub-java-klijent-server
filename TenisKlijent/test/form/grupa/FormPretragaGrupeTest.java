package form.grupa;

import forme.MainForm;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import testutil.Gui;
import testutil.LazniServer;
import testutil.TestKlijent;
import transfer.util.Operation;

public class FormPretragaGrupeTest {

    private LazniServer server;
    private MainForm glavna;
    private FormPretragaGrupe forma;

    @Before
    public void otvori() {
        Gui.proveriEkran();
        server = TestKlijent.pripremi();
        glavna = TestKlijent.glavnaForma();
        forma = Gui.saEdt(() -> new FormPretragaGrupe(glavna, true));
    }

    @After
    public void zatvori() {
        Gui.zatvoriSve();
    }

    private JTable tabela() {
        return Gui.polje(forma, "tblGrupe");
    }

    private void pretrazi(String tekst) {
        Gui.naEdt(() -> Gui.<JTextField>polje(forma, "txtPretraga").setText(tekst));
        Gui.klikni(Gui.<JButton>polje(forma, "btnPretraga"));
    }

    @Test
    public void prikazujeSveGrupe() {
        assertEquals("Pretraga grupe", forma.getTitle());
        assertEquals(2, tabela().getRowCount());
    }

    @Test
    public void pretragaPronalaziGrupu() {
        pretrazi("Mini");

        assertEquals("Sistem je nasao grupe po zadatoj vrednosti!", Gui.zatvoriPoruku());
        Gui.sacekajEdt();
        assertEquals(1, tabela().getRowCount());
        assertEquals("Mini Tenis A", tabela().getValueAt(0, 1));
    }

    @Test
    public void pretragaBezRezultata() {
        pretrazi("nepostojeca");

        Gui.Poruka p = Gui.cekajPoruku();
        assertEquals("Sistem ne moze da nadje grupu po zadatoj vrednosti!", p.getTekst());
        assertEquals("Greska", p.getNaslov());
        p.odgovori(JOptionPane.OK_OPTION);
    }

    @Test
    public void detaljiBezOznaceneGrupeNistaNeOtvaraju() {
        Gui.klikniISacekaj(Gui.<JButton>polje(forma, "btnDetalji"));

        assertFalse(Gui.imaOtvorenihPoruka());
        assertFalse(Gui.otvoren(FormDetaljiGrupe.class));
    }

    @Test
    public void detaljiOtvarajuFormuGrupe() {
        Gui.naEdt(() -> tabela().setRowSelectionInterval(1, 1));

        Gui.klikni(Gui.<JButton>polje(forma, "btnDetalji"));

        assertEquals("Sistem je ucitao grupu", Gui.zatvoriPoruku());
        FormDetaljiGrupe detalji = Gui.cekajProzor(FormDetaljiGrupe.class);
        assertEquals("Srednji Nivo A", Gui.<JTextField>polje(detalji, "txtNaziv").getText());
        Gui.naEdt(detalji::dispose);
        Gui.sacekajEdt();
    }

    @Test
    public void osvezavanjeTabele() {
        int pre = server.brojZahteva(Operation.GET_ALL_GRUPA);

        Gui.naEdt(forma::refreshTable);

        assertEquals(pre + 1, server.brojZahteva(Operation.GET_ALL_GRUPA));
    }
}
