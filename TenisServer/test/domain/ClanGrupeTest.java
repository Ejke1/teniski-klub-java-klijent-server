package domain;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import org.junit.Test;

public class ClanGrupeTest {

    private final Grupa grupa = new Grupa(1L, "Mini Tenis A", "Opis", 2, 10, null, null, null, null);
    private final Clan uros = new Clan(2L, "Uros", "Milosevic", 10, "uros@test.rs", "0643334444", null);

    @Test
    public void konstruktorGeteriISeteri() {
        ClanGrupe cg = new ClanGrupe(grupa, 2, "Ne moze petkom.", uros);

        assertSame(grupa, cg.getGrupa());
        assertEquals(2, cg.getRb());
        assertEquals("Ne moze petkom.", cg.getNapomena());
        assertSame(uros, cg.getClan());

        ClanGrupe nova = new ClanGrupe();
        nova.setGrupa(grupa);
        nova.setRb(5);
        nova.setNapomena("/");
        nova.setClan(uros);
        assertSame(grupa, nova.getGrupa());
        assertEquals(5, nova.getRb());
        assertEquals("/", nova.getNapomena());
        assertSame(uros, nova.getClan());
    }

    @Test
    public void sqlDelovi() {
        ClanGrupe cg = new ClanGrupe(grupa, 2, "/", uros);

        assertEquals(" ClanGrupe ", cg.nazivTabele());
        assertEquals(" cg ", cg.alijas());
        assertEquals(" JOIN CLAN C ON (C.CLANID = CG.CLANID)\n"
                + "JOIN GRUPA G ON (G.GRUPAID = CG.GRUPAID)\n"
                + "JOIN TRENER T ON (T.TRENERID = G.TRENERID)\n"
                + "JOIN KATEGORIJA K ON (K.KATEGORIJAID = G.KATEGORIJAID)\n"
                + "JOIN ADMINISTRATOR A ON (A.ADMINISTRATORID = G.ADMINISTRATORID) ", cg.join());
        assertEquals(" (grupaID, rb, napomena, clanID) ", cg.koloneZaInsert());
        assertEquals(" 1, 2, '/', 2 ", cg.vrednostiZaInsert());
        assertEquals(" napomena = '/', clanID = 2", cg.vrednostiZaUpdate());
        assertEquals(" GRUPAID = 1 AND RB = 2", cg.uslov());
        assertEquals(" WHERE G.GRUPAID = 1", cg.dodatniUslov());
        assertEquals(" ORDER BY RB ASC ", cg.orderBy());
    }
}
