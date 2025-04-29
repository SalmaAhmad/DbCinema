package org.example.cinemadb;



import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.io.IOException;

public class TicketViewController {
    @FXML private Label movieLabel;
    @FXML private Label showtimeLabel;
    @FXML private Label seatsLabel;
    @FXML private Label customerLabel;
    @FXML private Label paymentLabel;
    @FXML private Label totalLabel;

    private BookingDetails bookingDetails;

    public void setBookingDetails(BookingDetails details) {
        this.bookingDetails = details;
        updateUI();
    }

    private void updateUI() {
        // Display both ID and name
        movieLabel.setText(bookingDetails.getMovieName() + " (ID: " + bookingDetails.getMovieId() + ")");

        // Rest of your display logic remains the same
        showtimeLabel.setText(bookingDetails.getShowtime());
        seatsLabel.setText(String.join(", ", bookingDetails.getSeats()));
        customerLabel.setText(bookingDetails.getCustomerName() + "\n" + bookingDetails.getCustomerEmail() + "\n" + bookingDetails.getCustomerPhone());
        paymentLabel.setText(bookingDetails.getPaymentMethod() + " (" + bookingDetails.getPaymentStatus() + ")");
        totalLabel.setText(String.format("$%.2f", bookingDetails.getTotalPrice()));
    }
    @FXML
    private void handlePrint() {
        // Implement actual printing functionality here
        System.out.println("Printing ticket...");
        // For now just show confirmation
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Print Ticket");
        alert.setHeaderText(null);
        alert.setContentText("Ticket sent to printer!");
        alert.showAndWait();
    }

    @FXML
    private void handleBackToHome() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("Movie-List.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) totalLabel.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.centerOnScreen();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}