package com.jpm.show.controller;

import com.jpm.show.data.InMemoryDataStorage;
import com.jpm.show.entity.Booking;
import com.jpm.show.entity.SeatStatus;
import com.jpm.show.entity.Show;
import com.jpm.show.entity.Ticket;
import com.jpm.show.exception.InvalidBookingException;
import com.jpm.show.exception.InvalidCancellationException;
import com.jpm.show.exception.InvalidTicketException;
import com.jpm.show.service.BuyerService;
import org.junit.jupiter.api.*;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.ActiveProfiles;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@ActiveProfiles("test")
@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class BuyerControllerTest {
    @Autowired
    private BuyerController buyerController;
    @SpyBean
    private InMemoryDataStorage inMemoryDataStorage;
    @SpyBean
    private BuyerService buyerService;

    private static boolean initialised = false;

    @BeforeEach
    public void init() throws Exception {
        if (initialised) {
            return;
        }
        Show show = new Show("1", 10, 10, 120000);
        inMemoryDataStorage.createShow(show);
        initialised = true;
        System.out.println("Only run once");
    }

    @Test
    public void testAvailability() throws Exception {
        buyerController.availability("1");
        Mockito.verify(buyerService, Mockito.times(1)).getShowAvailableSeats("1");
        Mockito.verify(inMemoryDataStorage, Mockito.times(1)).getShow("1");
    }

    @Test
    @Order(1)
    public void testBook() throws Exception {
        UUID bookingId = UUID.fromString("76bb18c0-86c6-446e-884d-37550247d49d");
        UUID ticketId1 = UUID.fromString("86bb18c0-86c6-446e-884d-37550247d49d");
        UUID ticketId2 = UUID.fromString("96bb18c0-86c6-446e-884d-37550247d49d");

        ZonedDateTime currentTime =
                ZonedDateTime.of(2022, 1, 1, 0, 0, 0, 0, ZoneId.of("UTC+8"));
        Clock currentClock =
                Clock.fixed(currentTime.toInstant(), ZoneId.of("UTC+8"));

        try (MockedStatic<UUID> mockUUID = Mockito.mockStatic(UUID.class);
             MockedStatic<Clock> mockClock = Mockito.mockStatic(Clock.class)) {
            mockUUID.when(UUID::randomUUID).thenReturn(bookingId, ticketId1, ticketId2);
            mockClock.when(Clock::systemUTC).thenReturn(currentClock);

            buyerController.book("1", "123", "A1,A2");

            ArgumentCaptor<String> showNumberCaptor = ArgumentCaptor.forClass(String.class);
            ArgumentCaptor<String> phoneNumberCaptor = ArgumentCaptor.forClass(String.class);
            ArgumentCaptor<List<String>> seatsArgumentCaptor = ArgumentCaptor.forClass(List.class);
            Mockito.verify(buyerService, Mockito.times(1))
                    .bookShow(showNumberCaptor.capture(), phoneNumberCaptor.capture(), seatsArgumentCaptor.capture());
            Assertions.assertEquals("1", showNumberCaptor.getValue());
            Assertions.assertEquals("123", phoneNumberCaptor.getValue());
            assertThat(Arrays.asList("A1", "A2")).hasSameElementsAs(seatsArgumentCaptor.getValue());

            ArgumentCaptor<String> showNoCaptor = ArgumentCaptor.forClass(String.class);
            ArgumentCaptor<String> phoneNoCaptor = ArgumentCaptor.forClass(String.class);
            ArgumentCaptor<Booking> bookingArgumentCaptor = ArgumentCaptor.forClass(Booking.class);
            Mockito.verify(inMemoryDataStorage, Mockito.times(1))
                    .addNewBookingToShow(showNoCaptor.capture(), phoneNoCaptor.capture(), bookingArgumentCaptor.capture());

            Show expected = new Show("1", 10, 10, 120000);
            Booking booking = new Booking(bookingId.toString(), "1", "123", Instant.now().toEpochMilli());
            Ticket ticket = new Ticket(ticketId1.toString(), "A1", "1", bookingId.toString());
            booking.getTickets().put(ticketId1.toString(), ticket);
            ticket = new Ticket(ticketId2.toString(), "A2", "1", bookingId.toString());
            booking.getTickets().put(ticketId2.toString(), ticket);
            expected.getBookings().put("123", booking);
            expected.getSeatStatusMap().put("A1", SeatStatus.OCCUPIED);
            expected.getSeatStatusMap().put("A2", SeatStatus.OCCUPIED);

            Assertions.assertEquals("1", showNoCaptor.getValue());
            Assertions.assertEquals("123", phoneNoCaptor.getValue());
            Assertions.assertEquals(booking, bookingArgumentCaptor.getValue());
            Assertions.assertEquals(expected, inMemoryDataStorage.getShow("1"));
        }
    }

    @Test
    @Order(2)
    public void testCancel() throws Exception {
        // Must be same as testBook
        UUID bookingId = UUID.fromString("76bb18c0-86c6-446e-884d-37550247d49d");
        UUID ticketId1 = UUID.fromString("86bb18c0-86c6-446e-884d-37550247d49d");
        UUID ticketId2 = UUID.fromString("96bb18c0-86c6-446e-884d-37550247d49d");

        ZonedDateTime currentTime =
                ZonedDateTime.of(2022, 1, 1, 0, 0, 0, 0, ZoneId.of("UTC+8"));
        Clock currentClock =
                Clock.fixed(currentTime.toInstant(), ZoneId.of("UTC+8"));

        try (MockedStatic<Clock> mockClock = Mockito.mockStatic(Clock.class)) {
            mockClock.when(Clock::systemUTC).thenReturn(currentClock);

            // cancel ticketId1 (seat A1)
            buyerController.cancel(ticketId1.toString(), "123");

            Mockito.verify(buyerService, Mockito.times(1))
                    .cancelTicket(ticketId1.toString(), "123");
            Mockito.verify(inMemoryDataStorage, Mockito.times(1))
                    .removeTicketFromShow("1", "123", ticketId1.toString());

            Show expected = new Show("1", 10, 10, 120000);
            Booking booking = new Booking(bookingId.toString(), "1", "123", Instant.now().toEpochMilli());
            Ticket ticket = new Ticket(ticketId2.toString(), "A2", "1", bookingId.toString());
            booking.getTickets().put(ticketId2.toString(), ticket);
            expected.getBookings().put("123", booking);
            expected.getSeatStatusMap().put("A2", SeatStatus.OCCUPIED);
            Assertions.assertEquals(expected, inMemoryDataStorage.getShow("1"));
        }
    }

    @Test
    @Order(3)
    public void testBookFail() throws Exception {
        // book the same show with the same phone number
        buyerController.book("1", "123", "C1");
        Assertions.assertThrows(InvalidBookingException.class, () -> buyerService.bookShow("1", "123", Arrays.asList("C1")));

        // book the show with a seat that is not available
        buyerController.book("1", "345", "C1,A2");
        Assertions.assertThrows(InvalidTicketException.class, () -> buyerService.bookShow("1", "345", Arrays.asList("C1", "A2")));
        // Seat C1 must be available, A2 is the seat that is double booked
        SeatStatus seatStatusC1 = inMemoryDataStorage.checkSeatStatus("1", "C1");
        Assertions.assertEquals(SeatStatus.AVAILABLE, seatStatusC1);
        // Seat A2 must still be occupied
        SeatStatus seatStatusA2 = inMemoryDataStorage.checkSeatStatus("1", "A2");
        Assertions.assertEquals(SeatStatus.OCCUPIED, seatStatusA2);
    }

    @Test
    @Order(4)
    public void testCancelFail() {
        // cancel an invalid ticket number
        buyerController.cancel("invalid-ticket-id", "123");
        Assertions.assertThrows(InvalidTicketException.class, () -> buyerService.cancelTicket("invalid-ticket-id", "123"));

        // cancel an invalid phone number
        UUID ticketId2 = UUID.fromString("96bb18c0-86c6-446e-884d-37550247d49d");  // Must be same as testBook
        buyerController.cancel(ticketId2.toString(), "345");
        Assertions.assertThrows(InvalidTicketException.class, () -> buyerService.cancelTicket(ticketId2.toString(), "345"));

        // Exceed cancellation time window
        ZonedDateTime currentTime =
                ZonedDateTime.of(2022, 1, 1, 1, 0, 0, 0, ZoneId.of("UTC+8"));
        Clock currentClock =
                Clock.fixed(currentTime.toInstant(), ZoneId.of("UTC+8"));
        try (MockedStatic<Clock> mockClock = Mockito.mockStatic(Clock.class)) {
            mockClock.when(Clock::systemUTC).thenReturn(currentClock);
            buyerController.cancel(ticketId2.toString(), "123");
            Assertions.assertThrows(InvalidCancellationException.class, () -> buyerService.cancelTicket(ticketId2.toString(), "123"));
        }
    }
}
