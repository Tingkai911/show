package com.jpm.show.controller;

import com.jpm.show.data.InMemoryDataStorage;
import com.jpm.show.entity.Booking;
import com.jpm.show.entity.SeatStatus;
import com.jpm.show.entity.Show;
import com.jpm.show.entity.Ticket;
import com.jpm.show.exception.InvalidShowException;
import com.jpm.show.service.AdminService;
import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;

@ActiveProfiles("test")
@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AdminControllerTest {
    @Autowired
    private AdminController adminController;

    @SpyBean
    private InMemoryDataStorage inMemoryDataStorage;

    @SpyBean
    private AdminService adminService;

    @Test
    @Order(1)
    public void testSetup() throws Exception {
        adminController.setup("test123", "1", "10", "10", "2");
        Show expected = new Show("1", 10, 10, 120000);
        Mockito.verify(adminService, Mockito.times(1)).addShow("1", 10, 10, 2);
        Mockito.verify(inMemoryDataStorage, Mockito.times(1)).createShow(expected);
        Show actual = inMemoryDataStorage.getShow("1");
        Assertions.assertEquals(expected, actual);
    }

    @Test
    @Order(2)
    public void testInvalidSetup() {
        // showNumber already exist
        adminController.setup("test123", "1", "10", "10", "2");
        Show show = new Show("1", 10, 10, 120000);
        Assert.assertThrows(InvalidShowException.class, () -> inMemoryDataStorage.createShow(show));

        // invalid numRows
        adminController.setup("test123", "2", "100", "10", "2");
        Assert.assertThrows(InvalidShowException.class, () -> adminService.addShow("2", 100, 10, 2));

        // invalid numSeatsPerRow
        adminController.setup("test123", "2", "10", "100", "2");
        Assert.assertThrows(InvalidShowException.class, () -> adminService.addShow("2", 10, 100, 2));
    }

    @Test
    public void testView() throws Exception {
        // Mock data with booking
        Show mockShow = new Show("1", 10, 10, 120000);
        Booking booking = new Booking("1", "1", "123", Instant.now().toEpochMilli());
        Ticket ticket = new Ticket("1", "A1", "1", "1");
        booking.getTickets().put("1", ticket);
        mockShow.getBookings().put("123", booking);
        mockShow.getSeatStatusMap().put("A1", SeatStatus.RESERVED);
        Mockito.doReturn(mockShow).when(inMemoryDataStorage).getShow("1");

        adminController.view("test123", "1");

        Mockito.verify(adminService, Mockito.times(1)).viewShow("1");
        Mockito.verify(inMemoryDataStorage, Mockito.times(1)).getShow("1");
    }

    @Test
    public void testInvalidView() {
        // show number don't exist
        adminController.view("test123", "1010");
        Assert.assertThrows(InvalidShowException.class, () -> inMemoryDataStorage.getShow("1010"));
    }

    @Test
    public void testAuthorizationFail() throws Exception {
        adminController.setup("12345", "1", "10", "10", "2");
        adminController.view("12345", "1");
        Mockito.verify(adminService, Mockito.times(0)).viewShow("1");
        Mockito.verify(inMemoryDataStorage, Mockito.times(0)).getShow("1");
    }
}
