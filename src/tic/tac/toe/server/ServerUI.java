package tic.tac.toe.server;

import DataAccessLayer.DAL;
import DataAccessLayer.PlayerDTO;
import java.io.IOException;
import java.lang.reflect.Array;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Font;

public class ServerUI extends AnchorPane implements Runnable {

    protected final AnchorPane anchorPane;
    protected final Button _btnStart;
    protected final Button _btnStop;
    protected final Label label;
    protected final Label _offlineLabel;
    protected final Label label0;
    protected final Label _onlineLabel;
    protected final Label label1;
    protected final Label _allPlayerLabel;

    ServerSocket ss;
    Socket s;
    boolean isServerRunning;

    public ServerUI() {

        anchorPane = new AnchorPane();
        _btnStart = new Button();
        _btnStop = new Button();
        label = new Label();
        _offlineLabel = new Label();
        label0 = new Label();
        _onlineLabel = new Label();
        label1 = new Label();
        _allPlayerLabel = new Label();
        isServerRunning = false;

        setMaxHeight(USE_PREF_SIZE);
        setMaxWidth(USE_PREF_SIZE);
        setMinHeight(USE_PREF_SIZE);
        setMinWidth(USE_PREF_SIZE);
        setPrefHeight(600.0);
        setPrefWidth(800.0);
        setStyle("-fx-background-color: #008080;");

        AnchorPane.setBottomAnchor(anchorPane, 20.0);
        AnchorPane.setLeftAnchor(anchorPane, 20.0);
        AnchorPane.setRightAnchor(anchorPane, 20.0);
        AnchorPane.setTopAnchor(anchorPane, 20.0);
        anchorPane.setPrefHeight(600.0);
        anchorPane.setPrefWidth(750.0);
        anchorPane.setStyle("-fx-background-color: #FFA500;");

        _btnStart.setLayoutX(14.0);
        _btnStart.setLayoutY(388.0);
        _btnStart.setMnemonicParsing(false);
        _btnStart.setOnAction(this::handleOnBtnStart);
        _btnStart.setPrefHeight(84.0);
        _btnStart.setPrefWidth(280.0);
        _btnStart.setStyle("-fx-background-color: #008080; -fx-background-radius: 50;");
        _btnStart.setText("Start Server");
        _btnStart.setTextFill(javafx.scene.paint.Color.ORANGE);
        _btnStart.setFont(new Font("System Bold Italic", 40.0));

        _btnStop.setLayoutX(466.0);
        _btnStop.setLayoutY(388.0);
        _btnStop.setMnemonicParsing(false);
        _btnStop.setOnAction(this::handleOnBtnStop);
        _btnStop.setPrefHeight(86.0);
        _btnStop.setPrefWidth(280.0);
        _btnStop.setStyle("-fx-background-color: #008080; -fx-background-radius: 50;");
        _btnStop.setText("Stop Server");
        _btnStop.setTextAlignment(javafx.scene.text.TextAlignment.RIGHT);
        _btnStop.setTextFill(javafx.scene.paint.Color.valueOf("#b21515"));
        _btnStop.setFont(new Font("System Bold Italic", 40.0));

        label.setLayoutX(477.0);
        label.setLayoutY(202.0);
        label.setText("Offline:");
        label.setTextFill(javafx.scene.paint.Color.valueOf("#ea1414"));
        label.setFont(new Font("Showcard Gothic", 41.0));

        _offlineLabel.setLayoutX(651.0);
        _offlineLabel.setLayoutY(202.0);
        _offlineLabel.setPrefHeight(49.0);
        _offlineLabel.setPrefWidth(29.0);
        _offlineLabel.setText("0");
        _offlineLabel.setTextFill(javafx.scene.paint.Color.valueOf("#ea1414"));
        _offlineLabel.setFont(new Font("Showcard Gothic", 40.0));

        label0.setLayoutX(62.0);
        label0.setLayoutY(202.0);
        label0.setText("Online:");
        label0.setTextFill(javafx.scene.paint.Color.valueOf("#0aae12"));
        label0.setFont(new Font("Showcard Gothic", 41.0));

        _onlineLabel.setLayoutX(217.0);
        _onlineLabel.setLayoutY(202.0);
        _onlineLabel.setPrefHeight(49.0);
        _onlineLabel.setPrefWidth(29.0);
        _onlineLabel.setText("0");
        _onlineLabel.setTextFill(javafx.scene.paint.Color.valueOf("#0aae12"));
        _onlineLabel.setFont(new Font("Showcard Gothic", 40.0));

        label1.setLayoutX(206.0);
        label1.setLayoutY(52.0);
        label1.setText("All PLAYERS:");
        label1.setFont(new Font("Showcard Gothic", 41.0));

        _allPlayerLabel.setLayoutX(483.0);
        _allPlayerLabel.setLayoutY(52.0);
        _allPlayerLabel.setPrefHeight(49.0);
        _allPlayerLabel.setPrefWidth(29.0);
        _allPlayerLabel.setText("0");
        _allPlayerLabel.setTextFill(javafx.scene.paint.Color.valueOf("#252525"));
        _allPlayerLabel.setFont(new Font("Showcard Gothic", 40.0));

        anchorPane.getChildren().add(_btnStart);
        anchorPane.getChildren().add(_btnStop);
        anchorPane.getChildren().add(label);
        anchorPane.getChildren().add(_offlineLabel);
        anchorPane.getChildren().add(label0);
        anchorPane.getChildren().add(_onlineLabel);
        anchorPane.getChildren().add(label1);
        anchorPane.getChildren().add(_allPlayerLabel);
        getChildren().add(anchorPane);

    }

    protected void handleOnBtnStart(javafx.event.ActionEvent actionEvent) {
        if (!isServerRunning) {
            Thread serverThread = new Thread(this);
            serverThread.start();
        }

        DAL dal;
        try {
            dal = new DAL();
            ArrayList<PlayerDTO> _allPlayers = dal.getAllPlayers();
            

            _allPlayerLabel.setText(String.valueOf(_allPlayers.size()));
        } catch (SQLException ex) {
            Logger.getLogger(ServerUI.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    protected void handleOnBtnStop(javafx.event.ActionEvent actionEvent) {
        isServerRunning = false;
        try {
            if (ss != null && !ss.isClosed()) {
                ss.close();
                System.out.println("Server has closed");
            }
        } catch (IOException ex) {
            Logger.getLogger(ServerUI.class.getName()).log(Level.SEVERE, "Error closing server socket", ex);
        }
    }

    @Override
    public void run() {
        try {
            ss = new ServerSocket(6007); // accepting connection sent from the clients 
            isServerRunning = true;
            System.out.println("Server has started");

            while (isServerRunning) {
                try {
                    s = ss.accept();
                    
                    // Create a new PlayerDTO object for the client
                    PlayerDTO playerDTO = new PlayerDTO(); 
                    
                    new ClientHandler(s, playerDTO); // da el gded b2a (b3att lel ctor socket + playerDTO) >> now go to clientHandler ctor
                } catch (SocketException e) {
                    // Handle socket closed exception if needed
                    if (!ss.isClosed()) {
                        Logger.getLogger(ServerUI.class.getName()).log(Level.SEVERE, "Socket accept error", e);
                    }
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(ServerUI.class.getName()).log(Level.SEVERE, "Server start error", ex);
        } finally {
            // Ensure server socket is closed even if an exception occurs
            try {
                if (ss != null && !ss.isClosed()) {
                    ss.close();
                    System.out.println("Server socket closed in finally block");
                }
            } catch (IOException ex) {
                Logger.getLogger(ServerUI.class.getName()).log(Level.SEVERE, "Error closing server socket in finally", ex);
            }
        }
    }

}
