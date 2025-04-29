package org.example.cinemadb;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import static org.example.cinemadb.SQLServerConnect.getConnection;

public class MovieListController implements Initializable {
    @FXML private ListView<Movie> movieListView;
    @FXML private TextField searchField;
    @FXML
    private ComboBox<String> genreComboBox;



    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadMovies();
        searchMovies();
        movieListView.setCellFactory(listView -> new MovieListCell());
        populateGenreComboBox();
    }

    private void loadMovies() {
        try (Connection conn = SQLServerConnect.getConnection();
             CallableStatement stmt = conn.prepareCall("{call GetAllMovies()}")) { // stored procedure

            movieListView.getItems().clear();
            ResultSet rs = stmt.executeQuery(); // Execute the query

            while (rs.next()) {
                Movie movie = new Movie(
                        rs.getInt("Movie_ID"),
                        rs.getString("Name"),
                        rs.getString("Language"),
                        rs.getInt("Duration"),
                        rs.getString("Genre")
                );
                movieListView.getItems().add(movie);
            }
        } catch (SQLException e) {
            showAlert("Database Error", "Failed to load movies: " + e.getMessage());
        }
    }




    private void openMovieDetails(Movie movie) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("movie-details.fxml"));
            Parent root = loader.load();

            MovieDetailsController controller = loader.getController();
            controller.setMovie(movie);

            Stage stage = (Stage) movieListView.getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (IOException e) {
            showAlert("Error", "Failed to open movie details: " + e.getMessage());
        }
    }

    @FXML
    private void searchMovies() {
        String searchText = searchField.getText().toLowerCase();
        if (searchText.isEmpty()) {
            loadMovies();
            return;
        }

        try (Connection conn = getConnection();
             CallableStatement stmt = conn.prepareCall("{call SearchMovies(?)}")) {// here we called the stored procedure

            stmt.setString(1, searchText);
            ResultSet rs = stmt.executeQuery();

            movieListView.getItems().clear();
            while (rs.next()) {
                Movie movie = new Movie(
                        rs.getInt("Movie_ID"),
                        rs.getString("Name"),
                        rs.getString("Language"),
                        rs.getInt("Duration"),
                        rs.getString("Genre")
                );
                movieListView.getItems().add(movie);
            }
        } catch (SQLException e) {
            showAlert("Error", "Failed to search movies: " + e.getMessage());
        }
    }



    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }


    private void populateGenreComboBox() {
        // Clear existing items in ComboBox
        genreComboBox.getItems().clear();

        // Fetch genres from database and add them to the ComboBox
        try (Connection conn = SQLServerConnect.getConnection();
             CallableStatement stmt = conn.prepareCall("{call GetAvailableGenre}")) {  // Call the stored procedure

            ResultSet rs = stmt.executeQuery();  // Execute the procedure

            while (rs.next()) {
                String genre = rs.getString("Genre");
                genreComboBox.getItems().add(genre);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to fetch genres from the database.");
        }
    }


    @FXML
    private void filterByGenre() {
        String selectedGenre = genreComboBox.getValue();
        if (selectedGenre == null || selectedGenre.isEmpty()) return;

        List<Movie> filteredMovies = new ArrayList<>();

        try (Connection conn = SQLServerConnect.getConnection();
             CallableStatement stmt = conn.prepareCall("{call GetAvailableMoviesByGenre(?)}")) {

            stmt.setString(1, selectedGenre);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Movie movie = new Movie(
                        rs.getInt("Movie_ID"),
                        rs.getString("Name"),
                        rs.getString("Language"),
                        rs.getInt("Duration"),
                        rs.getString("Genre")
                );
                filteredMovies.add(movie);
            }
            updateMovieList(filteredMovies); // Method to refresh your ListView

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void updateMovieList(List<Movie> movies) {
        movieListView.getItems().clear();
        movieListView.getItems().addAll(movies);
    }


    private class MovieListCell extends ListCell<Movie> {
        @Override
        protected void updateItem(Movie movie, boolean empty) {
            super.updateItem(movie, empty);

            if (empty || movie == null) {
                setGraphic(null);
            } else {
                // Main card container with higher contrast background
                VBox card = new VBox(12);
                card.setStyle("-fx-background-color: linear-gradient(to bottom, #25255a, #30306a); " +  // Darker blue
                        "-fx-background-radius: 12; " +
                        "-fx-padding: 20; " +
                        "-fx-border-color: linear-gradient(to right, #fd5b78, #ff355e); " +
                        "-fx-border-width: 1.5; " +
                        "-fx-border-radius: 12; " +
                        "-fx-effect: dropshadow(gaussian, rgba(253,91,120,0.3), 12, 0.5, 0, 2);");

                // Movie title with improved contrast
                Label nameLabel = new Label(movie.getName());
                nameLabel.setStyle("-fx-text-fill: white; " +
                        "-fx-font-size: 18px; " +
                        "-fx-font-weight: bold; " +
                        "-fx-effect: dropshadow(gaussian, rgba(255,255,255,0.3), 0, 0, 0, 1);");

                // Details container with better contrast
                VBox detailsBox = new VBox(8);


                addDetailRow(detailsBox, "🎬 Genre", movie.getGenre());
                addDetailRow(detailsBox, "🌐 Language", movie.getLanguage());
                addDetailRow(detailsBox, "⏱ Duration", formatDuration(movie.getDuration()));


                // Contrast-enhanced button
                Button detailsButton = new Button("View Details →");
                detailsButton.setStyle("-fx-background-color: linear-gradient(to right, #ff355e, #fd5b78); " +  // Brighter gradient
                        "-fx-text-fill: white; " +
                        "-fx-font-weight: bold; " +
                        "-fx-font-size: 14px; " +
                        "-fx-padding: 8 20; " +
                        "-fx-background-radius: 20;");

                // Hover effect with more contrast
                detailsButton.setOnMouseEntered(e ->
                        detailsButton.setStyle(detailsButton.getStyle() +
                                "-fx-effect: dropshadow(gaussian, #ff355e, 15, 0.7, 0, 0); " +
                                "-fx-background-color: linear-gradient(to right, #ff6037, #fd5b78);"));

                detailsButton.setOnMouseExited(e ->
                        detailsButton.setStyle(detailsButton.getStyle()
                                .replace("-fx-effect: dropshadow(gaussian, #ff355e, 15, 0.7, 0, 0);", "")
                                .replace("-fx-background-color: linear-gradient(to right, #ff6037, #fd5b78);",
                                        "-fx-background-color: linear-gradient(to right, #ff355e, #fd5b78);")));

                detailsButton.setOnAction(e -> openMovieDetails(movie));
                detailsButton.setCursor(Cursor.HAND);

                card.getChildren().addAll(nameLabel, detailsBox, detailsButton);
                setGraphic(card);
            }
        }

        private void addDetailRow(VBox container, String label, String value) {
            HBox row = new HBox(10);
            Label detailLabel = new Label(label);
            detailLabel.setStyle("-fx-text-fill: #b3d9ff; -fx-font-weight: bold;");  // Brighter blue

            Label detailValue = new Label(value);
            detailValue.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");  // Bolder text

            row.getChildren().addAll(detailLabel, detailValue);
            container.getChildren().add(row);
        }

        private String formatDuration(int minutes) {
            return String.format("%dh %02dmin", minutes / 60, minutes % 60);
        }
    }
}



