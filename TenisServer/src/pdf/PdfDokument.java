package pdf;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PdfDokument {

    public static final float SIRINA = 595f;
    public static final float VISINA = 842f;

    private final List<StringBuilder> strane = new ArrayList<>();
    private StringBuilder trenutna;

    public PdfDokument() {
        novaStrana();
    }

    public final void novaStrana() {
        trenutna = new StringBuilder();
        strane.add(trenutna);
    }

    public int brojStrana() {
        return strane.size();
    }


    public void tekst(float x, float y, String tekst, boolean bold, float velicina) {
        tekst(x, y, tekst, bold, velicina, 0, 0, 0);
    }

    public void tekst(float x, float y, String tekst, boolean bold, float velicina,
            float r, float g, float b) {
        trenutna.append(String.format(Locale.US, "%.3f %.3f %.3f rg\n", r, g, b));
        trenutna.append("BT /").append(bold ? "F2" : "F1").append(' ')
                .append(fmt(velicina)).append(" Tf ")
                .append(fmt(x)).append(' ').append(fmt(y)).append(" Td (")
                .append(kodiraj(tekst)).append(") Tj ET\n");
    }

    public void linija(float x1, float y1, float x2, float y2, float debljina, float siva) {
        trenutna.append(String.format(Locale.US, "%.3f G %.2f w %.2f %.2f m %.2f %.2f l S\n",
                siva, debljina, x1, y1, x2, y2));
    }

    public void pravougaonik(float x, float y, float sirina, float visina, float r, float g, float b) {
        trenutna.append(String.format(Locale.US, "%.3f %.3f %.3f rg %.2f %.2f %.2f %.2f re f\n",
                r, g, b, x, y, sirina, visina));
    }


    public static float sirinaTeksta(String tekst, boolean bold, float velicina) {
        float ukupno = 0;
        for (char c : tekst.toCharArray()) {
            int w = (c >= 32 && c <= 126) ? HELVETICA[c - 32] : 556;
            ukupno += bold ? w * 1.07f : w;
        }
        return ukupno * velicina / 1000f;
    }

    public static String skrati(String tekst, boolean bold, float velicina, float maxSirina) {
        if (tekst == null) {
            return "";
        }
        if (sirinaTeksta(tekst, bold, velicina) <= maxSirina) {
            return tekst;
        }
        String s = tekst;
        while (s.length() > 1 && sirinaTeksta(s + "...", bold, velicina) > maxSirina) {
            s = s.substring(0, s.length() - 1);
        }
        return s + "...";
    }

    public static List<String> prelomi(String tekst, boolean bold, float velicina, float maxSirina) {
        List<String> redovi = new ArrayList<>();
        if (tekst == null || tekst.trim().isEmpty()) {
            redovi.add("");
            return redovi;
        }
        StringBuilder red = new StringBuilder();
        for (String rec : tekst.trim().split("\\s+")) {
            String probni = red.length() == 0 ? rec : red + " " + rec;
            if (sirinaTeksta(probni, bold, velicina) <= maxSirina || red.length() == 0) {
                red.setLength(0);
                red.append(probni);
            } else {
                redovi.add(red.toString());
                red.setLength(0);
                red.append(rec);
            }
        }
        redovi.add(red.toString());
        return redovi;
    }


    public byte[] uBajtove() {
        List<String> objekti = new ArrayList<>();
        int brStrana = strane.size();
        objekti.add("<< /Type /Catalog /Pages 2 0 R >>");

        StringBuilder kids = new StringBuilder();
        for (int i = 0; i < brStrana; i++) {
            kids.append(6 + i * 2).append(" 0 R ");
        }
        objekti.add("<< /Type /Pages /Kids [" + kids + "] /Count " + brStrana + " >>");
        objekti.add("<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica /Encoding 5 0 R >>");
        objekti.add("<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica-Bold /Encoding 5 0 R >>");
        objekti.add("<< /Type /Encoding /BaseEncoding /WinAnsiEncoding /Differences "
                + "[127 /Dcroat 129 /ccaron 141 /Ccaron 143 /cacute 144 /Cacute 157 /dcroat] >>");

        for (int i = 0; i < brStrana; i++) {
            int sadrzajId = 7 + i * 2;
            objekti.add("<< /Type /Page /Parent 2 0 R /MediaBox [0 0 595 842] "
                    + "/Resources << /Font << /F1 3 0 R /F2 4 0 R >> >> "
                    + "/Contents " + sadrzajId + " 0 R >>");
            String tok = strane.get(i).toString();
            objekti.add("<< /Length " + tok.getBytes(StandardCharsets.ISO_8859_1).length + " >>\nstream\n"
                    + tok + "endstream");
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        List<Integer> pozicije = new ArrayList<>();
        pisi(out, "%PDF-1.4\n%âãÏÓ\n");
        for (int i = 0; i < objekti.size(); i++) {
            pozicije.add(out.size());
            pisi(out, (i + 1) + " 0 obj\n" + objekti.get(i) + "\nendobj\n");
        }
        int xref = out.size();
        StringBuilder sb = new StringBuilder();
        sb.append("xref\n0 ").append(objekti.size() + 1).append("\n0000000000 65535 f \n");
        for (int p : pozicije) {
            sb.append(String.format("%010d 00000 n \n", p));
        }
        sb.append("trailer\n<< /Size ").append(objekti.size() + 1).append(" /Root 1 0 R >>\n")
                .append("startxref\n").append(xref).append("\n%%EOF\n");
        pisi(out, sb.toString());
        return out.toByteArray();
    }

    private static void pisi(ByteArrayOutputStream out, String s) {
        byte[] b = s.getBytes(StandardCharsets.ISO_8859_1);
        out.write(b, 0, b.length);
    }

    private static String fmt(float f) {
        return String.format(Locale.US, "%.2f", f);
    }

    private static String kodiraj(String s) {
        StringBuilder sb = new StringBuilder();
        if (s == null) {
            return "";
        }
        for (char c : s.toCharArray()) {
            int kod;
            switch (c) {
                case 'č': kod = 129; break;
                case 'Č': kod = 141; break;
                case 'ć': kod = 143; break;
                case 'Ć': kod = 144; break;
                case 'đ': kod = 157; break;
                case 'Đ': kod = 127; break;
                case 'š': kod = 0x9A; break;
                case 'Š': kod = 0x8A; break;
                case 'ž': kod = 0x9E; break;
                case 'Ž': kod = 0x8E; break;
                case '„': kod = 0x84; break;
                case '“': kod = 0x93; break;
                case '–': kod = 0x96; break;
                default:
                    kod = (c < 256) ? c : '?';
            }
            if (kod == '(' || kod == ')' || kod == '\\') {
                sb.append('\\').append((char) kod);
            } else if (kod < 32 || kod > 126) {
                sb.append(String.format("\\%03o", kod));
            } else {
                sb.append((char) kod);
            }
        }
        return sb.toString();
    }

    private static final int[] HELVETICA = {
        278, 278, 355, 556, 556, 889, 667, 191, 333, 333, 389, 584, 278, 333, 278, 278,
        556, 556, 556, 556, 556, 556, 556, 556, 556, 556, 278, 278, 584, 584, 584, 556,
        1015, 667, 667, 722, 722, 667, 611, 778, 722, 278, 500, 667, 556, 833, 722, 778,
        667, 778, 722, 667, 611, 722, 667, 944, 667, 667, 611, 278, 278, 278, 469, 556,
        333, 556, 556, 500, 556, 556, 278, 556, 556, 222, 222, 500, 222, 833, 556, 556,
        556, 556, 333, 500, 278, 556, 500, 722, 500, 500, 500, 334, 260, 334, 584
    };
}
