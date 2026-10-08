/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package so.grupa;

import db.DBBroker;
import domain.AbstractDomainObject;
import domain.ClanGrupe;
import domain.Grupa;
import java.util.ArrayList;
import so.AbstractSO;

/**
 *
 * @author Korisnik
 */
public class SOGetAllGrupa extends AbstractSO {

    private ArrayList<Grupa> lista;

    @Override
    protected void validate(AbstractDomainObject ado) throws Exception {
        if (!(ado instanceof Grupa)) {
            throw new Exception("Prosledjeni objekat nije instanca klase Grupa!");
        }
    }

    @Override
    protected void execute(AbstractDomainObject ado) throws Exception {

        ArrayList<AbstractDomainObject> grupe = DBBroker.getInstance().select(ado);
        lista = (ArrayList<Grupa>) (ArrayList<?>) grupe;


        for (Grupa trenutnaGrupa : lista) {

           
            ClanGrupe clanGrupe = new ClanGrupe();
            clanGrupe.setGrupa(trenutnaGrupa);


            ArrayList<ClanGrupe> clanoviTrenutneGrupe
                    = (ArrayList<ClanGrupe>) (ArrayList<?>) DBBroker.getInstance().select(clanGrupe);


            trenutnaGrupa.setClanoviGrupe(clanoviTrenutneGrupe);
        }
    }

    public ArrayList<Grupa> getLista() {
        return lista;
    }

}
