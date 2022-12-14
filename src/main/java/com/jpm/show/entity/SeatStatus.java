package com.jpm.show.entity;

public enum SeatStatus {
    // Seat is still available
    AVAILABLE,
    // Seat is being reserved by someone but booking is not complete
    RESERVED,
    // Seat is already booked by someone
    OCCUPIED
}
