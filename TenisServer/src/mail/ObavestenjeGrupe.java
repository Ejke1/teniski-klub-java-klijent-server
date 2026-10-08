package mail;

import domain.Clan;
import domain.ClanGrupe;
import domain.Grupa;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import pdf.PdfGrupa;

public class ObavestenjeGrupe {

    private ObavestenjeGrupe() {
    }

    static long maxCekanjeSekundi = 10;

    private static final ExecutorService IZVRSILAC = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "slanje-maila");
        t.setDaemon(true);
        return t;
    });

    public static String posalji(Grupa g, boolean novaGrupa) {
        MailLog.upisi("=== Grupa \"" + g.getNaziv() + "\" (ID " + g.getGrupaID() + ") je "
                + (novaGrupa ? "kreirana" : "izmenjena") + " - pokrecem slanje maila");
        Future<String> rezultat = IZVRSILAC.submit(() -> posaljiSvima(g, novaGrupa));
        try {
            return rezultat.get(maxCekanjeSekundi, TimeUnit.SECONDS);
        } catch (TimeoutException ex) {
            MailLog.upisi("Slanje traje duze od " + maxCekanjeSekundi + " s - nastavlja se u pozadini");
            return "Slanje maila clanovima je u toku (traje duze nego obicno) i nastavlja se u pozadini.\n"
                    + "Rezultat se upisuje u TenisServer/" + MailLog.FAJL + ".";
        } catch (Exception ex) {
            MailLog.upisi("Neocekivana greska: " + ex);
            return "Mail nije poslat: " + ex.getMessage();
        }
    }

    private static String posaljiSvima(Grupa g, boolean novaGrupa) {
        MailKonfiguracija konf;
        try {
            konf = MailKonfiguracija.ucitaj();
        } catch (Exception ex) {
            MailLog.upisi("Ne mogu da procitam " + MailKonfiguracija.FAJL + ": " + ex);
            return "Mail nije poslat: ne mogu da procitam " + MailKonfiguracija.FAJL + " (" + ex.getMessage() + ").";
        }
        if (!konf.isUkljuceno()) {
            MailLog.upisi("Slanje je iskljuceno (mail.enabled=false)");
            return "Slanje maila je iskljuceno (mail.enabled=false u " + MailKonfiguracija.FAJL + ").";
        }
        if (!konf.isPodesen()) {
            MailLog.upisi("Nisu uneti mail.username i mail.password");
            return "Mail nije poslat: u fajlu TenisServer/" + MailKonfiguracija.FAJL
                    + " nisu uneti mail.username i mail.password.";
        }

        List<ClanGrupe> clanovi = g.getClanoviGrupe() == null ? new ArrayList<>() : g.getClanoviGrupe();
        byte[] pdf = PdfGrupa.napravi(g, novaGrupa);
        MailLog.upisi("PDF napravljen (" + pdf.length + " bajtova), clanova: " + clanovi.size()
                + ", posiljalac: " + konf.getKorisnik() + ", server: " + konf.getHost() + ":" + konf.getPort());
        String nazivPdf = "Grupa_" + g.getNaziv().replaceAll("[^A-Za-z0-9_-]+", "_") + ".pdf";
        String naslov = "Teniski klub \u2013 grupa \u201E" + g.getNaziv() + "\u201C je "
                + (novaGrupa ? "kreirana" : "izmenjena");

        int poslato = 0;
        List<String> greske = new ArrayList<>();

        try (SmtpKlijent smtp = new SmtpKlijent(konf)) {
            smtp.otvori();
            for (ClanGrupe cg : clanovi) {
                Clan c = cg.getClan();
                if (c.getEmail() == null || c.getEmail().trim().isEmpty()) {
                    greske.add(c.getIme() + " " + c.getPrezime() + " (nema email)");
                    continue;
                }
                try {
                    smtp.posalji(c.getEmail().trim(), c.getIme() + " " + c.getPrezime(), naslov,
                            tekstPoruke(g, c, novaGrupa), nazivPdf, pdf, "application/pdf");
                    poslato++;
                    MailLog.upisi("POSLATO: " + c.getEmail());
                } catch (Exception ex) {
                    greske.add(c.getEmail() + " (" + ex.getMessage() + ")");
                    MailLog.upisi("GRESKA za " + c.getEmail() + ": " + ex.getMessage());
                    smtp.reset();
                }
            }
        } catch (Exception ex) {
            MailLog.upisi("Neuspelo povezivanje/prijava: " + ex.getMessage());
            return "Mail nije poslat: " + ex.getMessage();
        }

        StringBuilder izvestaj = new StringBuilder("Mail sa PDF spiskom clanova je poslat na ")
                .append(poslato).append(" od ").append(clanovi.size()).append(" clanova grupe.");
        if (!greske.isEmpty()) {
            izvestaj.append("\nNije poslato: ").append(String.join(", ", greske));
        }
        MailLog.upisi("Zavrseno: " + izvestaj.toString().replace("\n", " | "));
        return izvestaj.toString();
    }

    private static String tekstPoruke(Grupa g, Clan c, boolean novaGrupa) {
        String trener = g.getTrener() == null ? "-"
                : g.getTrener().getIme() + " " + g.getTrener().getPrezime();
        return "Poštovani/a " + c.getIme() + ",\n\n"
                + "obaveštavamo Vas da je grupa „" + g.getNaziv() + "“, čiji ste član, "
                + (novaGrupa ? "kreirana" : "izmenjena") + ".\n\n"
                + "Kategorija: " + (g.getKategorija() == null ? "-" : g.getKategorija().getNaziv()) + "\n"
                + "Trener: " + trener + "\n"
                + "Broj članova: " + (g.getClanoviGrupe() == null ? 0 : g.getClanoviGrupe().size()) + "\n\n"
                + "U prilogu se nalazi PDF dokument sa spiskom svih članova grupe i podacima o njima.\n\n"
                + "Srdačan pozdrav,\n"
                + "Teniski klub\n";
    }
}
