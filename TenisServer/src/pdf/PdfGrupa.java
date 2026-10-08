package pdf;

import domain.Clan;
import domain.ClanGrupe;
import domain.Grupa;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class PdfGrupa {

    private static final float LEVO = 40f;
    private static final float DESNO = PdfDokument.SIRINA - 40f;
    private static final float DNO = 60f;

    private static final String[] KOLONE = {"Rb", "Ime i prezime", "God.", "Email", "Telefon", "Napomena"};
    private static final float[] SIRINE = {28f, 112f, 34f, 150f, 76f, 115f};

    private static final float[] PLAVA = {0.13f, 0.34f, 0.60f};
    private static final float[] SVETLO_PLAVA = {0.87f, 0.92f, 0.98f};
    private static final float[] SIVA_RED = {0.96f, 0.96f, 0.96f};

    private PdfGrupa() {
    }

    public static byte[] napravi(Grupa g, boolean novaGrupa) {
        PdfDokument pdf = new PdfDokument();
        float y = zaglavlje(pdf, g, novaGrupa);
        y = podaciOGrupi(pdf, g, y);
        y -= 26;
        pdf.tekst(LEVO, y, "\u010Clanovi grupe (" + brojClanova(g) + ")", true, 13, PLAVA[0], PLAVA[1], PLAVA[2]);
        y -= 12;
        tabelaClanova(pdf, g, y);
        podnozja(pdf);
        return pdf.uBajtove();
    }


    private static float zaglavlje(PdfDokument pdf, Grupa g, boolean novaGrupa) {
        pdf.pravougaonik(0, PdfDokument.VISINA - 90, PdfDokument.SIRINA, 90, PLAVA[0], PLAVA[1], PLAVA[2]);
        pdf.tekst(LEVO, PdfDokument.VISINA - 42, "Teniski klub", true, 22, 1, 1, 1);
        pdf.tekst(LEVO, PdfDokument.VISINA - 66,
                "Spisak članova grupe – " + (novaGrupa ? "nova grupa" : "izmena grupe"),
                false, 12, 1, 1, 1);
        String datum = new SimpleDateFormat("dd.MM.yyyy. HH:mm").format(new Date());
        float w = PdfDokument.sirinaTeksta(datum, false, 10);
        pdf.tekst(DESNO - w, PdfDokument.VISINA - 42, datum, false, 10, 1, 1, 1);
        return PdfDokument.VISINA - 125;
    }

    private static float podaciOGrupi(PdfDokument pdf, Grupa g, float y) {
        pdf.tekst(LEVO, y, nvl(g.getNaziv()), true, 16, PLAVA[0], PLAVA[1], PLAVA[2]);
        y -= 10;
        pdf.linija(LEVO, y, DESNO, y, 1f, 0.75f);
        y -= 20;

        String trener = g.getTrener() == null ? "-"
                : g.getTrener().getIme() + " " + g.getTrener().getPrezime()
                + (g.getTrener().getBrojTelefona() == null ? "" : "  (tel. " + g.getTrener().getBrojTelefona() + ")");
        String admin = g.getAdministrator() == null ? "-"
                : g.getAdministrator().getIme() + " " + g.getAdministrator().getPrezime();

        String[][] polja = {
            {"Kategorija:", g.getKategorija() == null ? "-" : nvl(g.getKategorija().getNaziv())},
            {"Trener:", trener},
            {"Broj članova:", brojClanova(g) + " (maksimalni kapacitet: " + g.getMaxKapacitet() + ")"},
            {"Administrator:", admin}
        };
        for (String[] p : polja) {
            pdf.tekst(LEVO, y, p[0], true, 10.5f);
            pdf.tekst(LEVO + 110, y, p[1], false, 10.5f);
            y -= 17;
        }
        pdf.tekst(LEVO, y, "Opis:", true, 10.5f);
        for (String red : PdfDokument.prelomi(nvl(g.getOpis()), false, 10.5f, DESNO - LEVO - 110)) {
            pdf.tekst(LEVO + 110, y, red, false, 10.5f);
            y -= 14;
        }
        return y;
    }

    private static void tabelaClanova(PdfDokument pdf, Grupa g, float y) {
        final float visinaReda = 20f;
        y = zaglavljeTabele(pdf, y, visinaReda);

        List<ClanGrupe> clanovi = g.getClanoviGrupe();
        int i = 0;
        if (clanovi != null) {
            for (ClanGrupe cg : clanovi) {
                if (y - visinaReda < DNO) {
                    pdf.novaStrana();
                    y = PdfDokument.VISINA - 50;
                    y = zaglavljeTabele(pdf, y, visinaReda);
                }
                if (i % 2 == 1) {
                    pdf.pravougaonik(LEVO, y - visinaReda, DESNO - LEVO, visinaReda,
                            SIVA_RED[0], SIVA_RED[1], SIVA_RED[2]);
                }
                Clan c = cg.getClan();
                String napomena = "/".equals(cg.getNapomena()) ? "" : cg.getNapomena();
                String[] vrednosti = {
                    String.valueOf(cg.getRb()),
                    c.getIme() + " " + c.getPrezime(),
                    String.valueOf(c.getGodine()),
                    nvl(c.getEmail()),
                    nvl(c.getTelefon()),
                    nvl(napomena)
                };
                red(pdf, vrednosti, y, visinaReda, false);
                y -= visinaReda;
                pdf.linija(LEVO, y, DESNO, y, 0.5f, 0.8f);
                i++;
            }
        }
    }

    private static float zaglavljeTabele(PdfDokument pdf, float y, float visinaReda) {
        pdf.pravougaonik(LEVO, y - visinaReda, DESNO - LEVO, visinaReda,
                SVETLO_PLAVA[0], SVETLO_PLAVA[1], SVETLO_PLAVA[2]);
        red(pdf, KOLONE, y, visinaReda, true);
        y -= visinaReda;
        pdf.linija(LEVO, y, DESNO, y, 1f, 0.5f);
        return y;
    }

    private static void red(PdfDokument pdf, String[] vrednosti, float y, float visinaReda, boolean bold) {
        float x = LEVO;
        float velicina = 9f;
        for (int k = 0; k < vrednosti.length; k++) {
            String t = PdfDokument.skrati(vrednosti[k], bold, velicina, SIRINE[k] - 8);
            pdf.tekst(x + 4, y - visinaReda + 6.5f, t, bold, velicina);
            x += SIRINE[k];
        }
    }

    private static void podnozja(PdfDokument pdf) {
        String tekst = "Dokument je automatski generisan iz softverskog sistema teniskog kluba.";
        pdf.linija(LEVO, 45, DESNO, 45, 0.5f, 0.75f);
        pdf.tekst(LEVO, 32, tekst, false, 8, 0.4f, 0.4f, 0.4f);
        String strana = "Strana " + pdf.brojStrana() + " / " + pdf.brojStrana();
        pdf.tekst(DESNO - PdfDokument.sirinaTeksta(strana, false, 8), 32, strana, false, 8, 0.4f, 0.4f, 0.4f);
    }

    private static int brojClanova(Grupa g) {
        return g.getClanoviGrupe() == null ? 0 : g.getClanoviGrupe().size();
    }

    private static String nvl(String s) {
        return s == null ? "" : s;
    }
}
