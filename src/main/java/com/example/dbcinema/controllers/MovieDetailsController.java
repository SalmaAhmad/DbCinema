package org.example.cinemadb;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class MovieDetailsController {
    private Parent previousSceneRoot;
    @FXML
    private Label movieTitleLabel;
    @FXML
    private Label movieGenreLabel;
    @FXML
    private Label movieLanguageLabel;
    @FXML
    private Label movieDurationLabel;
    @FXML
    private FlowPane showtimeFlowPane;

    private Movie movie;

    public void setMovie(Movie movie) {
        this.movie = movie;
        loadMovieDetails();
        loadShowTimes();
    }

    private void loadMovieDetails() {
        if (movie != null) {
            movieTitleLabel.setText(movie.getName());
            movieGenreLabel.setText("Genre: " + movie.getGenre());
            movieLanguageLabel.setText("Language: " + movie.getLanguage());
            movieDurationLabel.setText("Duration: " + movie.getDuration());
        }
    }



    private void loadShowTimes() {
        try (Connection conn = SQLServerConnect.getConnection();
             CallableStatement stmt = conn.prepareCall("{call GetShowTimesByMovie(?)}")) {

            stmt.setInt(1, movie.getMovieId());
            ResultSet rs = stmt.executeQuery();

            List<ShowTime> showTimes = new java.util.ArrayList<>();

            while (rs.next()) {
                int showtimeId = rs.getInt("Showtime_ID");
                int movieId = rs.getInt("Movie_ID");
                int hallId = rs.getInt("H_ID");

                Timestamp timestamp = rs.getTimestamp("Start_time");
                LocalTime startTime = timestamp != null ? timestamp.toLocalDateTime().toLocalTime() : null;

                LocalDate showDate = rs.getDate("Showdate").toLocalDate();
                String hallLocation = rs.getString("HallLocation");

                // Get end time using your CalculateEndTime function
                LocalTime endTime = null;
                try (PreparedStatement stmt2 = conn.prepareStatement("SELECT dbo.CalculateEndTime(?) AS EndTime")) {
                    stmt2.setInt(1, showtimeId);
                    ResultSet rs2 = stmt2.executeQuery();
                    if (rs2.next()) {
                        Time sqlTime = rs2.getTime("EndTime");
                        endTime = sqlTime != null ? sqlTime.toLocalTime() : null;
                    }
                }
                ShowTime showTime = new ShowTime(
                        showtimeId,
                        movieId,
                        hallId,
                        startTime,
                        endTime,
                        showDate,
                        hallLocation

                );

                showTimes.add(showTime);
            }

            createShowTimeCards(showTimes);

        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }


    private void createShowTimeCards(List<ShowTime> showTimes) {
        showtimeFlowPane.getChildren().clear(); // Clear existing cards

        for (ShowTime showTime : showTimes) {
            // Card container with cinema styling
            VBox card = new VBox(15);
            card.setStyle("-fx-background-color: rgba(40,70,124,0.7); " +
                    "-fx-background-radius: 10; " +
                    "-fx-padding: 20; " +
                    "-fx-border-color: #fd5b78; " +
                    "-fx-border-width: 1.5; " +
                    "-fx-border-radius: 10; " +
                    "-fx-effect: dropshadow(gaussian, rgba(253,91,120,0.3), 10, 0.5, 0, 2);");
            card.setPrefSize(230, 200);
            card.setAlignment(Pos.CENTER);

            // Time label with movie reel icon
            HBox timeContainer = new HBox(8);
            timeContainer.setAlignment(Pos.CENTER);
            Label timeIcon = new Label("🎬");
            Label timeLabel = new Label(showTime.toString());
            timeLabel.setStyle("-fx-text-fill: white; " +
                    "-fx-font-size: 18px; " +
                    "-fx-font-weight: bold;");
            timeContainer.getChildren().addAll(timeIcon, timeLabel);


            // Book button with hover effects
            Button bookButton = new Button("BOOK NOW");
            bookButton.setStyle("-fx-background-color: linear-gradient(to right, #fd5b78, #ff355e); " +
                    "-fx-text-fill: white; " +
                    "-fx-font-weight: bold; " +
                    "-fx-font-size: 14px; " +
                    "-fx-padding: 10 25; " +
                    "-fx-background-radius: 20;");
            bookButton.setCursor(Cursor.HAND);

            // Hover effects
            bookButton.setOnMouseEntered(e ->
                    bookButton.setStyle(bookButton.getStyle() +
                            "-fx-effect: dropshadow(gaussian, #ff355e, 15, 0.7, 0, 0);"));

            bookButton.setOnMouseExited(e ->
                    bookButton.setStyle(bookButton.getStyle()
                            .replace("-fx-effect: dropshadow(gaussian, #ff355e, 15, 0.7, 0, 0);", "")));

            bookButton.setOnAction(e -> openBookingWindow(showTime));

            card.getChildren().addAll(timeContainer,bookButton);
            showtimeFlowPane.getChildren().add(card);
        }
    }


    private void openBookingWindow(ShowTime showTime) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("Seats.fxml"));
            Parent root = loader.load();

            // Get the controller and set the showtime
            SeatsController seatsController = loader.getController();
            seatsController.setShowTime(showTime);

            // Create a new stage for the seat selection
            Stage seatStage = new Stage();
            seatStage.setTitle("Seat Selection - " + movie.getName());
            seatStage.setScene(new Scene(root, 800, 600));
            seatStage.show();

        } catch (IOException ex) {
            ex.printStackTrace();
            // Show error alert to user
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Could not open seat selection");
            alert.setContentText(ex.getMessage());
            alert.showAndWait();
        }
    }


    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }


    public void handleBackButtonAction(javafx.event.ActionEvent event) {
        Parent movieDetailsParent = null;
        try {
            movieDetailsParent = FXMLLoader.load(getClass().getResource("Movie-List.fxml"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // Get the current window (stage)
        Stage window = (Stage) ((Node) event.getSource()).getScene().getWindow();

        // Create a new scene with SAME size as current scene
        Scene currentScene = window.getScene();
        double width = currentScene.getWidth();
        double height = currentScene.getHeight();

        Scene movieDetailsScene = new Scene(movieDetailsParent, width, height);

        // Set the new scene to the window
        window.setScene(movieDetailsScene);
        window.show();
    }
    }
