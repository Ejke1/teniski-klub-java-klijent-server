/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package thread;

import controller.ServerController;
import domain.Administrator;
import domain.Clan;
import domain.Grupa;
import domain.Kategorija;
import domain.Trener;
import java.io.EOFException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.logging.Level;
import java.util.logging.Logger;
import transfer.Request;
import transfer.Response;
import transfer.util.ResponseStatus;
import transfer.util.Operation;

/**
 *
 * @author Korisnik
 */
public class ThreadClient extends Thread {

    private Socket socket;

    ThreadClient(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            while (!socket.isClosed()) {
                
                ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
                Request request = (Request) in.readObject();
                
                Response response = handleRequest(request);
                
                ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                out.writeObject(response);
            }
        } catch (EOFException | SocketException e) {
            System.out.println("Klijent je prekinuo vezu.");
        } catch (Exception e) {
            Logger.getLogger(ThreadClient.class.getName()).log(Level.SEVERE, "Greska u komunikaciji sa klijentom.", e);
        }
    }

    private Response handleRequest(Request request) {
        Response response = new Response(null, null, ResponseStatus.Success);
        try {
            switch (request.getOperation()) {
                case Operation.ADD_CLAN:
                    ServerController.getInstance().addClan((Clan) request.getData());
                    break;
                case Operation.ADD_GRUPA:
                    response.setData(ServerController.getInstance().addGrupa((Grupa) request.getData()));
                    break;
                case Operation.DELETE_CLAN:
                    ServerController.getInstance().deleteClan((Clan) request.getData());
                    break;
                case Operation.DELETE_GRUPA:
                    ServerController.getInstance().deleteGrupa((Grupa) request.getData());
                    break;
                case Operation.UPDATE_CLAN:
                    ServerController.getInstance().updateClan((Clan) request.getData());
                    break;
                case Operation.UPDATE_GRUPA:
                    response.setData(ServerController.getInstance().updateGrupa((Grupa) request.getData()));
                    break;
                case Operation.GET_ALL_CLAN:
                    response.setData(ServerController.getInstance().getAllClan((Clan) request.getData()));
                    break;
                case Operation.GET_ALL_GRUPA:
                    response.setData(ServerController.getInstance().getAllGrupa((Grupa) request.getData()));
                    break;
                case Operation.ADD_TRENER:
                    ServerController.getInstance().addTrener((Trener) request.getData());
                    break;
                case Operation.DELETE_TRENER:
                    ServerController.getInstance().deleteTrener((Trener) request.getData());
                    break;
                case Operation.UPDATE_TRENER:
                    ServerController.getInstance().updateTrener((Trener) request.getData());
                    break;
                case Operation.GET_ALL_TRENER:
                    response.setData(ServerController.getInstance().getAllTrener((Trener) request.getData()));
                    break;
                case Operation.GET_ALL_KATEGORIJA:
                    response.setData(ServerController.getInstance().getAllKategorija((Kategorija) request.getData()));
                    break;
                case Operation.LOGIN:
                    Administrator administrator = (Administrator) request.getData();
                    Administrator admin = ServerController.getInstance().login(administrator);
                    response.setData(admin);
                    break;
                case Operation.LOGOUT:
                    Administrator ulogovani = (Administrator) request.getData();
                    ServerController.getInstance().logout(ulogovani);
                    break;
                default:
                    return null;
            }
        } catch (Exception ex) {
            response.setResponseStatus(ResponseStatus.Error);
            response.setException(ex);
        }
        return response;
    }

}
