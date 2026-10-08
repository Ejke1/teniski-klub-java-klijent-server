package so.grupa;

import domain.Administrator;
import domain.Grupa;
import domain.Kategorija;
import domain.Trener;
import java.util.ArrayList;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;
import testutil.TestBaza;

public class SOGetAllGrupaTest {

    @Before
    public void pripremiBazu() throws Exception {
        TestBaza.pripremi();
    }

    private ArrayList<Grupa> pretrazi(String tekst) throws Exception {
        Grupa kriterijum = new Grupa(null, tekst, "", 0, 0,
                new Kategorija(null, "", ""),
                new Trener(null, tekst, tekst, "", 0),
                new Administrator(null, "", "", "", ""),
                null);
        SOGetAllGrupa so = new SOGetAllGrupa();
        so.templateExecute(kriterijum);
        return so.getLista();
    }

    @Test
    public void ucitavaGrupeSaClanovimaTreneromIKategorijom() throws Exception {
        ArrayList<Grupa> grupe = pretrazi("");

        assertEquals(2, grupe.size());
        Grupa g = grupe.get(0);
        assertEquals("Mini Tenis A", g.getNaziv());
        assertEquals("Ivan", g.getTrener().getIme());
        assertEquals("Pocetni", g.getKategorija().getNaziv());
        assertEquals("coa", g.getAdministrator().getUsername());
        assertEquals(3, g.getClanoviGrupe().size());
        assertEquals(1, g.getClanoviGrupe().get(0).getRb());
        assertEquals("Lana", g.getClanoviGrupe().get(0).getClan().getIme());
        assertEquals("Ne moze petkom.", g.getClanoviGrupe().get(1).getNapomena());
        assertEquals("Petar", g.getClanoviGrupe().get(2).getClan().getIme());
    }

    @Test
    public void pretrazujePoNazivuGrupe() throws Exception {
        ArrayList<Grupa> grupe = pretrazi("srednji");

        assertEquals(1, grupe.size());
        assertEquals("Srednji Nivo A", grupe.get(0).getNaziv());
    }

    @Test
    public void pretrazujePoImenuTrenera() throws Exception {
        ArrayList<Grupa> grupe = pretrazi("tamara");

        assertEquals(1, grupe.size());
        assertEquals("Tamara", grupe.get(0).getTrener().getIme());
    }
}
