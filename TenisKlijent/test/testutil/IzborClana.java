package testutil;

import form.clan.FormIzborClana;
import javax.swing.JButton;
import javax.swing.JTable;

public final class IzborClana {

    private IzborClana() {
    }

    public static int brojPonudjenih() {
        FormIzborClana f = Gui.cekajProzor(FormIzborClana.class);
        JTable t = Gui.polje(f, "tblClanovi");
        return Gui.saEdt(t::getRowCount);
    }

    public static void izaberi(int red) {
        FormIzborClana f = Gui.cekajProzor(FormIzborClana.class);
        JTable t = Gui.polje(f, "tblClanovi");
        Gui.naEdt(() -> t.setRowSelectionInterval(red, red));
        JButton izaberi = Gui.polje(f, "btnIzaberi");
        Gui.naEdt(izaberi::doClick);
        Gui.cekajDaSeZatvori(f);
    }

    public static void otkazi() {
        FormIzborClana f = Gui.cekajProzor(FormIzborClana.class);
        JButton otkazi = Gui.polje(f, "btnOtkazi");
        Gui.naEdt(otkazi::doClick);
        Gui.cekajDaSeZatvori(f);
    }
}
