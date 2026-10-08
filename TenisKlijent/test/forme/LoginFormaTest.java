package forme;

import domain.Administrator;
import java.net.Socket;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import session.Session;
import testutil.Gui;
import testutil.LazniServer;
import testutil.TestKlijent;
import transfer.util.Operation;

public class LoginFormaTest {

    private LazniServer server;
    private LoginForma forma;
    private boolean zatvoren;

    @Before
    public void otvori() {
        Gui.proveriEkran();
        server = TestKlijent.pripremi();
        Session.getInstance().setUlogovani(null);
        zatvoren = false;
        forma = Gui.saEdt(() -> new LoginForma() {
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

    private void unesi(String korisnik, String lozinka) {
        Gui.naEdt(() -> {
            Gui.<JTextField>polje(forma, "txtUsername").setText(korisnik);
            Gui.<JPasswordField>polje(forma, "txtPassword").setText(lozinka);
        });
    }

    private JButton dugme(String ime) {
        return Gui.polje(forma, ime);
    }

    @Test
    public void pocetnoStanjeForme() {
        assertEquals("Login forma", forma.getTitle());
        assertEquals("coa", Gui.<JTextField>polje(forma, "txtUsername").getText());
    }

    @Test
    public void praznoKorisnickoImeNijeDozvoljeno() {
        unesi("", "coa123");

        Gui.klikni(dugme("btnLogin"));

        assertEquals("Korisnicko ime i lozinka moraju biti popunjeni!", Gui.zatvoriPoruku());
        assertEquals(0, server.brojZahteva(Operation.LOGIN));
    }

    @Test
    public void praznaLozinkaNijeDozvoljena() {
        unesi("coa", "");

        Gui.klikni(dugme("btnLogin"));

        assertEquals("Korisnicko ime i lozinka moraju biti popunjeni!", Gui.zatvoriPoruku());
        assertEquals(0, server.brojZahteva(Operation.LOGIN));
    }

    @Test
    public void pogresnaLozinkaPrikazujeGreskuSaServera() {
        unesi("coa", "pogresna");

        Gui.klikni(dugme("btnLogin"));

        Gui.Poruka p = Gui.cekajPoruku();
        assertEquals("Ne postoji administrator sa tim kredencijalima.", p.getTekst());
        assertEquals("Greska", p.getNaslov());
        assertEquals(JOptionPane.ERROR_MESSAGE, p.getTip());
        p.odgovori(JOptionPane.OK_OPTION);
        Gui.sacekajEdt();
        assertTrue(forma.isDisplayable());
        assertNull(Session.getInstance().getUlogovani());
    }

    @Test
    public void uspesnaPrijavaOtvaraGlavnuFormu() {
        unesi("coa", "coa123");

        Gui.klikni(dugme("btnLogin"));

        assertEquals("Uspesno ste se prijavili na sistem", Gui.zatvoriPoruku());
        MainForm glavna = Gui.cekajProzor(MainForm.class);
        Gui.sacekajEdt();
        assertEquals("Klijentska forma", glavna.getTitle());
        assertFalse(forma.isDisplayable());
        Administrator ulogovani = Session.getInstance().getUlogovani();
        assertEquals("Aleksandar", ulogovani.getIme());
        Administrator poslat = (Administrator) server.poslednji(Operation.LOGIN);
        assertEquals("coa", poslat.getUsername());
        assertEquals("coa123", poslat.getPassword());
        assertEquals("", poslat.getIme());
        assertEquals("", poslat.getPrezime());
    }

    @Test
    public void bezServeraPrijavljujeGreskuIZatvaraProgram() {
        Socket veza = Gui.polje(Session.getInstance(), "socket");
        Gui.postaviPolje(Session.getInstance(), "socket", null);
        try {
            Gui.klikni(dugme("btnLogin"));

            assertEquals("Server nije pokrenut!", Gui.zatvoriPoruku());
            Gui.sacekajEdt();
            assertTrue(zatvoren);
        } finally {
            Gui.postaviPolje(Session.getInstance(), "socket", veza);
        }
    }

    @Test
    public void otkaziZatvaraProgram() {
        Gui.klikniISacekaj(dugme("btnOtkazi"));

        assertTrue(zatvoren);
    }

    @Test
    public void pokretanjeProgramaOtvaraLoginFormu() {
        LoginForma.main(new String[0]);

        LoginForma otvorena = Gui.cekajProzor(LoginForma.class);
        assertEquals("Login forma", otvorena.getTitle());
    }
}
