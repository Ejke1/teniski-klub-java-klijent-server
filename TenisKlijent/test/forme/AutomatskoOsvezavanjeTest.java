package forme;

import domain.ClanGrupe;
import form.grupa.FormDetaljiGrupe;
import form.grupa.FormPretragaGrupe;
import javax.swing.JTable;
import models.TableModelClanovi;
import models.TableModelClanoviGrupe;
import models.TableModelGrupe;
import models.TableModelTreneri;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.After;
import org.junit.Test;
import testutil.Gui;
import testutil.LazniServer;
import testutil.TestKlijent;
import transfer.util.Operation;

public class AutomatskoOsvezavanjeTest {

    @After
    public void zatvori() {
        Gui.zatvoriSve();
    }

    @Test
    public void tabeleIFormeSeSameOsvezavajuSaServera() throws Exception {
        Gui.proveriEkran();
        LazniServer server = TestKlijent.pripremi();
        TableModelClanovi clanovi = new TableModelClanovi();
        TableModelTreneri treneri = new TableModelTreneri();
        TableModelGrupe grupe = new TableModelGrupe();
        Thread[] niti = {new Thread(clanovi), new Thread(treneri), new Thread(grupe)};
        for (Thread n : niti) {
            n.start();
        }
        MainForm glavna = TestKlijent.glavnaForma();
        TableModelClanoviGrupe uGlavnoj = (TableModelClanoviGrupe) Gui.<JTable>polje(glavna, "tblClanovi").getModel();
        Gui.naEdt(() -> uGlavnoj.dodajClana(new ClanGrupe(null, -1, "/", server.clan(1))));
        FormPretragaGrupe pretraga = Gui.saEdt(() -> new FormPretragaGrupe(glavna, true));
        FormDetaljiGrupe detalji = Gui.saEdt(() -> new FormDetaljiGrupe(pretraga, true, server.grupa(1)));
        TableModelClanoviGrupe uDetaljima = (TableModelClanoviGrupe) Gui.<JTable>polje(detalji, "tblClanovi").getModel();
        int trenera = server.brojZahteva(Operation.GET_ALL_TRENER);
        int grupa = server.brojZahteva(Operation.GET_ALL_GRUPA);

        server.izmeniClana(1, 14, 1);
        Gui.spavaj(10800);

        try {
            assertEquals(14, clanovi.getValueAt(0, 6));
            assertTrue(server.brojZahteva(Operation.GET_ALL_TRENER) > trenera);
            assertTrue(server.brojZahteva(Operation.GET_ALL_GRUPA) > grupa);
            assertEquals(14, Gui.saEdt(() -> uGlavnoj.getValueAt(0, 2)));
            assertEquals(14, Gui.saEdt(() -> uDetaljima.getValueAt(0, 2)));
        } finally {
            for (Thread n : niti) {
                n.interrupt();
            }
        }
    }
}
