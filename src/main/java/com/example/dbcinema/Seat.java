package org.example.cinemadb;
public class Seat {
    private final int seatId;
    private final int hallId;
    private final String seatNo;
    private boolean isReserved;

    public Seat(int seatId, int hallId, String seatNo, boolean isReserved) {
        this.seatId = seatId;
        this.hallId = hallId;
        this.seatNo = seatNo;
        this.isReserved = isReserved;
    }

    // Getters
    public int getSeatId() { return seatId; }
    public int getHallId() { return hallId; }
    public String getSeatNo() { return seatNo; }
    public boolean isReserved() { return isReserved; }
}


