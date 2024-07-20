package tic.toe.server;

import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import tic.tac.toe.server.ServerUI;

public class TicTacToeServerMain extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        Parent root = new ServerUI();
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }
    public static void main(String[] args) {
        launch(args);
    }

}