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
        System.out.println("CustomerFormController initialized");

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
        updateTotalPrice(); // Update the price label immediately
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

                // 3. Reserve seats and create tickets using stored procedure
                reserveSeatsWithTickets(conn, customerId, paymentId);

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

    private void reserveSeatsWithTickets(Connection conn, int customerId, int paymentId) throws SQLException {
        // The SQL Server stored procedure to call
        String procedureCall = "{call CreateTicketAndReserveSeat(?, ?, ?, ?, ?, ?, ?, ?)}";

        try (CallableStatement stmt = conn.prepareCall(procedureCall)) {
            for (Seat seat : selectedSeats) {
                stmt.setInt(1, customerId);                            // @CustomerId
                stmt.setInt(2, paymentId);                             // @PaymentId
                stmt.setInt(3, showTime.getShowtimeId());              // @ShowtimeId
                stmt.setInt(4, showTime.getMovieId());                 // @MovieId
                stmt.setInt(5, seat.getSeatId());                      // @SeatId
                stmt.setString(6, seat.getSeatNo());                   // @SeatNumber
                stmt.setString(7, seatTypeChoice.getValue());// @SeatType
                double calculatedPrice = calculatePrice(conn, seatTypeChoice.getValue());
                stmt.setDouble(8, calculatedPrice); // @Price
                stmt.execute();  // Call the procedure
            }
        }
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
        String query = "INSERT INTO Payment (Method, Amount, Paymentstatus) VALUES (?, ?, ?)";

        try (PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            // Set parameters according to your schema
            stmt.setString(1, paymentMethodChoice.getValue());  // Method
            stmt.setDouble(2, totalPrice);                     // Amount
            stmt.setString(3, "Completed");                 // Paymentstatus

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

    private double calculatePrice(Connection conn, String ticketType) throws SQLException {
        String query = "SELECT dbo.CalculatePayment(?, ?) AS CalculatedPrice";

        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setDouble(1, showTime.getPrice());
            stmt.setString(2, ticketType);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("CalculatedPrice");
                } else {
                    throw new SQLException("Failed to get calculated price.");
                }
            }
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("TicketView.fxml"));
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

    private String getMovieName(int movieId) throws SQLException {
        String query = "SELECT Name FROM Movie WHERE Movie_ID = ?";

        try (Connection conn = SQLServerConnect.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, movieId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("Name");
                }
                throw new SQLException("Movie not found");
            }
        }
    }


    private void updateTotalPrice() {
        double basePrice = showTime.getPrice();
        double multiplier = switch (seatTypeChoice.getValue()) {
            case "Premium" -> 1.5;
            case "VIP" -> 2.0;
            default -> 1.0;
        };
        this.totalPrice = selectedSeats.size() * basePrice * multiplier;
        totalPriceLabel.setText(String.format("Total: $%.2f", totalPrice));
    }

    public void handleBackButtonAction1(javafx.event.ActionEvent event) {     // Get the current window (stage)
        Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();

        // Save the current scene to SceneManager (to go back later)
        CustomerFormController.previousScene = currentStage.getScene();

        // Close the current window (this window)
        currentStage.close();

    }
}
