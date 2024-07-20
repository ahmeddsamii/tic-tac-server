package tic.tac.toe.server;
import DataAccessLayer.DAL;
import DataAccessLayer.PlayerDTO;
import DataAccessLayer.RequestDTO;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ClientHandler extends Thread {

private ObjectInputStream dis;
    private ObjectOutputStream dos;
    private static Vector<ClientHandler> clients = new Vector<>();
    private static ArrayList<PlayerDTO> onlinePlayers = new ArrayList<>();
    private String username;
    private Socket socket;

    public ClientHandler(Socket s, PlayerDTO playerDTO) {
        try {
            this.socket = s;
            dis = new ObjectInputStream(s.getInputStream());
            dos = new ObjectOutputStream(s.getOutputStream());
            this.username = playerDTO.getUsername(); 
            start();
        } catch (IOException ex) {
            Logger.getLogger(ClientHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    @Override
    public void run() {
        try {
            while (true) {
                Object receivedObject = dis.readObject();

                if (receivedObject instanceof PlayerDTO) {
                    PlayerDTO p = (PlayerDTO) receivedObject;
                    int screenIndicator = p.getScreenIndicator();
                    DAL dal = new DAL();

                    switch (screenIndicator) {
                        case -1: // case sign in 
                            if (dal.checdAndLogin(p)) {
                                dal.updateIpAndStatusOnline(p);
                                this.username = p.getUsername();
                                System.out.println(p.getUsername());
                                synchronized (clients) {
                                    clients.add(this);
                                    logConnectedClients();
                                }

                                dos.writeObject("true"); // sussefully log in 
                            } else {
                                dos.writeObject("false"); // failed log in 
                            }
                            break;
                        case 0: // sign up 
                            if (dal.isUsernameExists(p.getUsername())) {
                                dos.writeObject("This user already exists!"); // already exists user 
                            } else {
                                dal.insert(p);
                                dos.writeObject("Registered successfully"); // sussefully register 
                            }
                            break;
                        case 3:
                            onlinePlayers = dal.getOnlinePlayers();
                            for (PlayerDTO pp : onlinePlayers) {
                                dos.writeObject(pp); // send to all online player 
                            }
                            break;
                        case 4: // case signout 
                            System.out.println(p.getUsername());
                            dal.updateStatusOffline(p);
                            break;
                        default:
                            break;
                    }

                }
}
}
