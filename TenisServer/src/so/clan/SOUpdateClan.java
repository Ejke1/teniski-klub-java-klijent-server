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
import java.util.regex.Pattern;
import so.AbstractSO;

/**
 *
 * @author Korisnik
 */
public class SOUpdateClan extends AbstractSO {

    private static final Pattern EMAIL_PATTERN
            = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");

    private static final Pattern TELEFON_PATTERN
            = Pattern.compile("^06[0-9]{8}$");

    @Override
    protected void validate(AbstractDomainObject ado) throws Exception {
        if (!(ado instanceof Clan)) {
            throw new Exception("Prosledjeni objekat nije instanca klase Clan!");
        }

        Clan c = (Clan) ado;

        if (!EMAIL_PATTERN.matcher(c.getEmail()).matches()) {
            throw new Exception("Email nije u ispravnom formatu!");
        }

        if (!TELEFON_PATTERN.matcher(c.getTelefon()).matches()) {
            throw new Exception("Telefon mora biti u formatu 06XXXXXXXX!");
        }

        if (c.getGodine() < 5 || c.getGodine() > 65) {
            throw new Exception("Godine moraju biti izmedju 5 i 65!");
        }

        ArrayList<Clan> clanovi = (ArrayList<Clan>) (ArrayList<?>) DBBroker.getInstance()
                .select(new Clan(null, "", "", 0, "", "", null));

        for (Clan clan : clanovi) {
            if (!clan.getClanID().equals(c.getClanID())) {
                if (clan.getEmail().equalsIgnoreCase(c.getEmail())) {
                    throw new Exception("Clan sa tim emailom vec postoji!");
                }
                if (clan.getTelefon().equals(c.getTelefon())) {
                    throw new Exception("Clan sa tim telefonom vec postoji!");
                }
            }
        }
    }

    @Override
    protected void execute(AbstractDomainObject ado) throws Exception {
        DBBroker.getInstance().update(ado);
    }

}
