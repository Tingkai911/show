package com.jpm.show.entity;

import lombok.Data;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Data
public class Booking {
    private String id;
    private String showId;
    private String buyerPhoneNo;
    private long bookingTimestamp;
    // Key = ticket id
    private Map<String, Ticket> tickets;

    public Booking(String id, String showId, String buyerPhoneNo, long bookingTimestamp) {
        this.id = id;
        this.showId = showId;
        this.buyerPhoneNo = buyerPhoneNo;
        this.bookingTimestamp = bookingTimestamp;
        this.tickets = new ConcurrentHashMap<>();
    }
}
