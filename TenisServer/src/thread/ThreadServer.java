/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package thread;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Korisnik
 */
public class ThreadServer extends Thread {

    private ServerSocket serverSocket;

    public ThreadServer() {
        this(Integer.getInteger("server.port", 9000));
    }

    public ThreadServer(int port) {
        try {
            serverSocket = new ServerSocket(port);
        } catch (Exception e) {
            Logger.getLogger(ThreadServer.class.getName()).log(Level.SEVERE,
                    "Server ne moze da se pokrene na portu " + port + ".", e);
        }
    }

    @Override
    public void run() {
        if (serverSocket == null) {
            return;
        }
        try {
            while (!serverSocket.isClosed()) {
                Socket socket = serverSocket.accept();
                System.out.println("Klijent se povezao!");
                ThreadClient th = new ThreadClient(socket);
                th.start();
            }
        } catch (Exception e) {
            if (!serverSocket.isClosed()) {
                Logger.getLogger(ThreadServer.class.getName()).log(Level.SEVERE, "Greska u radu servera.", e);
                return;
            }
        }
        System.out.println("Server je zaustavljen.");
    }

    public ServerSocket getServerSocket() {
        return serverSocket;
    }

    public void setServerSocket(ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
    }

}
