package testutil;

import domain.Administrator;
import forme.MainForm;
import session.Session;

public final class TestKlijent {

    private TestKlijent() {
    }

    public static LazniServer server() {
        Gui.zaustaviPozadinskeNiti();
        LazniServer server = LazniServer.pokreni();
        server.resetuj();
        return server;
    }

    public static LazniServer pripremi() {
        LazniServer server = server();
        Session.getInstance().setUlogovani(new Administrator(1L, "Aleksandar", "Eic", "coa", "coa123"));
        return server;
    }

    public static MainForm glavnaForma() {
        return Gui.saEdt(MainForm::new);
    }
}
