package com.jpm.show.controller;

import com.jpm.show.config.AdminUsersConfig;
import com.jpm.show.entity.Booking;
import com.jpm.show.entity.Show;
import com.jpm.show.entity.Ticket;
import com.jpm.show.service.AdminService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component // Not really a controller, but following this kind of structure allows refactoring to REST api quickly later
@AllArgsConstructor
public class AdminController {
    private AdminService adminService;
    private AdminUsersConfig adminUsersConfig;

    public void setup(String user, String showNumber, String numRows, String numSeatsPerRow, String cancellationWindowMinutes) {
        if (!adminUsersConfig.getUsers().contains(user)) {
            System.out.println("Unauthorized");
            log.error("Unauthorized");
            return;
        }

        try {
            System.out.printf("Processing \"setup %s %s %s %s\"\n", showNumber, numRows, numSeatsPerRow, cancellationWindowMinutes);
            log.info(String.format("Processing \"setup %s %s %s %s\"", showNumber, numRows, numSeatsPerRow, cancellationWindowMinutes));

            adminService.addShow(showNumber, Integer.parseInt(numRows),
                    Integer.parseInt(numSeatsPerRow), Integer.parseInt(cancellationWindowMinutes));

            System.out.println("Show is successfully added");
            log.info("Show is successfully added");
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            log.error("Error: " + e.getMessage());
        }
    }

    public void view(String user, String showNumber) {
        if (!adminUsersConfig.getUsers().contains(user)) {
            System.out.println("Unauthorized");
            log.error("Unauthorized");
            return;
        }

        try {
            System.out.printf("Processing \"view %s\"\n", showNumber);
            log.info(String.format("Processing \"view %s\"", showNumber));

            StringBuilder sb = new StringBuilder();
            Show show = adminService.viewShow(showNumber);
            sb.append("show#, ticket#, buyer phone#, seat#\n");
            for (Map.Entry<String, Booking> booking : show.getBookings().entrySet()) {
                for (Map.Entry<String, Ticket> ticket : booking.getValue().getTickets().entrySet()) {
                    sb.append(booking.getValue().getShowId())
                            .append(", ")
                            .append(ticket.getValue().getId())
                            .append(", ")
                            .append(booking.getValue().getBuyerPhoneNo())
                            .append(", ")
                            .append(ticket.getValue().getSeatNo())
                            .append("\n");
                }
            }

            System.out.println(sb.toString());
            log.info(sb.toString());
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            log.error("Error: " + e.getMessage());
        }
    }
}
