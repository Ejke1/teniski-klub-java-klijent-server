package pdf;

import domain.Clan;
import domain.ClanGrupe;
import domain.Grupa;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import testutil.TestBaza;

public class PdfGrupaTest {

    private static String kaoTekst(byte[] pdf) {
        return new String(pdf, StandardCharsets.ISO_8859_1);
    }

    @Test
    public void praviIspravanPdfDokument() {
        String pdf = kaoTekst(PdfGrupa.napravi(TestBaza.grupa(1), true));

        assertTrue(pdf.startsWith("%PDF-1.4"));
        assertTrue(pdf.trim().endsWith("%%EOF"));
        assertTrue(pdf.contains("/Type /Catalog"));
        assertTrue(pdf.contains("/Count 1"));
    }

    @Test
    public void sadrziPodatkeOGrupiISveClanove() {
        String pdf = kaoTekst(PdfGrupa.napravi(TestBaza.grupa(1), true));

        assertTrue(pdf.contains("(Mini Tenis A)"));
        assertTrue(pdf.contains("(Ivan Ristic  \\(tel. 0601010101\\))"));
        assertTrue(pdf.contains("(Lana Stojanovic)"));
        assertTrue(pdf.contains("(Uros Milosevic)"));
        assertTrue(pdf.contains("(Petar Vasic)"));
        assertTrue(pdf.contains("(lana.stojanovic@test.rs)"));
        assertTrue(pdf.contains("(Ne moze petkom.)"));
        assertTrue(pdf.contains("nova grupa"));
    }

    @Test
    public void naglasavaDaJeGrupaIzmenjena() {
        String pdf = kaoTekst(PdfGrupa.napravi(TestBaza.grupa(1), false));

        assertTrue(pdf.contains("izmena grupe"));
    }

    @Test
    public void ispravnoKodiraSrpskaSlova() {
        Grupa g = TestBaza.grupa(1);
        g.getClanoviGrupe().get(0).getClan().setPrezime("Đorđević");
        g.getClanoviGrupe().get(1).getClan().setIme("Čedomir");

        String pdf = kaoTekst(PdfGrupa.napravi(g, true));

        assertTrue(pdf.contains("/Differences [127 /Dcroat 129 /ccaron 141 /Ccaron 143 /cacute 144 /Cacute 157 /dcroat]"));
        assertTrue(pdf.contains("(Lana \\177or\\235evi\\217)"));
        assertTrue(pdf.contains("(\\215edomir Milosevic)"));
    }

    @Test
    public void velikuGrupuPrelamaNaViseStrana() {
        Grupa g = TestBaza.grupa(1);
        ArrayList<ClanGrupe> clanovi = new ArrayList<>();
        for (int i = 1; i <= 30; i++) {
            Clan c = new Clan((long) i, "Clan" + i, "Test", 10, "clan" + i + "@test.rs", "0640000000", TestBaza.kategorija(1));
            clanovi.add(new ClanGrupe(g, i, "/", c));
        }
        g.setClanoviGrupe(clanovi);

        String pdf = kaoTekst(PdfGrupa.napravi(g, true));

        assertTrue(pdf.contains("/Count 2"));
        assertTrue(pdf.contains("(Clan1 Test)"));
        assertTrue(pdf.contains("(Clan30 Test)"));
    }

    @Test
    public void radiIBezClanova() {
        Grupa g = TestBaza.grupa(1);
        g.setClanoviGrupe(null);

        String pdf = kaoTekst(PdfGrupa.napravi(g, true));

        assertTrue(pdf.startsWith("%PDF-"));
    }

    @Test
    public void radiSaNepotpunimPodacima() {
        Grupa g = TestBaza.grupa(1);
        g.setTrener(null);
        g.setAdministrator(null);
        g.setKategorija(null);
        g.setOpis(null);
        g.getClanoviGrupe().get(0).getClan().setEmail(null);
        g.getClanoviGrupe().get(0).getClan().setTelefon(null);

        String pdf = kaoTekst(PdfGrupa.napravi(g, true));

        assertTrue(pdf.startsWith("%PDF-"));
        assertTrue(pdf.contains("(Trener:)"));
        assertTrue(pdf.contains("(Lana Stojanovic)"));
    }

    @Test
    public void trenerBezTelefonaSePrikazujeSamoImenom() {
        Grupa g = TestBaza.grupa(1);
        g.getTrener().setBrojTelefona(null);

        String pdf = kaoTekst(PdfGrupa.napravi(g, true));

        assertTrue(pdf.contains("(Ivan Ristic)"));
    }

    @Test
    public void napomenaKosaCrtaSeNePrikazuje() {
        String pdf = kaoTekst(PdfGrupa.napravi(TestBaza.grupa(1), true));

        assertTrue(!pdf.contains("(/)"));
    }
}
