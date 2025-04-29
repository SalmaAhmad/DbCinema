package org.example.cinemadb;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.HBox;

public class MovieCell extends ListCell<Movie> {
    @FXML private HBox cellRoot;
    @FXML private Label titleLabel;
    @FXML private Label genreLabel;
    @FXML private Label durationLabel;
    @FXML private Label ratingLabel;
    @FXML private Label descriptionLabel;

    public MovieCell() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("movie-cell.fxml"));
            loader.setController(this);
            loader.load();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void updateItem(Movie movie, boolean empty) {
        super.updateItem(movie, empty);

        if (empty || movie == null) {
            setGraphic(null);
        } else {
            titleLabel.setText(movie.getName());
            genreLabel.setText(movie.getGenre());
            durationLabel.setText(movie.getLanguage());

            setGraphic(cellRoot);
        }
    }
}