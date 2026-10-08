package transfer;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import org.junit.Test;
import transfer.util.Operation;
import transfer.util.ResponseStatus;

public class TransferTest {

    @Test
    public void zahtevGeteriISeteri() {
        Request prazan = new Request();
        assertNull(prazan.getData());

        Object podaci = new Object();
        prazan.setOperation(Operation.ADD_CLAN);
        prazan.setData(podaci);

        assertEquals(Operation.ADD_CLAN, prazan.getOperation());
        assertSame(podaci, prazan.getData());
    }

    @Test
    public void odgovorGeteriISeteri() {
        Response prazan = new Response();
        assertNull(prazan.getData());
        assertNull(prazan.getException());
        assertNull(prazan.getResponseStatus());

        Exception greska = new Exception("greska");
        prazan.setData("podaci");
        prazan.setException(greska);
        prazan.setResponseStatus(ResponseStatus.Error);

        assertEquals("podaci", prazan.getData());
        assertSame(greska, prazan.getException());
        assertEquals(ResponseStatus.Error, prazan.getResponseStatus());
    }

    @Test
    public void svakaOperacijaImaJedinstvenBroj() throws Exception {
        Set<Integer> brojevi = new HashSet<>();
        for (Field f : Operation.class.getFields()) {
            brojevi.add(f.getInt(null));
        }

        assertEquals(15, Operation.class.getFields().length);
        assertEquals(15, brojevi.size());
        assertEquals(0, Operation.LOGIN);
        assertEquals(14, Operation.GET_ALL_KATEGORIJA);
    }

    @Test
    public void statusOdgovora() {
        assertEquals(2, ResponseStatus.values().length);
        assertEquals(ResponseStatus.Success, ResponseStatus.valueOf("Success"));
        assertEquals(ResponseStatus.Error, ResponseStatus.valueOf("Error"));
    }
}
