package com.pavelurusov.conway;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Life extends Application {

    @Override
    public void start(Stage stage) throws Exception{
        // set up the scene graph
        Parent root = FXMLLoader.load(getClass().getResource("mainWindow.fxml"));
        stage.setTitle("Conway’s Game of Life");
        stage.setScene(new Scene(root));
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
