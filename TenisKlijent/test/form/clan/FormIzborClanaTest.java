package form.clan;

import domain.Clan;
import domain.Kategorija;
import java.awt.event.MouseEvent;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import testutil.Gui;
import testutil.LazniServer;
import testutil.TestKlijent;
import transfer.util.Operation;

public class FormIzborClanaTest {

    private LazniServer server;
    private FormIzborClana forma;

    @Before
    public void otvori() {
        Gui.proveriEkran();
        server = TestKlijent.pripremi();
        forma = Gui.saEdt(() -> new FormIzborClana(null, true, server.kategorija(1)));
    }

    @After
    public void zatvori() {
        Gui.zatvoriSve();
    }

    private JTable tabela() {
        return Gui.polje(forma, "tblClanovi");
    }

    private void pretrazi(String tekst) {
        Gui.naEdt(() -> Gui.<JTextField>polje(forma, "txtPretraga").setText(tekst));
        Gui.klikni(Gui.<JButton>polje(forma, "btnPretrazi"));
    }

    @Test
    public void prikazujeSamoClanoveKategorije() {
        assertEquals("Izbor clana - kategorija: Pocetni", forma.getTitle());
        assertEquals("Pocetni", Gui.<JLabel>polje(forma, "lblKategorija").getText());
        assertEquals(3, tabela().getRowCount());
        Clan kriterijum = (Clan) server.poslednji(Operation.GET_ALL_CLAN);
        assertEquals(Long.valueOf(1), kriterijum.getKategorija().getKategorijaID());
    }

    @Test
    public void odbacujeClanoveDrugihKategorijaIakoIhServerVrati() {
        server.ignorisiKategorijuPriPretrazi();
        server.dodajClanaBezKategorije(new Clan(50L, "Bez", "Kategorije", 10, "bez@test.rs", "0640000000", null));

        FormIzborClana f = Gui.saEdt(() -> new FormIzborClana(null, true, server.kategorija(1)));

        assertEquals(3, Gui.<JTable>polje(f, "tblClanovi").getRowCount());
    }

    @Test
    public void pretragaPoTekstu() {
        pretrazi("LANA");
        Gui.sacekajEdt();

        assertEquals(1, tabela().getRowCount());
        assertEquals("lana", ((Clan) server.poslednji(Operation.GET_ALL_CLAN)).getIme());
    }

    @Test
    public void pretragaEnteromUPolju() {
        JTextField polje = Gui.polje(forma, "txtPretraga");
        Gui.naEdt(() -> polje.setText("vasic"));

        Gui.naEdt(polje::postActionEvent);

        assertEquals(1, tabela().getRowCount());
    }

    @Test
    public void pretragaBezRezultata() {
        pretrazi("nepostojeci");

        Gui.Poruka p = Gui.cekajPoruku();
        assertEquals("Sistem ne moze da nadje clanove po zadatoj vrednosti!", p.getTekst());
        assertEquals("Greska", p.getNaslov());
        p.odgovori(JOptionPane.OK_OPTION);
        assertEquals(0, tabela().getRowCount());
    }

    @Test
    public void izborBezOznacenogReda() {
        Gui.klikni(Gui.<JButton>polje(forma, "btnIzaberi"));

        assertEquals("Izaberite clana iz tabele!", Gui.zatvoriPoruku());
        assertNull(forma.getIzabraniClan());
        assertTrue(forma.isDisplayable());
    }

    @Test
    public void izborOznacenogClana() {
        Gui.naEdt(() -> tabela().setRowSelectionInterval(1, 1));

        Gui.klikniISacekaj(Gui.<JButton>polje(forma, "btnIzaberi"));

        assertEquals("Uros", forma.getIzabraniClan().getIme());
        assertFalse(forma.isDisplayable());
    }

    private void klikniMisem(int brojKlikova) {
        JTable t = tabela();
        Gui.naEdt(() -> t.dispatchEvent(new MouseEvent(t, MouseEvent.MOUSE_CLICKED, System.currentTimeMillis(),
                0, 5, 5, brojKlikova, false, MouseEvent.BUTTON1)));
    }

    @Test
    public void dvoklikBiraClana() {
        Gui.naEdt(() -> tabela().setRowSelectionInterval(0, 0));

        klikniMisem(2);

        assertEquals("Lana", forma.getIzabraniClan().getIme());
        assertFalse(forma.isDisplayable());
    }

    @Test
    public void jedanKlikNeBiraClana() {
        Gui.naEdt(() -> tabela().setRowSelectionInterval(0, 0));

        klikniMisem(1);

        assertNull(forma.getIzabraniClan());
        assertTrue(forma.isDisplayable());
    }

    @Test
    public void dvoklikBezOznacenogRedaNistaNeRadi() {
        klikniMisem(2);

        assertNull(forma.getIzabraniClan());
        assertTrue(forma.isDisplayable());
    }

    @Test
    public void otkazivanje() {
        Gui.naEdt(() -> tabela().setRowSelectionInterval(0, 0));

        Gui.klikniISacekaj(Gui.<JButton>polje(forma, "btnOtkazi"));

        assertNull(forma.getIzabraniClan());
        assertFalse(forma.isDisplayable());
    }

    @Test
    public void greskaPriUcitavanjuClanova() {
        server.greska(Operation.GET_ALL_CLAN, "Server ne radi");
        AtomicReference<FormIzborClana> f = new AtomicReference<>();

        Gui.kasnije(() -> f.set(new FormIzborClana(null, true, new Kategorija(1L, "Pocetni", ""))));

        Gui.Poruka p = Gui.cekajPoruku();
        assertEquals("Sistem ne moze da ucita clanove: Server ne radi", p.getTekst());
        assertEquals("Greska", p.getNaslov());
        p.odgovori(JOptionPane.OK_OPTION);
        Gui.sacekajEdt();
        assertEquals(0, Gui.<JTable>polje(f.get(), "tblClanovi").getRowCount());
    }

    @Test
    public void mozeSeOtvoritiIBezBlokiranja() {
        FormIzborClana f = Gui.saEdt(() -> new FormIzborClana(null, false, server.kategorija(2)));

        assertEquals(java.awt.Dialog.ModalityType.MODELESS, f.getModalityType());
        assertEquals(2, Gui.<JTable>polje(f, "tblClanovi").getRowCount());
    }
}
