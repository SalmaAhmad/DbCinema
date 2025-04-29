package org.example.cinemadb;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SeatsController {
    @FXML private FlowPane seatFlowPane;
    @FXML private Label movieTitleLabel;
    @FXML private Label showtimeLabel;
    @FXML private Label totalPriceLabel;
    @FXML private Button confirmButton;

    private ShowTime showTime;
    private List<Seat> selectedSeats = new ArrayList<>();

    public void setShowTime(ShowTime showTime) {
        this.showTime = showTime;
        updateShowInfo();
        loadAvailableSeats();
    }

    private void updateShowInfo() {
        movieTitleLabel.setText("Movie: " + showTime.getMovieId());
        showtimeLabel.setText(showTime.toString());
        updateTotalPrice();
    }

    private void loadAvailableSeats() {
        seatFlowPane.getChildren().clear();
        selectedSeats.clear();

        try (Connection conn = SQLServerConnect.getConnection()) {
            // Get all seats for this hall
            String query = "SELECT Seat_ID, SeatNo FROM Seats WHERE H_ID = ?";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setInt(1, showTime.getHallId());
                ResultSet rs = stmt.executeQuery();

                while (rs.next()) {
                    Button seatButton = createSeatButton(
                            rs.getInt("Seat_ID"),
                            rs.getString("SeatNo")
                    );
                    seatFlowPane.getChildren().add(seatButton);
                }
            }
        } catch (SQLException e) {
            showAlert("Error", "Failed to load seats: " + e.getMessage());
        }
    }

    private Button createSeatButton(int seatId, String seatNo) {
        Button button = new Button(seatNo);
        button.setPrefSize(40, 40);
        button.setUserData(new Seat(seatId, showTime.getHallId(), seatNo, false));

        // Check initial availability
        if (!isSeatAvailable(seatId)) {
            button.setStyle("-fx-background-color: #ff0000; -fx-text-fill: white;"); // Red for reserved
            button.setDisable(true);
        } else {
            button.setStyle("-fx-background-color: #00ff00; -fx-text-fill: black;"); // Green for available
            button.setOnAction(e -> {
                Seat seat = (Seat) button.getUserData();
                toggleSeatSelection(seat, button);
            });
        }
        return button;
    }
    private void toggleSeatSelection(Seat seat, Button button) {
        if (selectedSeats.contains(seat)) {
            selectedSeats.remove(seat);
            button.setStyle("-fx-background-color: #00ff00; -fx-text-fill: black;"); // Green for available
        } else {
            // Check if seat is available for this showtime
            if (isSeatAvailable(seat.getSeatId())) {
                selectedSeats.add(seat);
                button.setStyle("-fx-background-color: #0000ff; -fx-text-fill: white;"); // Blue for selected
            } else {
                showAlert("Seat Taken", "This seat is already reserved for the selected showtime");
            }
        }
        updateTotalPrice();
    }

    private boolean isSeatAvailable(int seatId) {
        String query = "SELECT COUNT(*) FROM SeatReservation " +
                "WHERE Seat_ID = ? AND Showtime_ID = ?";

        try (Connection conn = SQLServerConnect.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, seatId);
            stmt.setInt(2, showTime.getShowtimeId());
            ResultSet rs = stmt.executeQuery();
            return rs.next() && rs.getInt(1) == 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    private void updateTotalPrice() {
        double total = selectedSeats.size() * showTime.getPrice();
        totalPriceLabel.setText(String.format("Total: $%.2f", total));
        confirmButton.setDisable(selectedSeats.isEmpty());
    }

    @FXML
    private void handleConfirmBooking() {
        try {
            // 1. First verify the FXML file exists
            System.out.println("Attempting to load CustomerForm.fxml...");

            // 2. Get the resource URL properly
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/org/example/cinemadb/CustomerForm.fxml"));

            // Debug: Print the actual path being used
            System.out.println("Loading from: " + loader.getLocation());

            // 3. Load the FXML
            Parent root = loader.load();
            System.out.println("FXML loaded successfully!");

            // 4. Get controller and pass data
            CustomerFormController controller = loader.getController();
            controller.setBookingData(
                    showTime,
                    selectedSeats,
                    selectedSeats.size() * showTime.getPrice()
            );

            // 5. Create and show new window
            Stage stage = new Stage();
            stage.setTitle("Complete Your Booking");
            stage.setScene(new Scene(root));
            stage.show();

            // 6. Close current window
            ((Stage) confirmButton.getScene().getWindow()).close();

        } catch (IOException e) {
            System.err.println("ERROR loading CustomerForm.fxml:");
            e.printStackTrace();

            showAlert("Loading Error",
                    "Failed to load booking form.\n" +
                            "Error: " + e.getMessage() + "\n" +
                            "Please ensure CustomerForm.fxml exists in the correct location.");
        }
    }
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

}
