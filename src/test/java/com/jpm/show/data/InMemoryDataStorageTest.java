package com.jpm.show.data;

import com.jpm.show.entity.Booking;
import com.jpm.show.entity.SeatStatus;
import com.jpm.show.entity.Show;
import com.jpm.show.entity.Ticket;
import com.jpm.show.exception.InvalidShowException;
import com.jpm.show.exception.InvalidTicketException;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class InMemoryDataStorageTest {
    @Autowired
    private InMemoryDataStorage inMemoryDataStorage;

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
    @Order(1)
    public void testCreateShow() throws Exception {
        Show newShow = new Show("2", 20, 20, 220000);
        inMemoryDataStorage.createShow(newShow);
        Assertions.assertEquals(newShow, inMemoryDataStorage.getShow("2"));
        Show show = new Show("1", 10, 10, 120000); // same as init
        Assertions.assertEquals(show, inMemoryDataStorage.getShow("1"));
    }

    @Test
    public void testCreateShowFail() {
        Show newShow = new Show("1", 20, 20, 220000);
        Assertions.assertThrows(InvalidShowException.class, () -> inMemoryDataStorage.createShow(newShow));
    }

    @Test
    public void testGetShowFail() {
        Assertions.assertThrows(InvalidShowException.class, () -> inMemoryDataStorage.getShow("101"));
    }

    @Test
    public void testCheckSeatStatus() throws Exception {
        Assertions.assertEquals(SeatStatus.AVAILABLE, inMemoryDataStorage.checkSeatStatus("1", "A1"));
    }

    @Test
    public void testCheckSeatStatusFail() {
        // Fail from invalid show number
        Assertions.assertThrows(InvalidShowException.class, () -> inMemoryDataStorage.checkSeatStatus("101", "A1"));
        // Fail from invalid seat number
        Assertions.assertThrows(InvalidShowException.class, () -> inMemoryDataStorage.checkSeatStatus("1", "AA1"));
    }

    @Test
    @Order(2)
    public void testUpdateSeatStatus() throws Exception {
        inMemoryDataStorage.updateSeatStatus("2", "A1", SeatStatus.OCCUPIED);
        Show expected = new Show("2", 20, 20, 220000); // same as testCreateShow
        expected.getSeatStatusMap().put("A1", SeatStatus.OCCUPIED);
        Assertions.assertEquals(expected, inMemoryDataStorage.getShow("2"));
    }

    @Test
    public void testUpdateSeatStatusFail() throws Exception {
        // Fail from invalid show number
        Assertions.assertThrows(InvalidShowException.class, () -> inMemoryDataStorage.updateSeatStatus("101", "A1", SeatStatus.OCCUPIED));
        // Fail from invalid seat number
        Assertions.assertThrows(InvalidShowException.class, () -> inMemoryDataStorage.updateSeatStatus("1", "AA1", SeatStatus.OCCUPIED));
        Show expected = new Show("1", 10, 10, 120000); // same as init
        Assertions.assertEquals(expected, inMemoryDataStorage.getShow("1"));
    }

    @Test
    @Order(3)
    public void testAddNewBookingToShow() throws Exception {
        Show expected = new Show("2", 20, 20, 220000); // same as testCreateShow
        expected.getSeatStatusMap().put("A1", SeatStatus.OCCUPIED);  // same as testUpdateSeatStatus
        Booking booking = new Booking("booking1", "2", "123", 123456);
        Ticket ticket = new Ticket("ticket1", "B1", "2", "booking1");
        booking.getTickets().put("ticket1", ticket);
        expected.getBookings().put("123", booking);

        inMemoryDataStorage.addNewBookingToShow("2", "123", booking);
        Assertions.assertEquals(expected, inMemoryDataStorage.getShow("2"));
        Assertions.assertEquals(expected, inMemoryDataStorage.getShowFromTicketId("ticket1"));
    }

    @Test
    public void testAddNewBookingToShowFail() {
        Booking booking = new Booking("booking1", "101", "123", 123456);
        Assertions.assertThrows(InvalidShowException.class, () -> inMemoryDataStorage.addNewBookingToShow("101", "123", booking));
    }

    @Test
    @Order(4)
    public void testRemoveTicketFromShow() throws Exception {
        inMemoryDataStorage.removeTicketFromShow("2", "123", "ticket1"); // same as testAddNewBookingToShow
        Show expected = new Show("2", 20, 20, 220000); // same as testCreateShow
        expected.getSeatStatusMap().put("A1", SeatStatus.OCCUPIED);  // testUpdateSeatStatus
        Booking booking = new Booking("booking1", "2", "123", 123456);
        expected.getBookings().put("123", booking); // ticket is removed
        Assertions.assertEquals(expected, inMemoryDataStorage.getShow("2"));
        Assertions.assertThrows(InvalidTicketException.class, () -> inMemoryDataStorage.getShowFromTicketId("ticket1"));
    }

    @Test
    public void testRemoveTicketFromShowFail() {
        // Fail from invalid show id
        Assertions.assertThrows(InvalidShowException.class, () -> inMemoryDataStorage.removeTicketFromShow("101", "123", "ticket1"));
        // Fail from invalid ticket id
        Assertions.assertThrows(InvalidTicketException.class, () -> inMemoryDataStorage.removeTicketFromShow("1", "123", "ticket1"));
    }
}
