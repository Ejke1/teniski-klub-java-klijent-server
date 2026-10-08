package domain;

import java.util.ArrayList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import org.junit.Test;

public class GrupaTest {

    private final Kategorija pocetni = new Kategorija(1L, "Pocetni", "Opis");
    private final Trener ivan = new Trener(1L, "Ivan", "Ristic", "0601010101", 8);
    private final Administrator coa = new Administrator(1L, "Aleksandar", "Eic", "coa", "coa123");

    private Grupa grupa() {
        return new Grupa(1L, "Mini Tenis A", "Pocetnici.", 3, 10, pocetni, ivan, coa, new ArrayList<>());
    }

    @Test
    public void konstruktorGeteriISeteri() {
        Grupa g = grupa();

        assertEquals(Long.valueOf(1), g.getGrupaID());
        assertEquals("Mini Tenis A", g.getNaziv());
        assertEquals("Pocetnici.", g.getOpis());
        assertEquals(3, g.getBrojClanova());
        assertEquals(10, g.getMaxKapacitet());
        assertSame(pocetni, g.getKategorija());
        assertSame(ivan, g.getTrener());
        assertSame(coa, g.getAdministrator());
        assertEquals(0, g.getClanoviGrupe().size());

        Grupa nova = new Grupa();
        ArrayList<ClanGrupe> clanovi = new ArrayList<>();
        nova.setGrupaID(2L);
        nova.setNaziv("Srednji Nivo A");
        nova.setOpis("Opis");
        nova.setBrojClanova(2);
        nova.setMaxKapacitet(12);
        nova.setKategorija(pocetni);
        nova.setTrener(ivan);
        nova.setAdministrator(coa);
        nova.setClanoviGrupe(clanovi);
        assertEquals(Long.valueOf(2), nova.getGrupaID());
        assertEquals("Srednji Nivo A", nova.getNaziv());
        assertEquals("Opis", nova.getOpis());
        assertEquals(2, nova.getBrojClanova());
        assertEquals(12, nova.getMaxKapacitet());
        assertSame(clanovi, nova.getClanoviGrupe());
    }

    @Test
    public void sqlDelovi() {
        Grupa g = grupa();

        assertEquals(" Grupa ", g.nazivTabele());
        assertEquals(" g ", g.alijas());
        assertEquals(" JOIN TRENER T ON (T.TRENERID = G.TRENERID)\n"
                + "JOIN KATEGORIJA K ON (K.KATEGORIJAID = G.KATEGORIJAID)\n"
                + "JOIN ADMINISTRATOR A ON (A.ADMINISTRATORID = G.ADMINISTRATORID) ", g.join());
        assertEquals(" (naziv, opis, brojClanova, maxKapacitet, KategorijaID, TrenerID, AdministratorID) ", g.koloneZaInsert());
        assertEquals("'Mini Tenis A', 'Pocetnici.', 3, 10, 1, 1, 1", g.vrednostiZaInsert());
        assertEquals(" grupaID = 1", g.uslov());
        assertEquals(" WHERE LOWER(T.IME) LIKE '%Ivan%' OR LOWER(T.PREZIME) LIKE '%Ristic%' OR LOWER(G.NAZIV) LIKE '%Mini Tenis A%'",
                g.dodatniUslov());
        assertEquals(" ORDER BY GRUPAID ASC ", g.orderBy());
    }

    @Test
    public void izmenaGrupeNeMenjaNazivNiKategoriju() {
        String izmena = grupa().vrednostiZaUpdate();

        assertEquals(" trenerID = 1, brojClanova = 3, maxKapacitet = 10, opis = 'Pocetnici.' ", izmena);
        assertFalse(izmena.toLowerCase().contains("naziv"));
        assertFalse(izmena.toLowerCase().contains("kategorija"));
    }
}
