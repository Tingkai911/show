package com.jpm.show.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Ticket {
    private String id;
    private String seatNo;
    private String showId;
    private String bookingId;
}
