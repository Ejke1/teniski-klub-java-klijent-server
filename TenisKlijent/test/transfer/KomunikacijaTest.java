package transfer;

import domain.Administrator;
import domain.Clan;
import domain.ClanGrupe;
import domain.Grupa;
import domain.Kategorija;
import domain.Trener;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import static org.junit.Assert.assertEquals;
import org.junit.Test;
import transfer.util.Operation;
import transfer.util.ResponseStatus;

public class KomunikacijaTest {

    private static Object prenesi(Object o) throws Exception {
        ByteArrayOutputStream bajtovi = new ByteArrayOutputStream();
        try (ObjectOutputStream out = new ObjectOutputStream(bajtovi)) {
            out.writeObject(o);
        }
        try (ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bajtovi.toByteArray()))) {
            return in.readObject();
        }
    }

    @Test
    public void zahtevZaKreiranjeGrupePrenosiGrupuSaClanovima() throws Exception {
        Kategorija k = new Kategorija(1L, "Pocetni", "Opis");
        Grupa g = new Grupa(null, "Test mail", "Opis", 2, 10, k,
                new Trener(1L, "Ivan", "Ristic", "0601010101", 8),
                new Administrator(1L, "Aleksandar", "Eic", "coa", "coa123"), new ArrayList<>());
        g.getClanoviGrupe().add(new ClanGrupe(g, 1, "/",
                new Clan(1L, "Lana", "Stojanovic", 9, "lana@test.rs", "0641112222", k)));
        g.getClanoviGrupe().add(new ClanGrupe(g, 2, "Ne moze petkom.",
                new Clan(2L, "Uros", "Milosevic", 10, "uros@test.rs", "0643334444", k)));

        Request primljen = (Request) prenesi(new Request(Operation.ADD_GRUPA, g));

        assertEquals(Operation.ADD_GRUPA, primljen.getOperation());
        Grupa pg = (Grupa) primljen.getData();
        assertEquals("Test mail", pg.getNaziv());
        assertEquals("Ivan", pg.getTrener().getIme());
        assertEquals(2, pg.getClanoviGrupe().size());
        assertEquals("Uros", pg.getClanoviGrupe().get(1).getClan().getIme());
        assertEquals("Ne moze petkom.", pg.getClanoviGrupe().get(1).getNapomena());
    }

    @Test
    public void odgovorSaGreskomPrenosiPorukuServera() throws Exception {
        Response odgovor = new Response(null, new Exception("Morate uneti barem 2 clana!"), ResponseStatus.Error);

        Response primljen = (Response) prenesi(odgovor);

        assertEquals(ResponseStatus.Error, primljen.getResponseStatus());
        assertEquals("Morate uneti barem 2 clana!", primljen.getException().getMessage());
    }

    @Test
    public void uspesanOdgovorPrenosiIzvestajOSlanjuMaila() throws Exception {
        String izvestaj = "Mail sa PDF spiskom clanova je poslat na 2 od 2 clanova grupe.";

        Response primljen = (Response) prenesi(new Response(izvestaj, null, ResponseStatus.Success));

        assertEquals(ResponseStatus.Success, primljen.getResponseStatus());
        assertEquals(izvestaj, primljen.getData());
    }
}
