package com.jpm.show.data;

import com.google.gson.Gson;
import com.jpm.show.entity.Booking;
import com.jpm.show.entity.SeatStatus;
import com.jpm.show.entity.Show;
import com.jpm.show.entity.Ticket;
import com.jpm.show.exception.InvalidShowException;
import com.jpm.show.exception.InvalidTicketException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class InMemoryDataStorage {
    // Key = show id
    private final Map<String, Show> showMap = new ConcurrentHashMap<>();
    // Key = ticket id, value = show id
    private final Map<String, String> ticketMap = new ConcurrentHashMap<>();
    private final Gson gson = new Gson();

    public void createShow(Show show) throws Exception {
        if (showMap.containsKey(show.getId())) {
            throw new InvalidShowException("Show ID: " + show.getId() + " already exists");
        }
        showMap.put(show.getId(), show);
    }

    public Show getShow(String showId) throws Exception {
        if (!showMap.containsKey(showId)) {
            throw new InvalidShowException("Show ID: " + showId + " is not found");
        }
        return deepCopyShow(showMap.get(showId));
    }

    public SeatStatus checkSeatStatus(String showId, String seatNo) throws Exception {
        if (!showMap.containsKey(showId)) {
            throw new InvalidShowException("Show ID: " + showId + " is not found");
        }
        Map<String, SeatStatus> showSeatStatus = showMap.get(showId).getSeatStatusMap();
        if (!showSeatStatus.containsKey(seatNo)) {
            throw new InvalidShowException("Invalid seat number: " + seatNo);
        }
        return showSeatStatus.get(seatNo);
    }

    public void updateSeatStatus(String showId, String seatNo, SeatStatus seatStatus) throws Exception {
        if (!showMap.containsKey(showId)) {
            throw new InvalidShowException("Show ID: " + showId + " is not found");
        }
        Map<String, SeatStatus> showSeatStatus = showMap.get(showId).getSeatStatusMap();
        if (!showSeatStatus.containsKey(seatNo)) {
            throw new InvalidShowException("Invalid seat number: " + seatNo);
        }
        showSeatStatus.put(seatNo, seatStatus);
    }

    public void addNewBookingToShow(String showId, String phoneNumber, Booking booking) throws Exception {
        if (!showMap.containsKey(showId)) {
            throw new InvalidShowException("Show ID: " + showId + " is not found");
        }
        showMap.get(showId).getBookings().put(phoneNumber, booking);
        for (Map.Entry<String, Ticket> ticket : booking.getTickets().entrySet()) {
            ticketMap.put(ticket.getKey(), showId);
        }
    }

    public Show getShowFromTicketId(String ticketId) throws Exception {
        if (!ticketMap.containsKey(ticketId)) {
            throw new InvalidTicketException("Ticket ID: " + ticketId + " is not found");
        }
        String showId = ticketMap.get(ticketId);
        if (!showMap.containsKey(showId)) {
            throw new InvalidShowException("Show ID: " + showId + " from Ticket ID: " + ticketId + " is not found");
        }
        return deepCopyShow(showMap.get(showId));
    }

    public void removeTicketFromShow(String showId, String phoneNumber, String ticketId) throws Exception {
        if (!showMap.containsKey(showId)) {
            throw new InvalidShowException("Show ID: " + showId + " is not found");
        }
        if (!ticketMap.containsKey(ticketId)) {
            throw new InvalidTicketException("Ticket ID: " + ticketId + " is not found");
        }
        showMap.get(showId).getBookings().get(phoneNumber).getTickets().remove(ticketId);
        ticketMap.remove(ticketId);
    }

    private Show deepCopyShow(Show show) {
        return gson.fromJson(gson.toJson(show), Show.class);
    }
}
