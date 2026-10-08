/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package so.trener;

import db.DBBroker;
import domain.AbstractDomainObject;
import domain.Trener;
import java.util.ArrayList;
import so.AbstractSO;

/**
 *
 * @author Korisnik
 */
public class SOGetAllTrener extends AbstractSO {

    private ArrayList<Trener> lista;

    @Override
    protected void validate(AbstractDomainObject ado) throws Exception {
        if (!(ado instanceof Trener)) {
            throw new Exception("Prosledjeni objekat nije instanca klase Trener!");
        }
    }

    @Override
    protected void execute(AbstractDomainObject ado) throws Exception {
        ArrayList<AbstractDomainObject> treneri = DBBroker.getInstance().select(ado);
        lista = (ArrayList<Trener>) (ArrayList<?>) treneri;
    }

    public ArrayList<Trener> getLista() {
        return lista;
    }

}
