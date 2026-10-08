package form.trener;

import forme.MainForm;
import javax.swing.JButton;
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

public class FormPretragaTreneraTest {

    private LazniServer server;
    private MainForm glavna;
    private FormPretragaTrenera forma;

    @Before
    public void otvori() {
        Gui.proveriEkran();
        server = TestKlijent.pripremi();
        glavna = TestKlijent.glavnaForma();
        forma = Gui.saEdt(() -> new FormPretragaTrenera(glavna, true));
    }

    @After
    public void zatvori() {
        Gui.zatvoriSve();
    }

    private JTable tabela() {
        return Gui.polje(forma, "tblTreneri");
    }

    @Test
    public void prikazujeSveTrenere() {
        assertEquals("Pretraga trenera", forma.getTitle());
        assertEquals(2, tabela().getRowCount());
    }

    @Test
    public void pretragaDokKorisnikKuca() {
        JTextField polje = Gui.polje(forma, "txtPretraga");
        Gui.naEdt(() -> polje.setText("Tamara"));

        Gui.naEdt(() -> Gui.pustiTaster(polje));

        assertEquals(1, tabela().getRowCount());
        assertEquals("Vukovic", tabela().getValueAt(0, 2));
    }

    @Test
    public void enterUPretraziNistaNeMenja() {
        JTextField polje = Gui.polje(forma, "txtPretraga");
        int pre = server.brojZahteva(Operation.GET_ALL_TRENER);

        Gui.naEdt(polje::postActionEvent);

        assertEquals(pre, server.brojZahteva(Operation.GET_ALL_TRENER));
    }

    @Test
    public void detaljiBezOznacenogTreneraNistaNeOtvaraju() {
        Gui.klikniISacekaj(Gui.<JButton>polje(forma, "btnDetalji"));

        assertFalse(Gui.otvoren(FormDetaljiTrenera.class));
    }

    @Test
    public void detaljiOtvarajuFormuTrenera() {
        Gui.naEdt(() -> tabela().setRowSelectionInterval(1, 1));

        Gui.klikni(Gui.<JButton>polje(forma, "btnDetalji"));

        FormDetaljiTrenera detalji = Gui.cekajProzor(FormDetaljiTrenera.class);
        assertEquals("Tamara", Gui.<JTextField>polje(detalji, "txtIme").getText());
        Gui.naEdt(detalji::dispose);
        Gui.sacekajEdt();
    }

    @Test
    public void osvezavanjeTabele() {
        int pre = server.brojZahteva(Operation.GET_ALL_TRENER);

        Gui.naEdt(forma::refreshTable);

        assertEquals(pre + 1, server.brojZahteva(Operation.GET_ALL_TRENER));
    }
}
