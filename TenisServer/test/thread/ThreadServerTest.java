package thread;

import controller.ServerController;
import domain.Administrator;
import domain.Clan;
import domain.Grupa;
import domain.Kategorija;
import domain.Trener;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import testutil.Gui;
import testutil.HvatacLogova;
import testutil.TestBaza;
import transfer.Request;
import transfer.Response;
import transfer.util.Operation;
import transfer.util.ResponseStatus;

public class ThreadServerTest {

    private ThreadServer server;
    private Socket klijent;

    @Before
    public void pokreniServer() throws Exception {
        TestBaza.pripremi();
        ServerController.getInstance().getUlogovaniAdministratori().clear();
        server = new ThreadServer(0);
        server.start();
        klijent = poveziSe();
    }

    @After
    public void ugasiServer() throws Exception {
        klijent.close();
        server.getServerSocket().close();
        server.join(5000);
        ServerController.getInstance().getUlogovaniAdministratori().clear();
    }

    private Socket poveziSe() throws Exception {
        Socket s = new Socket(InetAddress.getLoopbackAddress(), server.getServerSocket().getLocalPort());
        s.setSoTimeout(20000);
        return s;
    }

    private static Response posalji(Socket s, int operacija, Object podaci) throws Exception {
        ObjectOutputStream out = new ObjectOutputStream(s.getOutputStream());
        out.writeObject(new Request(operacija, podaci));
        out.flush();
        ObjectInputStream in = new ObjectInputStream(s.getInputStream());
        return (Response) in.readObject();
    }

    private Response posalji(int operacija, Object podaci) throws Exception {
        return posalji(klijent, operacija, podaci);
    }

    private static void uspesno(Response r) {
        assertEquals(r.getException() == null ? "" : r.getException().getMessage(), ResponseStatus.Success, r.getResponseStatus());
    }

    @Test
    public void podrazumevaniKonstruktorCitaPortIzPodesavanja() {
        String prethodni = System.getProperty("server.port");
        System.setProperty("server.port", "0");
        try {
            ThreadServer s = new ThreadServer();
            assertTrue(s.getServerSocket().isBound());
            s.getServerSocket().close();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        } finally {
            if (prethodni == null) {
                System.clearProperty("server.port");
            } else {
                System.setProperty("server.port", prethodni);
            }
        }
    }

    @Test
    public void prijavaIOdjavaPrekoMreze() throws Exception {
        Administrator a = new Administrator();
        a.setUsername("coa");
        a.setPassword("coa123");

        Response prijava = posalji(Operation.LOGIN, a);
        uspesno(prijava);
        Administrator ulogovani = (Administrator) prijava.getData();
        assertEquals("Aleksandar", ulogovani.getIme());
        assertEquals(1, ServerController.getInstance().getUlogovaniAdministratori().size());

        uspesno(posalji(Operation.LOGOUT, ulogovani));
        assertTrue(ServerController.getInstance().getUlogovaniAdministratori().isEmpty());
    }

    @Test
    public void greskaSeVracaKlijentuKaoOdgovorSaGreskom() throws Exception {
        Administrator a = new Administrator();
        a.setUsername("coa");
        a.setPassword("pogresna");

        Response r = posalji(Operation.LOGIN, a);

        assertEquals(ResponseStatus.Error, r.getResponseStatus());
        assertEquals("Ne postoji administrator sa tim kredencijalima.", r.getException().getMessage());
        assertNull(r.getData());
    }

    @Test
    public void sveOperacijeNadClanovima() throws Exception {
        Clan nov = new Clan(null, "Jovana", "Peric", 11, "jovana.peric@test.rs", "0641234567", TestBaza.kategorija(1));
        uspesno(posalji(Operation.ADD_CLAN, nov));
        Long id = Long.valueOf(TestBaza.vrednost("SELECT ClanID FROM Clan WHERE Email = 'jovana.peric@test.rs'"));

        Clan izmenjen = new Clan(id, "Jovana", "Peric", 12, "jovana.peric@test.rs", "0641234567", TestBaza.kategorija(1));
        uspesno(posalji(Operation.UPDATE_CLAN, izmenjen));
        assertEquals("12", TestBaza.vrednost("SELECT Godine FROM Clan WHERE ClanID = " + id));

        Response sviClanovi = posalji(Operation.GET_ALL_CLAN, new Clan(null, "", "", 0, "", "", null));
        uspesno(sviClanovi);
        assertEquals(7, ((ArrayList<?>) sviClanovi.getData()).size());

        uspesno(posalji(Operation.DELETE_CLAN, izmenjen));
        assertEquals(6, TestBaza.broj("SELECT COUNT(*) FROM Clan"));

        Clan neispravan = new Clan(null, "X", "Y", 11, "nije-mejl", "0641234567", TestBaza.kategorija(1));
        Response greska = posalji(Operation.ADD_CLAN, neispravan);
        assertEquals(ResponseStatus.Error, greska.getResponseStatus());
        assertEquals("Email nije u ispravnom formatu!", greska.getException().getMessage());
    }

    @Test
    public void sveOperacijeNadTrenerima() throws Exception {
        uspesno(posalji(Operation.ADD_TRENER, new Trener(null, "Milan", "Jovic", "0603030303", 7)));
        Long id = Long.valueOf(TestBaza.vrednost("SELECT TrenerID FROM Trener WHERE BrojTelefona = '0603030303'"));

        uspesno(posalji(Operation.UPDATE_TRENER, new Trener(id, "Milan", "Jovic", "0603030303", 10)));
        assertEquals("10", TestBaza.vrednost("SELECT GodineIskustva FROM Trener WHERE TrenerID = " + id));

        Response svi = posalji(Operation.GET_ALL_TRENER, new Trener(null, "", "", "", 0));
        uspesno(svi);
        assertEquals(3, ((ArrayList<?>) svi.getData()).size());

        uspesno(posalji(Operation.DELETE_TRENER, new Trener(id, "Milan", "Jovic", "0603030303", 10)));
        assertEquals(2, TestBaza.broj("SELECT COUNT(*) FROM Trener"));
    }

    @Test
    public void sveOperacijeNadGrupama() throws Exception {
        Response dodavanje = posalji(Operation.ADD_GRUPA, TestBaza.novaGrupa("Mrezna grupa", 1, 2));
        uspesno(dodavanje);
        assertTrue(String.valueOf(dodavanje.getData()).startsWith("Slanje maila je iskljuceno"));
        Long id = Long.valueOf(TestBaza.vrednost("SELECT GrupaID FROM Grupa WHERE Naziv = 'Mrezna grupa'"));

        Grupa grupa = TestBaza.grupa(1);
        grupa.setOpis("Izmenjeno preko mreze.");
        Response izmena = posalji(Operation.UPDATE_GRUPA, grupa);
        uspesno(izmena);
        assertTrue(String.valueOf(izmena.getData()).startsWith("Slanje maila je iskljuceno"));
        assertEquals("Izmenjeno preko mreze.", TestBaza.vrednost("SELECT Opis FROM Grupa WHERE GrupaID = 1"));

        Response sve = posalji(Operation.GET_ALL_GRUPA, new Grupa(null, "", "", 0, 0, new Kategorija(null, "", ""),
                new Trener(null, "", "", "", 0), new Administrator(null, "", "", "", ""), null));
        uspesno(sve);
        assertEquals(3, ((ArrayList<?>) sve.getData()).size());

        Grupa zaBrisanje = new Grupa();
        zaBrisanje.setGrupaID(id);
        uspesno(posalji(Operation.DELETE_GRUPA, zaBrisanje));
        assertEquals(0, TestBaza.broj("SELECT COUNT(*) FROM Grupa WHERE GrupaID = " + id));
    }

    @Test
    public void vracaKategorije() throws Exception {
        Response r = posalji(Operation.GET_ALL_KATEGORIJA, new Kategorija(null, "", ""));

        uspesno(r);
        assertEquals(4, ((ArrayList<?>) r.getData()).size());
    }

    @Test
    public void nepoznataOperacijaVracaPrazanOdgovor() throws Exception {
        assertNull(posalji(99, null));
        uspesno(posalji(Operation.GET_ALL_KATEGORIJA, new Kategorija(null, "", "")));
    }

    @Test
    public void viseKlijenataRadiIstovremeno() throws Exception {
        try (Socket drugi = poveziSe()) {
            uspesno(posalji(drugi, Operation.GET_ALL_KATEGORIJA, new Kategorija(null, "", "")));
            uspesno(posalji(Operation.GET_ALL_KATEGORIJA, new Kategorija(null, "", "")));
        }
        try (Socket treci = poveziSe()) {
            uspesno(posalji(treci, Operation.GET_ALL_TRENER, new Trener(null, "", "", "", 0)));
        }
    }

    @Test
    public void gasenjeServeraZaustavljaPrihvatanjeKlijenata() throws Exception {
        server.getServerSocket().close();
        server.join(5000);

        assertFalse(server.isAlive());
    }

    @Test
    public void serverSePostavljaPrekoSetera() throws Exception {
        java.net.ServerSocket novi = new java.net.ServerSocket(0);
        java.net.ServerSocket stari = server.getServerSocket();
        server.setServerSocket(novi);
        assertEquals(novi.getLocalPort(), server.getServerSocket().getLocalPort());
        server.setServerSocket(stari);
        novi.close();
    }

    @Test
    public void zauzetPortSeBeleziINitSeOdmahZavrsava() throws Exception {
        try (java.net.ServerSocket zauzet = new java.net.ServerSocket(0);
                HvatacLogova log = new HvatacLogova(ThreadServer.class)) {
            ThreadServer drugi = new ThreadServer(zauzet.getLocalPort());

            assertNull(drugi.getServerSocket());
            assertEquals("Server ne moze da se pokrene na portu " + zauzet.getLocalPort() + ".", log.cekajGresku().getMessage());

            drugi.start();
            drugi.join(5000);
            assertFalse(drugi.isAlive());
        }
    }

    @Test
    public void gasenjeServeraNijeGreska() throws Exception {
        try (HvatacLogova log = new HvatacLogova(ThreadServer.class)) {
            String ispis = uhvatiIspis(() -> {
                server.getServerSocket().close();
                server.join(5000);
            }, "Server je zaustavljen.");

            assertTrue(ispis.contains("Server je zaustavljen."));
            assertTrue(log.greske().isEmpty());
        }
    }

    @Test
    public void neocekivanaGreskaServeraSeBelezi() throws Exception {
        ThreadServer s = new ThreadServer(0);
        s.getServerSocket().close();
        java.net.ServerSocket neispravan = new java.net.ServerSocket() {
            @Override
            public Socket accept() throws java.io.IOException {
                throw new java.io.IOException("Test greska");
            }
        };
        s.setServerSocket(neispravan);
        try (HvatacLogova log = new HvatacLogova(ThreadServer.class)) {
            s.start();
            s.join(5000);

            assertFalse(s.isAlive());
            assertEquals("Greska u radu servera.", log.cekajGresku().getMessage());
        } finally {
            neispravan.close();
        }
    }

    @Test
    public void prekidVezeKlijentaNijeGreska() throws Exception {
        try (HvatacLogova log = new HvatacLogova(ThreadClient.class)) {
            uspesno(posalji(Operation.GET_ALL_KATEGORIJA, new Kategorija(null, "", "")));

            String ispis = uhvatiIspis(() -> klijent.close(), "Klijent je prekinuo vezu.");

            assertTrue(ispis.contains("Klijent je prekinuo vezu."));
            assertTrue(log.greske().isEmpty());
        }
    }

    @Test
    public void pogresanObjekatOdKlijentaSeBeleziAServerRadiDalje() throws Exception {
        try (HvatacLogova log = new HvatacLogova(ThreadClient.class)) {
            ObjectOutputStream out = new ObjectOutputStream(klijent.getOutputStream());
            out.writeObject("ovo nije zahtev");
            out.flush();

            assertEquals("Greska u komunikaciji sa klijentom.", log.cekajGresku().getMessage());
        }
        try (Socket drugi = poveziSe()) {
            uspesno(posalji(drugi, Operation.GET_ALL_KATEGORIJA, new Kategorija(null, "", "")));
        }
    }

    private interface Radnja {

        void izvrsi() throws Exception;
    }

    private static String uhvatiIspis(Radnja radnja, String ocekivano) throws Exception {
        java.io.PrintStream stari = System.out;
        java.io.ByteArrayOutputStream bafer = new java.io.ByteArrayOutputStream();
        System.setOut(new java.io.PrintStream(bafer, true, "UTF-8"));
        try {
            radnja.izvrsi();
            long kraj = System.currentTimeMillis() + 10000;
            while (!bafer.toString("UTF-8").contains(ocekivano) && System.currentTimeMillis() < kraj) {
                Gui.spavaj(10);
            }
        } finally {
            System.setOut(stari);
        }
        String ispis = bafer.toString("UTF-8");
        stari.print(ispis);
        return ispis;
    }
}
