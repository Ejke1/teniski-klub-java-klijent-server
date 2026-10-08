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
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import session.Session;
import transfer.Request;
import transfer.Response;
import transfer.util.ResponseStatus;
import transfer.util.Operation;

/**
 *
 * @author Korisnik
 */
public class ClientController {

    private static ClientController instance;

    private ClientController() {
    }

    public static ClientController getInstance() {
        if (instance == null) {
            instance = new ClientController();
        }
        return instance;
    }

    public Administrator login(Administrator administrator) throws Exception {
        return (Administrator) sendRequest(Operation.LOGIN, administrator);
    }

    public void logout(Administrator ulogovani) throws Exception {
        sendRequest(Operation.LOGOUT, ulogovani);
    }

    public void addClan(Clan clan) throws Exception {
        sendRequest(Operation.ADD_CLAN, clan);
    }

    public String addGrupa(Grupa grupa) throws Exception {
        return (String) sendRequest(Operation.ADD_GRUPA, grupa);
    }

    public void deleteClan(Clan clan) throws Exception {
        sendRequest(Operation.DELETE_CLAN, clan);
    }

    public void deleteGrupa(Grupa grupa) throws Exception {
        sendRequest(Operation.DELETE_GRUPA, grupa);
    }

    public void updateClan(Clan clan) throws Exception {
        sendRequest(Operation.UPDATE_CLAN, clan);
    }

    public String updateGrupa(Grupa grupa) throws Exception {
        return (String) sendRequest(Operation.UPDATE_GRUPA, grupa);
    }

    public ArrayList<Clan> getAllClan(Clan clan) throws Exception {
        return (ArrayList<Clan>) sendRequest(Operation.GET_ALL_CLAN, clan);
    }

    public ArrayList<Grupa> getAllGrupa(Grupa grupa) throws Exception {
        return (ArrayList<Grupa>) sendRequest(Operation.GET_ALL_GRUPA, grupa);
    }

    public ArrayList<Kategorija> getAllKategorija(Kategorija kategorija) throws Exception {
        return (ArrayList<Kategorija>) sendRequest(Operation.GET_ALL_KATEGORIJA, kategorija);
    }

    public void addTrener(Trener trener) throws Exception {
        sendRequest(Operation.ADD_TRENER, trener);
    }

    public void deleteTrener(Trener trener) throws Exception {
        sendRequest(Operation.DELETE_TRENER, trener);
    }

    public void updateTrener(Trener trener) throws Exception {
        sendRequest(Operation.UPDATE_TRENER, trener);
    }

    public ArrayList<Trener> getAllTrener(Trener trener) throws Exception {
        return (ArrayList<Trener>) sendRequest(Operation.GET_ALL_TRENER, trener);
    }

    private synchronized Object sendRequest(int operation, Object data) throws Exception {
        Request request = new Request(operation, data);

      
        ObjectOutputStream out = new ObjectOutputStream(Session.getInstance().getSocket().getOutputStream());
        out.writeObject(request);

        
        ObjectInputStream in = new ObjectInputStream(Session.getInstance().getSocket().getInputStream());
        Response response = (Response) in.readObject();

        if (response.getResponseStatus().equals(ResponseStatus.Error)) {
            throw response.getException();
        } else {
            return response.getData();
        }

    }

}
