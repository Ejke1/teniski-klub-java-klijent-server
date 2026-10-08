package testutil;

import domain.Administrator;
import domain.Clan;
import domain.ClanGrupe;
import domain.Grupa;
import domain.Kategorija;
import domain.Trener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import transfer.Request;
import transfer.Response;
import transfer.util.Operation;
import transfer.util.ResponseStatus;

public final class LazniServer {

    private static LazniServer instanca;

    private final ServerSocket server;
    private final List<Administrator> administratori = new ArrayList<>();
    private final List<Kategorija> kategorije = new ArrayList<>();
    private final List<Trener> treneri = new ArrayList<>();
    private final List<Clan> clanovi = new ArrayList<>();
    private final List<Grupa> grupe = new ArrayList<>();
    private final Map<Integer, String> greske = new HashMap<>();
    private final Map<Integer, Object> poslednji = new HashMap<>();
    private final Map<Integer, Integer> brojZahteva = new HashMap<>();
    private boolean ignorisiKategoriju;

    private LazniServer() throws IOException {
        server = new ServerSocket(0, 50, InetAddress.getLoopbackAddress());
        resetuj();
        Thread nit = new Thread(this::prihvataj, "lazni-server");
        nit.setDaemon(true);
        nit.start();
    }

    public static synchronized LazniServer pokreni() {
        if (instanca == null) {
            try {
                instanca = new LazniServer();
            } catch (IOException ex) {
                throw new IllegalStateException(ex);
            }
            System.setProperty("server.host", "127.0.0.1");
            System.setProperty("server.port", String.valueOf(instanca.getPort()));
        }
        return instanca;
    }

    public int getPort() {
        return server.getLocalPort();
    }

    public synchronized void resetuj() {
        administratori.clear();
        kategorije.clear();
        treneri.clear();
        clanovi.clear();
        grupe.clear();
        greske.clear();
        poslednji.clear();
        brojZahteva.clear();
        ignorisiKategoriju = false;

        administratori.add(new Administrator(1L, "Aleksandar", "Eic", "coa", "coa123"));
        administratori.add(new Administrator(2L, "Stefan", "Markovic", "stefan", "stefan123"));

        kategorije.add(new Kategorija(1L, "Pocetni", "Osnovna tehnika."));
        kategorije.add(new Kategorija(2L, "Srednji", "Taktika."));
        kategorije.add(new Kategorija(3L, "Napredni", "Takmicari."));

        treneri.add(new Trener(1L, "Ivan", "Ristic", "0601010101", 8));
        treneri.add(new Trener(2L, "Tamara", "Vukovic", "0602020202", 11));

        clanovi.add(new Clan(1L, "Lana", "Stojanovic", 9, "lana@test.rs", "0641112222", kategorija(1)));
        clanovi.add(new Clan(2L, "Uros", "Milosevic", 10, "uros@test.rs", "0643334444", kategorija(1)));
        clanovi.add(new Clan(3L, "Petar", "Vasic", 7, "petar@test.rs", "0645552222", kategorija(1)));
        clanovi.add(new Clan(4L, "Mina", "Popovic", 12, "mina@test.rs", "0655556666", kategorija(2)));
        clanovi.add(new Clan(5L, "Andrej", "Nikolic", 13, "andrej@test.rs", "0657778888", kategorija(2)));

        Grupa mini = new Grupa(1L, "Mini Tenis A", "Pocetnici.", 2, 10, kategorija(1), trener(1),
                administratori.get(0), new ArrayList<>());
        mini.getClanoviGrupe().add(new ClanGrupe(mini, 1, "/", clan(1)));
        mini.getClanoviGrupe().add(new ClanGrupe(mini, 2, "Ne moze petkom.", clan(2)));
        grupe.add(mini);

        Grupa srednji = new Grupa(2L, "Srednji Nivo A", "Srednji nivo.", 2, 12, kategorija(2), trener(2),
                administratori.get(0), new ArrayList<>());
        srednji.getClanoviGrupe().add(new ClanGrupe(srednji, 1, "/", clan(4)));
        srednji.getClanoviGrupe().add(new ClanGrupe(srednji, 2, "/", clan(5)));
        grupe.add(srednji);
    }

    public synchronized void greska(int operacija, String poruka) {
        greske.put(operacija, poruka);
    }

    public synchronized Object poslednji(int operacija) {
        return poslednji.get(operacija);
    }

    public synchronized int brojZahteva(int operacija) {
        return brojZahteva.getOrDefault(operacija, 0);
    }

    public synchronized Kategorija kategorija(long id) {
        for (Kategorija k : kategorije) {
            if (k.getKategorijaID() == id) {
                return new Kategorija(k.getKategorijaID(), k.getNaziv(), k.getOpis());
            }
        }
        return null;
    }

    public synchronized Trener trener(long id) {
        for (Trener t : treneri) {
            if (t.getTrenerID() == id) {
                return new Trener(t.getTrenerID(), t.getIme(), t.getPrezime(), t.getBrojTelefona(), t.getGodineIskustva());
            }
        }
        return null;
    }

    public synchronized Clan clan(long id) {
        for (Clan c : clanovi) {
            if (c.getClanID() == id) {
                return kopija(c);
            }
        }
        return null;
    }

    public synchronized Grupa grupa(long id) {
        for (Grupa g : grupe) {
            if (g.getGrupaID() == id) {
                return kopija(g);
            }
        }
        return null;
    }

    public synchronized List<Clan> clanovi() {
        List<Clan> lista = new ArrayList<>();
        for (Clan c : clanovi) {
            lista.add(kopija(c));
        }
        return lista;
    }

    public synchronized List<Trener> treneri() {
        List<Trener> lista = new ArrayList<>();
        for (Trener t : treneri) {
            lista.add(trener(t.getTrenerID()));
        }
        return lista;
    }

    public synchronized int brojGrupa() {
        return grupe.size();
    }

    public synchronized void izmeniClana(long id, int godine, long kategorijaId) {
        for (Clan c : clanovi) {
            if (c.getClanID() == id) {
                c.setGodine(godine);
                c.setKategorija(kategorija(kategorijaId));
            }
        }
    }

    public synchronized void ignorisiKategorijuPriPretrazi() {
        ignorisiKategoriju = true;
    }

    public synchronized void dodajClanaBezKategorije(Clan c) {
        c.setKategorija(null);
        clanovi.add(c);
    }

    public synchronized Trener dodajTrenera(String ime, String prezime, String telefon, int godine) {
        Trener t = new Trener(200L + treneri.size(), ime, prezime, telefon, godine);
        treneri.add(t);
        return trener(t.getTrenerID());
    }

    public synchronized void ukloniClana(long id) {
        clanovi.removeIf(c -> c.getClanID() == id);
    }

    private static Clan kopija(Clan c) {
        Kategorija k = c.getKategorija() == null ? null
                : new Kategorija(c.getKategorija().getKategorijaID(), c.getKategorija().getNaziv(), c.getKategorija().getOpis());
        return new Clan(c.getClanID(), c.getIme(), c.getPrezime(), c.getGodine(), c.getEmail(), c.getTelefon(), k);
    }

    private Grupa kopija(Grupa g) {
        Grupa nova = new Grupa(g.getGrupaID(), g.getNaziv(), g.getOpis(), g.getBrojClanova(), g.getMaxKapacitet(),
                g.getKategorija(), g.getTrener(), g.getAdministrator(), new ArrayList<>());
        for (ClanGrupe cg : g.getClanoviGrupe()) {
            Clan aktuelni = clan(cg.getClan().getClanID());
            nova.getClanoviGrupe().add(new ClanGrupe(nova, cg.getRb(), cg.getNapomena(),
                    aktuelni != null ? aktuelni : cg.getClan()));
        }
        return nova;
    }

    private void prihvataj() {
        while (!server.isClosed()) {
            try {
                Socket s = server.accept();
                Thread nit = new Thread(() -> obradi(s), "lazni-server-klijent");
                nit.setDaemon(true);
                nit.start();
            } catch (IOException ex) {
                return;
            }
        }
    }

    private void obradi(Socket s) {
        try {
            while (!s.isClosed()) {
                Request zahtev = (Request) new ObjectInputStream(s.getInputStream()).readObject();
                Response odgovor = odgovori(zahtev);
                ObjectOutputStream out = new ObjectOutputStream(s.getOutputStream());
                out.writeObject(odgovor);
                out.flush();
            }
        } catch (Exception kraj) {
            try {
                s.close();
            } catch (IOException ignore) {
            }
        }
    }

    private synchronized Response odgovori(Request zahtev) {
        int op = zahtev.getOperation();
        poslednji.put(op, zahtev.getData());
        brojZahteva.merge(op, 1, Integer::sum);
        String greska = greske.remove(op);
        if (greska != null) {
            return new Response(null, new Exception(greska), ResponseStatus.Error);
        }
        try {
            return new Response(obradiOperaciju(op, zahtev.getData()), null, ResponseStatus.Success);
        } catch (Exception ex) {
            return new Response(null, ex, ResponseStatus.Error);
        } catch (NepoznataOperacija ex) {
            return null;
        }
    }

    private static class NepoznataOperacija extends Throwable {
    }

    private Object obradiOperaciju(int op, Object podaci) throws Exception, NepoznataOperacija {
        switch (op) {
            case Operation.LOGIN: {
                Administrator a = (Administrator) podaci;
                for (Administrator admin : administratori) {
                    if (admin.getUsername().equals(a.getUsername()) && admin.getPassword().equals(a.getPassword())) {
                        return admin;
                    }
                }
                throw new Exception("Ne postoji administrator sa tim kredencijalima.");
            }
            case Operation.LOGOUT:
                return null;
            case Operation.GET_ALL_KATEGORIJA: {
                Kategorija k = (Kategorija) podaci;
                List<Kategorija> lista = new ArrayList<>();
                for (Kategorija kat : kategorije) {
                    if (sadrzi(kat.getNaziv(), k.getNaziv())) {
                        lista.add(kategorija(kat.getKategorijaID()));
                    }
                }
                return lista;
            }
            case Operation.GET_ALL_TRENER: {
                Trener k = (Trener) podaci;
                List<Trener> lista = new ArrayList<>();
                for (Trener t : treneri) {
                    if (sadrzi(t.getIme(), k.getIme()) || sadrzi(t.getPrezime(), k.getPrezime())) {
                        lista.add(trener(t.getTrenerID()));
                    }
                }
                return lista;
            }
            case Operation.GET_ALL_CLAN: {
                Clan k = (Clan) podaci;
                List<Clan> lista = new ArrayList<>();
                for (Clan c : clanovi) {
                    boolean tekst = sadrzi(c.getIme(), k.getIme()) || sadrzi(c.getPrezime(), k.getPrezime())
                            || sadrzi(c.getEmail(), k.getEmail());
                    boolean kat = ignorisiKategoriju || k.getKategorija() == null
                            || k.getKategorija().getKategorijaID().equals(c.getKategorija().getKategorijaID());
                    if (tekst && kat) {
                        lista.add(kopija(c));
                    }
                }
                return lista;
            }
            case Operation.GET_ALL_GRUPA: {
                Grupa k = (Grupa) podaci;
                List<Grupa> lista = new ArrayList<>();
                for (Grupa g : grupe) {
                    if (sadrzi(g.getTrener().getIme(), k.getTrener().getIme())
                            || sadrzi(g.getTrener().getPrezime(), k.getTrener().getPrezime())
                            || sadrzi(g.getNaziv(), k.getNaziv())) {
                        lista.add(kopija(g));
                    }
                }
                return lista;
            }
            case Operation.ADD_CLAN: {
                Clan c = (Clan) podaci;
                c.setClanID(sledeciId(clanovi.size() + 100L));
                clanovi.add(c);
                return null;
            }
            case Operation.UPDATE_CLAN: {
                Clan c = (Clan) podaci;
                clanovi.replaceAll(stari -> stari.getClanID().equals(c.getClanID()) ? c : stari);
                return null;
            }
            case Operation.DELETE_CLAN: {
                Clan c = (Clan) podaci;
                for (Grupa g : grupe) {
                    for (ClanGrupe cg : g.getClanoviGrupe()) {
                        if (cg.getClan().getClanID().equals(c.getClanID())) {
                            throw new Exception("Cannot delete or update a parent row: a foreign key constraint fails");
                        }
                    }
                }
                clanovi.removeIf(stari -> stari.getClanID().equals(c.getClanID()));
                return null;
            }
            case Operation.ADD_TRENER: {
                Trener t = (Trener) podaci;
                t.setTrenerID(sledeciId(treneri.size() + 100L));
                treneri.add(t);
                return null;
            }
            case Operation.UPDATE_TRENER: {
                Trener t = (Trener) podaci;
                treneri.replaceAll(stari -> stari.getTrenerID().equals(t.getTrenerID()) ? t : stari);
                return null;
            }
            case Operation.DELETE_TRENER: {
                Trener t = (Trener) podaci;
                for (Grupa g : grupe) {
                    if (g.getTrener().getTrenerID().equals(t.getTrenerID())) {
                        throw new Exception("Cannot delete or update a parent row: a foreign key constraint fails");
                    }
                }
                treneri.removeIf(stari -> stari.getTrenerID().equals(t.getTrenerID()));
                return null;
            }
            case Operation.ADD_GRUPA: {
                Grupa g = (Grupa) podaci;
                g.setGrupaID(sledeciId(grupe.size() + 100L));
                grupe.add(g);
                return izvestaj(g);
            }
            case Operation.UPDATE_GRUPA: {
                Grupa g = (Grupa) podaci;
                grupe.replaceAll(stara -> stara.getGrupaID().equals(g.getGrupaID()) ? g : stara);
                return izvestaj(g);
            }
            case Operation.DELETE_GRUPA: {
                Grupa g = (Grupa) podaci;
                grupe.removeIf(stara -> stara.getGrupaID().equals(g.getGrupaID()));
                return null;
            }
            default:
                throw new NepoznataOperacija();
        }
    }

    private static long sledeciId(long predlog) {
        return predlog;
    }

    public static String izvestaj(Grupa g) {
        int n = g.getClanoviGrupe() == null ? 0 : g.getClanoviGrupe().size();
        return "Mail sa PDF spiskom clanova je poslat na " + n + " od " + n + " clanova grupe.";
    }

    private static boolean sadrzi(String vrednost, String deo) {
        if (deo == null) {
            return false;
        }
        return vrednost != null && vrednost.toLowerCase(Locale.ROOT).contains(deo.toLowerCase(Locale.ROOT));
    }
}
