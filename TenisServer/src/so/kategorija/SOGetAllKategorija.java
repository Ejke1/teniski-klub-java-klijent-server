/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package so.kategorija;

import db.DBBroker;
import domain.AbstractDomainObject;
import domain.Kategorija;
import java.util.ArrayList;
import so.AbstractSO;

/**
 *
 * @author Korisnik
 */
public class SOGetAllKategorija extends AbstractSO {

    private ArrayList<Kategorija> lista;

    @Override
    protected void validate(AbstractDomainObject ado) throws Exception {
        if (!(ado instanceof Kategorija)) {
            throw new Exception("Prosledjeni objekat nije instanca klase Kategorija!");
        }
    }

    @Override
    protected void execute(AbstractDomainObject ado) throws Exception {
        ArrayList<AbstractDomainObject> kategorije = DBBroker.getInstance().select(ado);
        lista = (ArrayList<Kategorija>) (ArrayList<?>) kategorije;
    }

    public ArrayList<Kategorija> getLista() {
        return lista;
    }

}
