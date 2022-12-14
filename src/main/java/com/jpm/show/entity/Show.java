package com.jpm.show.entity;

import lombok.Data;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Data
public class Show {
    private String id;
    private int numRows;
    private int numSeatsPerRow;
    private long cancellationWindowMils;
    // Key = buyer phone number
    private Map<String, Booking> bookings;
    // Key = seat number, value = AVAILABLE, RESERVED or OCCUPIED (SeatStatus enum)
    private Map<String, SeatStatus> seatStatusMap;

    public Show(String id, int numRows, int numSeatsPerRow, long cancellationWindowMils) {
        this.id = id;
        this.numRows = numRows;
        this.numSeatsPerRow = numSeatsPerRow;
        this.cancellationWindowMils = cancellationWindowMils;
        this.bookings = new ConcurrentHashMap<>();

        this.seatStatusMap = new ConcurrentHashMap<>();
        for(int i = 0; i < numRows; i++) {
            for(int j = 0; j < numSeatsPerRow; j++) {
                StringBuilder sb = new StringBuilder();
                sb.append(Character.toString((char) 'A' + i));
                sb.append(j + 1);
                seatStatusMap.put(sb.toString(), SeatStatus.AVAILABLE);
            }
        }
    }
}
