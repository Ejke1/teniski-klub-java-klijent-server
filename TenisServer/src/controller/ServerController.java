/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controller;

import domain.Administrator;
import domain.Clan;
import domain.Grupa;
import domain.Kategorija;
import domain.Trener;
import java.util.ArrayList;
import mail.ObavestenjeGrupe;
import so.clan.SOAddClan;
import so.clan.SODeleteClan;
import so.clan.SOGetAllClan;
import so.clan.SOUpdateClan;
import so.grupa.SOAddGrupa;
import so.grupa.SODeleteGrupa;
import so.grupa.SOGetAllGrupa;
import so.grupa.SOUpdateGrupa;
import so.kategorija.SOGetAllKategorija;
import so.login.SOLogin;
import so.trener.SOAddTrener;
import so.trener.SODeleteTrener;
import so.trener.SOGetAllTrener;
import so.trener.SOUpdateTrener;

/**
 *
 * @author Korisnik
 */
public class ServerController {

    private static ServerController instance;
    private ArrayList<Administrator> ulogovaniAdministratori = new ArrayList<>();

    private ServerController() {
    }

    public static ServerController getInstance() {
        if (instance == null) {
            instance = new ServerController();
        }
        return instance;
    }

    public ArrayList<Administrator> getUlogovaniAdministratori() {
        return ulogovaniAdministratori;
    }

    public void setUlogovaniAdministratori(ArrayList<Administrator> ulogovaniAdministratori) {
        this.ulogovaniAdministratori = ulogovaniAdministratori;
    }

    public Administrator login(Administrator administrator) throws Exception {
        SOLogin so = new SOLogin();
        so.templateExecute(administrator);
        return so.getUlogovani();
    }

    public void addClan(Clan clan) throws Exception {
        (new SOAddClan()).templateExecute(clan);
    }

    public String addGrupa(Grupa grupa) throws Exception {
        mail.MailLog.upisi("Kreiranje grupe \"" + grupa.getNaziv() + "\" - cuvam u bazi...");
        (new SOAddGrupa()).templateExecute(grupa);
        mail.MailLog.upisi("Grupa sacuvana u bazi.");
        return ObavestenjeGrupe.posalji(grupa, true);
    }

    public void deleteClan(Clan clan) throws Exception {
        (new SODeleteClan()).templateExecute(clan);
    }

    public void deleteGrupa(Grupa grupa) throws Exception {
        (new SODeleteGrupa()).templateExecute(grupa);
    }

    public void updateClan(Clan clan) throws Exception {
        (new SOUpdateClan()).templateExecute(clan);
    }

    public String updateGrupa(Grupa grupa) throws Exception {
        mail.MailLog.upisi("Izmena grupe \"" + grupa.getNaziv() + "\" - cuvam u bazi...");
        (new SOUpdateGrupa()).templateExecute(grupa);
        mail.MailLog.upisi("Grupa sacuvana u bazi.");
        return ObavestenjeGrupe.posalji(grupa, false);
    }

    public ArrayList<Clan> getAllClan(Clan clan) throws Exception {
        SOGetAllClan so = new SOGetAllClan();
        so.templateExecute(clan);
        return so.getLista();
    }

    public ArrayList<Grupa> getAllGrupa(Grupa grupa) throws Exception {
        SOGetAllGrupa so = new SOGetAllGrupa();
        so.templateExecute(grupa);
        return so.getLista();
    }

    public ArrayList<Kategorija> getAllKategorija(Kategorija kategorija) throws Exception {
        SOGetAllKategorija so = new SOGetAllKategorija();
        so.templateExecute(kategorija);
        return so.getLista();
    }

    public void logout(Administrator ulogovani) {
        ulogovaniAdministratori.remove(ulogovani);
    }

    public void addTrener(Trener trener) throws Exception {
        (new SOAddTrener()).templateExecute(trener);
    }

    public void deleteTrener(Trener trener) throws Exception {
        (new SODeleteTrener()).templateExecute(trener);
    }

    public void updateTrener(Trener trener) throws Exception {
        (new SOUpdateTrener()).templateExecute(trener);
    }

    public ArrayList<Trener> getAllTrener(Trener trener) throws Exception {
        SOGetAllTrener so = new SOGetAllTrener();
        so.templateExecute(trener);
        return so.getLista();
    }

}
