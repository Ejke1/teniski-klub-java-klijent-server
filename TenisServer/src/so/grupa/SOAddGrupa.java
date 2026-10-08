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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import so.AbstractSO;

/**
 *
 * @author Korisnik
 */
public class SOAddGrupa extends AbstractSO {

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

        ArrayList<Grupa> grupe = (ArrayList<Grupa>) (ArrayList<?>) DBBroker.getInstance().select(ado);

        for (Grupa grupa : grupe) {
            if (grupa.getNaziv().equals(g.getNaziv())) {
                throw new Exception("Grupa sa tim nazivom vec postoji!");
            }
        }

    }

    @Override
    protected void execute(AbstractDomainObject ado) throws Exception {
        PreparedStatement ps = DBBroker.getInstance().insert(ado);

       
        ResultSet tableKeys = ps.getGeneratedKeys();
        tableKeys.next();
        Long grupaID = tableKeys.getLong(1);

       
        Grupa novaGrupa = (Grupa) ado;
        novaGrupa.setGrupaID(grupaID);

     
        for (ClanGrupe clanGrupe : novaGrupa.getClanoviGrupe()) {
            clanGrupe.setGrupa(novaGrupa);
            DBBroker.getInstance().insert(clanGrupe);
        }

    }

}
