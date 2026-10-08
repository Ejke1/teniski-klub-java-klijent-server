/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package so.clan;

import db.DBBroker;
import domain.AbstractDomainObject;
import domain.Clan;
import java.util.ArrayList;
import so.AbstractSO;

/**
 *
 * @author Korisnik
 */
public class SOGetAllClan extends AbstractSO {

    private ArrayList<Clan> lista;

    @Override
    protected void validate(AbstractDomainObject ado) throws Exception {
        if (!(ado instanceof Clan)) {
            throw new Exception("Prosledjeni objekat nije instanca klase Clan!");
        }
    }

    @Override
    protected void execute(AbstractDomainObject ado) throws Exception {
        ArrayList<AbstractDomainObject> clanovi = DBBroker.getInstance().select(ado);
        lista = (ArrayList<Clan>) (ArrayList<?>) clanovi;
    }

    public ArrayList<Clan> getLista() {
        return lista;
    }

}
