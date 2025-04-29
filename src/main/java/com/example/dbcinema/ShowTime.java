package org.example.cinemadb;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class ShowTime {
    private final int showtimeId;
    private final int movieId;
    private final int hallId;
    private final String hallLocation;
    private final LocalTime endtime;
    private LocalTime startTime;
    private final LocalDate showDate;


    private final double price = 12.50; // Default price

    // Constructor
    public ShowTime(int showtimeId, int movieId, int hallId, LocalTime startTime, LocalTime endtime,
                    LocalDate showDate,String hallLocation) {
        this.showtimeId = showtimeId;
        this.movieId = movieId;
        this.hallId = hallId;
        this.startTime = startTime;
        this.hallLocation=hallLocation;
        this.showDate = showDate;
        this.endtime=endtime;
    }

    // If you get a LocalDateTime from the database, you can use this setter:
    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime.toLocalTime(); // Convert to LocalTime
    }

    // Getters
    public int getShowtimeId() { return showtimeId; }
    public int getMovieId() { return movieId; }
    public int getHallId() { return hallId; }
    public LocalTime getStartTime() { return startTime; }
    public LocalDate getShowDate() { return showDate; }
    public String getLocation() { return hallLocation; }
    public double getPrice() { return price; }
    public LocalTime getEndTime(){return endtime;}

    public String toString() {
        return "Date: " + getShowDate() + "\n" +
                "Start Time: " + getStartTime() + "\n" +"End Time "+getEndTime()+ "\n"+
                "Hall Number: " + getHallId()+"\n"+"Location: "+getLocation() + "\n";
    }

}
