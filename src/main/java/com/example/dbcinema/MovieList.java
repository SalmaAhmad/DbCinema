package org.example.cinemadb;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class MovieList extends Application {

    public static double ROOT_WIDTH;
    public static double ROOT_HEIGHT;
    @Override

    public void start(Stage primaryStage) throws Exception {
        // Load the FXML file
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("Movie-List.fxml")));

        // Create the scene
        Scene scene = new Scene(root, 1000, 700);

        // Set up the stage
        primaryStage.setTitle("Cinema Booking System");
        primaryStage.setScene(root.getScene());
        primaryStage.show();
        ROOT_WIDTH = scene.getWidth();
        ROOT_HEIGHT = scene.getHeight();
    }


    public static void main(String[] args) {
        launch(args);
    }
}

