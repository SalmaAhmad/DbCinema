package org.example.cinemadb;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class CustomerFormController {
    private Parent previousSceneRoot;
    // FXML injected fields
    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private TextField emailField;
    @FXML private TextField phoneField;
    @FXML private ChoiceBox<String> paymentMethodChoice;
    @FXML private Label totalPriceLabel;
    @FXML private Button completeBookingButton;
    @FXML
    private ChoiceBox<String> seatTypeChoice;
    private List<Seat> selectedSeats;
    private ShowTime showTime;
    private double totalPrice;
    public static Scene previousScene;
    @FXML

    public void initialize() {
        // Debug initialization
        System.out.println("CustomerFormController initialized");

        // Verify FXML injection
        if (completeBookingButton == null) {
            throw new IllegalStateException("completeBookingButton was not injected - check FXML file");
        }
        if (seatTypeChoice == null || paymentMethodChoice == null) {
            throw new IllegalStateException("ChoiceBoxes were not injected - check FXML file");
        }

        // Initialize choice boxes
        seatTypeChoice.getItems().addAll("Standard", "Premium", "VIP");
        paymentMethodChoice.getItems().addAll("Credit Card", "Debit Card", "PayPal", "Cash");

        // Set default selections
        seatTypeChoice.getSelectionModel().selectFirst();
        paymentMethodChoice.getSelectionModel().selectFirst();

        // Set up button action (single handler)
        completeBookingButton.setOnAction(event -> {
            System.out.println("Complete Booking button clicked");
            handleCompleteBooking();
        });

        seatTypeChoice.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            updateTotalPrice();
        });
        // Apply styling
        applyControlStyles();
    }



    private void applyControlStyles() {
        String choiceBoxStyle = "-fx-background-color: #4d4d4d; " +
                "-fx-mark-color: white; " +
                "-fx-text-fill: white;";

        seatTypeChoice.setStyle(choiceBoxStyle);
        paymentMethodChoice.setStyle(choiceBoxStyle);

        completeBookingButton.setStyle("-fx-background-color: #e50914; " +
                "-fx-text-fill: white; " +
                "-fx-font-weight: bold;");
    }
    @FXML

    public void setBookingData(ShowTime showTime, List<Seat> seats, double totalPrice) {
        this.showTime = showTime;
        this.selectedSeats = seats;
        this.totalPrice = totalPrice;
        totalPriceLabel.setText(String.format("Total: $%.2f", totalPrice));
    }

    private void markSeatsReserved(Connection conn, List<Integer> ticketIds) throws SQLException {
        String query = "INSERT INTO SeatReservation (Seat_ID, Showtime_ID, Ticket_ID) " +
                "VALUES (?, ?, ?)";

        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            for (int i = 0; i < selectedSeats.size(); i++) {
                Seat seat = selectedSeats.get(i);
                stmt.setInt(1, seat.getSeatId());
                stmt.setInt(2, showTime.getShowtimeId());
                stmt.setInt(3, ticketIds.get(i));
                stmt.addBatch();
            }
            stmt.executeBatch();
        }
    }

    @FXML
    private void handleCompleteBooking() {
        try (Connection conn = SQLServerConnect.getConnection()) {
            conn.setAutoCommit(false); // Start transaction

            try {
                // 1. Create customer
                int customerId = createCustomer(conn);

                // 2. Create payment
                int paymentId = createPayment(conn, customerId);

                // 3. Create tickets and get the generated ticket IDs
                List<Integer> ticketIds = createTickets(conn, customerId, paymentId);

                // 4. Mark seats as reserved for this specific showtime
                markSeatsReserved(conn, ticketIds);

                conn.commit(); // Commit transaction

                // Show ticket view after successful booking
                showTicketView();

                // Close the current window
                ((Stage) completeBookingButton.getScene().getWindow()).close();

            } catch (SQLException e) {
                conn.rollback();
                showAlert("Database Error", "Failed to complete booking: " + e.getMessage());
                e.printStackTrace();
            }
        } catch (SQLException e) {
            showAlert("Connection Error", "Could not connect to database: " + e.getMessage());
            e.printStackTrace();
        }
    }
    private boolean validateInputs() {
        if (firstNameField.getText().trim().isEmpty()) {
            showAlert("Validation Error", "First name is required");
            return false;
        }

        if (lastNameField.getText().trim().isEmpty()) {
            showAlert("Validation Error", "Last name is required");
            return false;
        }

        return true;
    }

    private int createCustomer(Connection conn) throws SQLException {
        String query = "INSERT INTO Customer (First_Name, Last_Name, Email, PhoneNo) VALUES (?, ?, ?, ?)";

        try (PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            // Validate and format phone number
            String PhoneNo = phoneField.getText().replaceAll("[^0-9]", "");
            if (PhoneNo.length() != 11) {
                throw new SQLException("Phone number must be 11 digits");
            }

            // Set parameters (note: no C_ID parameter as it's auto-incremented)
            stmt.setString(1, firstNameField.getText().trim());
            stmt.setString(2, lastNameField.getText().trim());
            stmt.setString(3, emailField.getText().trim());
            stmt.setString(4, PhoneNo);

            // Execute update
            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating customer failed, no rows affected");
            }

            // Retrieve the auto-generated C_ID
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1); // Returns the auto-generated C_ID
                }
                else {
                    throw new SQLException("Creating customer failed, no ID obtained");
                }
            }
        }
    }
    private int createPayment(Connection conn, int customerId) throws SQLException {
        // Corrected query - removed Customer_ID since it's not in your schema
        // and matches your actual column names
        String query = "INSERT INTO Payment (Method, Amount, Paymentstatus) VALUES (?, ?, ?)";

        try (PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            // Set parameters according to your schema
            stmt.setString(1, paymentMethodChoice.getValue());  // Method
            stmt.setDouble(2, totalPrice);                     // Amount
            stmt.setString(3, "Completed");                    // Paymentstatus

            // Execute update
            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating payment failed, no rows affected");
            }

            // Retrieve the auto-generated P_ID
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);  // Returns the auto-generated P_ID
                }
                throw new SQLException("Creating payment failed, no ID obtained");
            }
        }
    }

    private List<Integer> createTickets(Connection conn, int customerId, int paymentId) throws SQLException {
        String query = "INSERT INTO Ticket (Price, Showtime_ID, SeatNumber, Type, Payment_ID, Customer_ID, Movie_ID) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

        List<Integer> ticketIds = new ArrayList<>();

        try (PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            for (Seat seat : selectedSeats) {
                stmt.setDouble(1, calculatePrice(String.valueOf(seatTypeChoice)));
                stmt.setInt(2, showTime.getShowtimeId());
                stmt.setString(3, seat.getSeatNo());
                stmt.setString(4, seatTypeChoice.getValue());
                stmt.setInt(5, paymentId);
                stmt.setInt(6, customerId);
                stmt.setInt(7, showTime.getMovieId());
                stmt.executeUpdate();

                // Get the generated ticket ID
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        ticketIds.add(rs.getInt(1));
                    }
                }
            }
        }
        return ticketIds;
    }
    private double calculatePrice(String seatType) {
        double basePrice = showTime.getPrice();
        return switch (seatType) {
            case "Premium" -> basePrice * 1.5;
            case "VIP" -> basePrice * 2.0;
            default -> basePrice; // Standard
        };
    }

    private void showSuccessAndClose() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Booking Confirmed");
        alert.setHeaderText("Your booking was successful!");
        alert.setContentText("Would you like to view your ticket now?");

        ButtonType yesButton = new ButtonType("Yes", ButtonBar.ButtonData.YES);
        ButtonType noButton = new ButtonType("No", ButtonBar.ButtonData.NO);
        alert.getButtonTypes().setAll(yesButton, noButton);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == yesButton) {
            showTicketView();
        }

        // Close the current window regardless of choice
        ((Stage) completeBookingButton.getScene().getWindow()).close();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    // Add this to your handleCompleteBooking method after successful booking:
    private void showTicketView() {
        try {
            // First get the actual movie name from database
            String movieName = getMovieName(showTime.getMovieId());

            // Create booking details with both movie ID and name
            BookingDetails details = new BookingDetails(
                    showTime.getMovieId(),
                    movieName, // Now using actual movie name
                    showTime.toString(),
                    selectedSeats.stream().map(Seat::getSeatNo).collect(Collectors.toList()),
                    firstNameField.getText() + " " + lastNameField.getText(),
                    emailField.getText(),
                    phoneField.getText(),
                    paymentMethodChoice.getValue(),
                    "Paid",
                    totalPrice
            );

            // Load and show ticket view (rest of the method remains the same)
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/cinemadb/TicketView.fxml"));
            Parent root = loader.load();

            TicketViewController controller = loader.getController();
            controller.setBookingDetails(details);

            Stage stage = new Stage();
            stage.setTitle("Your Ticket");
            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException | SQLException e) {
            showAlert("Error", "Failed to show ticket: " + e.getMessage());
        }
    }

    // Add this helper method to get movie name from database
    private String getMovieName(int movieId) throws SQLException {
        String query = "SELECT Name FROM Movie WHERE Movie_ID = ?";
        try (Connection conn = SQLServerConnect.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, movieId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("Name");
            }
            throw new SQLException("Movie not found");
        }
    }
    private void updateTotalPrice() {
        double basePrice = showTime.getPrice();
        double multiplier = switch (seatTypeChoice.getValue()) {
            case "Premium" -> 1.5;
            case "VIP" -> 2.0;
            default -> 1.0; // Standard
        };
        double total = selectedSeats.size() * basePrice * multiplier;
        totalPriceLabel.setText(String.format("$%.2f", total));
    }

    public void setPreviousScene(Parent previousScene) {
        this.previousSceneRoot = previousScene;
    }

    // FXML handler
    @FXML
    private void handleBackToPrevious(ActionEvent event) {
        try {
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            if (previousSceneRoot != null) {
                stage.setScene(new Scene(previousSceneRoot));
            } else {
                // Fallback to home
                Parent root = FXMLLoader.load(getClass().getResource("/org/example/cinemadb/MovieList.fxml"));
                stage.setScene(new Scene(root));
            }
        } catch (IOException e) {
            showAlert("Navigation Error", "Failed to return to previous screen");
        }
    }




    public void handleBackButtonAction1(javafx.event.ActionEvent event) {     // Get the current window (stage)
        Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();

        // Save the current scene to SceneManager (to go back later)
        CustomerFormController.previousScene = currentStage.getScene();

        // Close the current window (this window)
        currentStage.close();


    }
}

