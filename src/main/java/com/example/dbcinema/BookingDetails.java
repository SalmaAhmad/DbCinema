package org.example.cinemadb;



import java.util.List;

public class BookingDetails {
    private final int movieId;
    private String movieName;
    private String showtime;
    private List<String> seats;
    private String customerName;
    private String customerEmail;
    private String customerPhone;
    private String paymentMethod;
    private String paymentStatus;
    private double totalPrice;

    // Constructor
    public BookingDetails(int movieId,String movieName, String showtime, List<String> seats,
                          String customerName, String customerEmail, String customerPhone,
                          String paymentMethod, String paymentStatus, double totalPrice) {
        this.movieName=movieName;
        this.movieId= movieId;
        this.showtime = showtime;
        this.seats = seats;
        this.customerName = customerName;
        this.customerEmail = customerEmail;
        this.customerPhone = customerPhone;
        this.paymentMethod = paymentMethod;
        this.paymentStatus = paymentStatus;
        this.totalPrice = totalPrice;
    }

    // Getters
    public int getMovieId() { return movieId; }
    public String getMovieName() { return movieName; }
    public String getShowtime() { return showtime; }
    public List<String> getSeats() { return seats; }
    public String getCustomerName() { return customerName; }
    public String getCustomerEmail() { return customerEmail; }
    public String getCustomerPhone() { return customerPhone; }
    public String getPaymentMethod() { return paymentMethod; }
    public String getPaymentStatus() { return paymentStatus; }
    public double getTotalPrice() { return totalPrice; }
}
