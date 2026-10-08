package pdf;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class PdfDokumentTest {

    private static String tekst(PdfDokument pdf) {
        return new String(pdf.uBajtove(), StandardCharsets.ISO_8859_1);
    }

    @Test
    public void noviDokumentImaJednuStranu() {
        PdfDokument pdf = new PdfDokument();

        assertEquals(1, pdf.brojStrana());
        assertTrue(tekst(pdf).contains("/Count 1"));
    }

    @Test
    public void novaStranaPovecavaBrojStrana() {
        PdfDokument pdf = new PdfDokument();
        pdf.novaStrana();
        pdf.novaStrana();

        assertEquals(3, pdf.brojStrana());
        assertTrue(tekst(pdf).contains("/Count 3"));
    }

    @Test
    public void tabelaReferenciPokazujeNaObjekte() {
        PdfDokument pdf = new PdfDokument();
        pdf.tekst(40, 800, "Proba", true, 12);
        pdf.novaStrana();
        pdf.tekst(40, 800, "Druga strana", false, 10);
        String s = tekst(pdf);

        int xref = Integer.parseInt(s.substring(s.indexOf("startxref\n") + 10, s.indexOf("\n%%EOF")).trim());
        assertTrue(s.startsWith("xref", xref));
        Matcher m = Pattern.compile("(\\d{10}) 00000 n ").matcher(s);
        int broj = 0;
        while (m.find()) {
            broj++;
            int pozicija = Integer.parseInt(m.group(1));
            assertTrue(s.startsWith(broj + " 0 obj", pozicija));
        }
        assertEquals(9, broj);
    }

    @Test
    public void tekstLinijaIPravougaonikSeUpisujuUSadrzaj() {
        PdfDokument pdf = new PdfDokument();
        pdf.tekst(10, 20, "Obican", false, 9);
        pdf.tekst(10, 40, "Podebljan", true, 11, 1, 0, 0);
        pdf.linija(0, 0, 100, 0, 1.5f, 0.5f);
        pdf.pravougaonik(5, 5, 50, 20, 0, 0, 1);
        String s = tekst(pdf);

        assertTrue(s.contains("BT /F1 9.00 Tf 10.00 20.00 Td (Obican) Tj ET"));
        assertTrue(s.contains("1.000 0.000 0.000 rg\nBT /F2 11.00 Tf 10.00 40.00 Td (Podebljan) Tj ET"));
        assertTrue(s.contains("0.500 G 1.50 w 0.00 0.00 m 100.00 0.00 l S"));
        assertTrue(s.contains("0.000 0.000 1.000 rg 5.00 5.00 50.00 20.00 re f"));
    }

    @Test
    public void kodiraSrpskaSlovaISpecijalneZnake() {
        PdfDokument pdf = new PdfDokument();
        pdf.tekst(0, 0, "čćđšžČĆĐŠŽ „–“ (a\\b) €", false, 10);
        String s = tekst(pdf);

        assertTrue(s, s.contains("(\\201\\217\\235\\232\\236\\215\\220\\177\\212\\216 \\204\\226\\223 \\(a\\\\b\\) ?) Tj"));
    }

    @Test
    public void nullTekstSeIspisujeKaoPrazan() {
        PdfDokument pdf = new PdfDokument();
        pdf.tekst(0, 0, null, false, 10);

        assertTrue(tekst(pdf).contains("Td () Tj"));
    }

    @Test
    public void racunaSirinuTeksta() {
        assertEquals(0f, PdfDokument.sirinaTeksta("", false, 10), 0.001f);
        assertEquals(6.67f, PdfDokument.sirinaTeksta("A", false, 10), 0.001f);
        assertEquals(6.67f * 1.07f, PdfDokument.sirinaTeksta("A", true, 10), 0.001f);
        assertEquals(5.56f, PdfDokument.sirinaTeksta("č", false, 10), 0.001f);
    }

    @Test
    public void skracujeTekstKojiNeStaje() {
        assertEquals("", PdfDokument.skrati(null, false, 10, 50));
        assertEquals("Kratko", PdfDokument.skrati("Kratko", false, 10, 100));

        String skraceno = PdfDokument.skrati("Veoma dugacak tekst koji ne staje u kolonu", false, 10, 60);
        assertTrue(skraceno.endsWith("..."));
        assertTrue(PdfDokument.sirinaTeksta(skraceno, false, 10) <= 60);
    }

    @Test
    public void prelamaTekstURedove() {
        List<String> prazno = PdfDokument.prelomi("  ", false, 10, 100);
        assertEquals(1, prazno.size());
        assertEquals("", prazno.get(0));
        assertEquals("", PdfDokument.prelomi(null, false, 10, 100).get(0));

        List<String> redovi = PdfDokument.prelomi("jedan dva tri cetiri pet sest sedam osam devet deset",
                false, 10, 80);
        assertTrue(redovi.size() > 1);
        for (String red : redovi) {
            assertTrue(red, PdfDokument.sirinaTeksta(red, false, 10) <= 80);
        }
        assertEquals("jedan dva tri cetiri pet sest sedam osam devet deset", String.join(" ", redovi));
    }

    @Test
    public void predugackaRecOstajeUJednomRedu() {
        List<String> redovi = PdfDokument.prelomi("Supercalifragilistic", false, 10, 20);

        assertEquals(1, redovi.size());
        assertEquals("Supercalifragilistic", redovi.get(0));
    }

    @Test
    public void kontrolniZnakoviSeKodiraju() {
        PdfDokument pdf = new PdfDokument();
        pdf.tekst(0, 0, "a\tb", false, 10);

        assertTrue(tekst(pdf).contains("(a\\011b) Tj"));
        assertEquals(5.56f, PdfDokument.sirinaTeksta("\t", false, 10), 0.001f);
    }

    @Test
    public void skracivanjeNaJedanZnak() {
        assertEquals("a...", PdfDokument.skrati("abcdef", false, 10, 1));
    }
}
