package com.jpm.show.service;

import com.jpm.show.data.InMemoryDataStorage;
import com.jpm.show.entity.Booking;
import com.jpm.show.entity.SeatStatus;
import com.jpm.show.entity.Show;
import com.jpm.show.entity.Ticket;
import com.jpm.show.exception.InvalidBookingException;
import com.jpm.show.exception.InvalidCancellationException;
import com.jpm.show.exception.InvalidTicketException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;


// Services are usable for different interfaces (i.e. both command line and REST api can use the same service)
@Slf4j
@Service
@AllArgsConstructor
public class BuyerService {
    private InMemoryDataStorage inMemoryDataStorage;

    public List<String> getShowAvailableSeats(String showNumber) throws Exception {
        List<String> availableSeats = new ArrayList<>();
        Show show = inMemoryDataStorage.getShow(showNumber);
        Map<String, SeatStatus> seatStatusMap = show.getSeatStatusMap();
        for (Map.Entry<String, SeatStatus> seat : seatStatusMap.entrySet()) {
            if (seat.getValue() == SeatStatus.AVAILABLE) {
                availableSeats.add(seat.getKey());
            }
        }
        return availableSeats;
    }

    // Need lock or synchronised block to be completely thread safe
    public synchronized List<Ticket> bookShow(String showNumber, String phoneNumber, List<String> seatNumbers) throws Exception {
        Show show = inMemoryDataStorage.getShow(showNumber);
        String showId = show.getId();
        if (show.getBookings().containsKey(phoneNumber)) {
            throw new InvalidBookingException("Only one phone number allowed per booking per show");
        }

        String bookingId = UUID.randomUUID().toString();
        Booking booking = new Booking(bookingId, showNumber, phoneNumber, Instant.now().toEpochMilli());
        List<String> reservedSeats = new ArrayList<>();
        for (String seatNo : seatNumbers) {
            // Check in-memory data to see if the seat is still AVAILABLE in case of a dirty read
            SeatStatus seatStatus = inMemoryDataStorage.checkSeatStatus(showId, seatNo);
            if (seatStatus != SeatStatus.AVAILABLE) {
                // Revert previously RESERVED seats back to AVAILABLE in in-memory data
                for (String reservedSeatNo : reservedSeats) {
                    inMemoryDataStorage.updateSeatStatus(showId, reservedSeatNo, SeatStatus.AVAILABLE);
                }
                throw new InvalidTicketException("Seat No is not available: " + seatNo);
            }
            // Mark the current seat as RESERVED in in-memory data to avoid a separate thread from booking it
            inMemoryDataStorage.updateSeatStatus(showId, seatNo, SeatStatus.RESERVED);
            reservedSeats.add(seatNo);

            String ticketId = UUID.randomUUID().toString();
            Ticket ticket = new Ticket(ticketId, seatNo, showId, bookingId);
            booking.getTickets().put(ticketId, ticket);
        }

        // All seats successfully booked, flush to in-memory data
        // Mark the seat as OCCUPIED in in-memory data
        for (String seatNo : reservedSeats) {
            inMemoryDataStorage.updateSeatStatus(showId, seatNo, SeatStatus.OCCUPIED);
        }
        inMemoryDataStorage.addNewBookingToShow(show.getId(), phoneNumber, booking);
        return new ArrayList<>(booking.getTickets().values());
    }

    // Need lock or synchronise block to be completely thread safe
    public synchronized void cancelTicket(String ticketNumber, String phoneNumber) throws Exception {
        Show show = inMemoryDataStorage.getShowFromTicketId(ticketNumber);
        if (!show.getBookings().containsKey(phoneNumber)) {
            throw new InvalidTicketException("Wrong Phone number provided for ticket: " + ticketNumber);
        }

        Optional<Booking> currentBookingOptional = Optional.ofNullable(show.getBookings().get(phoneNumber));
        if (currentBookingOptional.isEmpty()) {
            throw new InvalidBookingException(String.format("Booking with phone number %s is not found for show %s", phoneNumber, show.getId()));
        }
        Booking currentBooking = currentBookingOptional.get();
        if (Instant.now().toEpochMilli() - currentBooking.getBookingTimestamp() > show.getCancellationWindowMils()) {
            throw new InvalidCancellationException("Exceeded allowed time window to cancel seats");
        }

        // flush to in-memory data
        String seatNo = currentBooking.getTickets().get(ticketNumber).getSeatNo();
        inMemoryDataStorage.updateSeatStatus(show.getId(), seatNo, SeatStatus.AVAILABLE);
        inMemoryDataStorage.removeTicketFromShow(show.getId(), phoneNumber, ticketNumber);
    }
}
