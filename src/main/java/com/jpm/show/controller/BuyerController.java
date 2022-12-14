package com.jpm.show.controller;

import com.jpm.show.entity.Ticket;
import com.jpm.show.service.BuyerService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Slf4j
@Component // Not really a controller, but following this kind of structure allows refactoring to REST api quickly later
@AllArgsConstructor
public class BuyerController {
    private BuyerService buyerService;

    public void availability(String showNumber) {
        try {
            System.out.printf("Processing \"availability %s\"\n", showNumber);

            List<String> availableSeats = buyerService.getShowAvailableSeats(showNumber);
            StringBuilder sb = new StringBuilder();
            sb.append("Available Seats: ");
            for(String seatNo : availableSeats) {
                sb.append(seatNo).append(", ");
            }
            System.out.println(sb.toString());
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    public void book(String showNumber, String phoneNumber, String seatNumberCommand) {
        try {
            System.out.printf("Processing \"book %s %s %s\"\n", showNumber, phoneNumber, seatNumberCommand);

            List<String> seatNumbers = Arrays.asList(seatNumberCommand.split(","));
            List<Ticket> tickets = buyerService.bookShow(showNumber, phoneNumber, seatNumbers);
            StringBuilder sb = new StringBuilder();
            sb.append("Successfully booked the following shows\n");
            sb.append("show#, ticket#, buyer phone#, seat#\n");
            for(Ticket ticket : tickets) {
                sb.append(ticket.getShowId())
                        .append(", ")
                        .append(ticket.getId())
                        .append(", ")
                        .append(phoneNumber)
                        .append(", ")
                        .append(ticket.getSeatNo())
                        .append("\n");
            }
            System.out.println(sb.toString());
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    public void cancel(String ticketNumber, String phoneNumber) {
        try {
            System.out.printf("Processing \"cancel %s %s\"\n", ticketNumber, phoneNumber);
            buyerService.cancelTicket(ticketNumber, phoneNumber);
            System.out.println("Ticket No is canceled: " + ticketNumber);
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}
