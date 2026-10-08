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
import java.util.HashMap;
import so.AbstractSO;

/**
 *
 * @author Korisnik
 */
public class SOUpdateGrupa extends AbstractSO {

    @Override
    protected void validate(AbstractDomainObject ado) throws Exception {
        if (!(ado instanceof Grupa)) {
            throw new Exception("Prosledjeni objekat nije instanca klase Grupa!");
        }

        Grupa g = (Grupa) ado;

        if (g.getMaxKapacitet() < 2 || g.getMaxKapacitet() > 15) {
            throw new Exception("Maksimalni kapacitet mora biti izmedju 2 i 15!");
        }

        if (g.getClanoviGrupe().size() > g.getMaxKapacitet()) {
            throw new Exception("Maksimalni kapacitet je " + g.getMaxKapacitet() + ", "
                    + "a vi ste uneli " + g.getClanoviGrupe().size() + "!");
        }

        if (g.getClanoviGrupe().size() < 2) {
            throw new Exception("Morate uneti barem 2 clana!");
        }

    }

    @Override
    protected void execute(AbstractDomainObject ado) throws Exception {
        Grupa g = (Grupa) ado;


        DBBroker.getInstance().update(g);

    
        ArrayList<ClanGrupe> stariClanovi
                = (ArrayList<ClanGrupe>) (ArrayList<?>) DBBroker.getInstance()
                        .select(new ClanGrupe(g, 0, null, null));

        HashMap<Integer, ClanGrupe> mapaStarih = new HashMap<>();
        for (ClanGrupe cg : stariClanovi) {
            mapaStarih.put(cg.getRb(), cg);
        }


        HashMap<Integer, ClanGrupe> mapaNovih = new HashMap<>();
        for (ClanGrupe novi : g.getClanoviGrupe()) {
            mapaNovih.put(novi.getRb(), novi);
        }

       
        for (ClanGrupe stari : stariClanovi) {
            if (!mapaNovih.containsKey(stari.getRb())) {
                DBBroker.getInstance().delete(stari);
            }
        }

      
        for (ClanGrupe novi : g.getClanoviGrupe()) {
            if (mapaStarih.containsKey(novi.getRb())) {
                DBBroker.getInstance().update(novi);
            }
        }

      
        for (ClanGrupe novi : g.getClanoviGrupe()) {
            if (!mapaStarih.containsKey(novi.getRb())) {
                DBBroker.getInstance().insert(novi);
            }
        }
    }

}
