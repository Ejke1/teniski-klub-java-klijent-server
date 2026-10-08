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
import java.util.regex.Pattern;
import so.AbstractSO;

/**
 *
 * @author Korisnik
 */
public class SOUpdateTrener extends AbstractSO {

    private static final Pattern TELEFON_PATTERN
            = Pattern.compile("^06[0-9]{8}$");

    @Override
    protected void validate(AbstractDomainObject ado) throws Exception {
        if (!(ado instanceof Trener)) {
            throw new Exception("Prosledjeni objekat nije instanca klase Trener!");
        }

        Trener t = (Trener) ado;

        if (!TELEFON_PATTERN.matcher(t.getBrojTelefona()).matches()) {
            throw new Exception("Telefon mora biti u formatu 06XXXXXXXX!");
        }

        if (t.getGodineIskustva() < 5 || t.getGodineIskustva() > 70) {
            throw new Exception("Godine moraju biti izmedju 5 i 70!");
        }

        ArrayList<Trener> treneri = (ArrayList<Trener>) (ArrayList<?>) DBBroker.getInstance()
                .select(new Trener(null, "", "", "", 0));

        for (Trener trener : treneri) {
            if (!trener.getTrenerID().equals(t.getTrenerID())) {
                if (trener.getBrojTelefona().equals(t.getBrojTelefona())) {
                    throw new Exception("Trener sa tim brojem telefona vec postoji!");
                }
            }
        }

    }

    @Override
    protected void execute(AbstractDomainObject ado) throws Exception {
        DBBroker.getInstance().update(ado);
    }

}
