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
                else if (receivedObject instanceof RequestDTO) {
                    RequestDTO request = (RequestDTO) receivedObject;
                    System.out.println("Received RequestDTO with screenIndicator: " + request.getScreenIndicator());
                    switch (request.getScreenIndicator()) {
                        case 5: // sending sequest to another player 
                            String receiverUsername = request.getReciver_username();

                            for (ClientHandler client : clients) {
                                System.out.println("Checking client: " + client.username);
                                if (receiverUsername.equals(client.username)) {
                                    request.setScreenIndicator(8);
                                    client.dos.writeObject(request);
                                    client.dos.flush();
                                    System.out.println("Sent request to client: " + receiverUsername);
                                    break;
                                }
                            }
                            break;
                        case 6: // accepting the request 
                            String senderUsername = request.getReciver_username();
                            System.out.println("Accepted request from: " + senderUsername);
                            for (ClientHandler client : clients) {
                                System.out.println("Checking client: " + client.username);
                                if (senderUsername.equals(client.username)) {
                                    request.setScreenIndicator(6);
                                    client.dos.writeObject(request);
                                    client.dos.flush();
                                    System.out.println("Sent acceptance to client: " + senderUsername);
                                    break;
                                }
                            }
                            break;
                        default:
                            System.out.println("Unhandled screenIndicator: " + request.getScreenIndicator());
                            break;
                    }
                }

                dos.flush();
            }
        } catch (SocketException ex) {
            Logger.getLogger(ClientHandler.class.getName()).log(Level.SEVERE, "SocketException: Connection reset", ex);
        } catch (IOException | ClassNotFoundException | SQLException ex) {
            Logger.getLogger(ClientHandler.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if (dis != null) {
                    dis.close();
                }
                if (dos != null) {
                    dos.close();
                }
                synchronized (clients) {
                    clients.remove(this);
                }
            } catch (IOException ex) {
                Logger.getLogger(ClientHandler.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
}
    private static void logConnectedClients() {
        System.out.println("Connected clients:");
        for (ClientHandler client : clients) {
            System.out.println("- " + client.username);
        }
    }
            
}
