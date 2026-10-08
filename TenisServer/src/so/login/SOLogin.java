/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package so.login;

import controller.ServerController;
import db.DBBroker;
import domain.AbstractDomainObject;
import domain.Administrator;
import java.util.ArrayList;
import so.AbstractSO;

/**
 *
 * @author Korisnik
 */
public class SOLogin extends AbstractSO {

    Administrator ulogovani;

    @Override
    protected void validate(AbstractDomainObject ado) throws Exception {
        if (!(ado instanceof Administrator)) {
            throw new Exception("Prosledjeni objekat nije instanca klase Administrator!");
        }

        Administrator a = (Administrator) ado;
        
        if (ServerController.getInstance().getUlogovaniAdministratori().size() == 3) {
            throw new Exception("Vec je prijavljeno tri korisnika na sistem!");
        }

        for (Administrator administrator : ServerController.getInstance().getUlogovaniAdministratori()) {
            if (administrator.getUsername().equals(a.getUsername())) {
                throw new Exception("Ovaj administrator je vec ulogovan na sistem!");
            }
        }

    }

    @Override
    protected void execute(AbstractDomainObject ado) throws Exception {

        Administrator a = (Administrator) ado;

        ArrayList<Administrator> administratori
                = (ArrayList<Administrator>) (ArrayList<?>) DBBroker.getInstance().select(ado);

        for (Administrator administrator : administratori) {
            if (administrator.getUsername().equals(a.getUsername())
                    && administrator.getPassword().equals(a.getPassword())) {
                ulogovani = administrator;
                ServerController.getInstance().getUlogovaniAdministratori().add(administrator);
                return;
            }
        }

        throw new Exception("Ne postoji administrator sa tim kredencijalima.");

    }

    public Administrator getUlogovani() {
        return ulogovani;
    }

}
