package com.jpm.show.service;

import com.jpm.show.config.SeatConstrainsConfig;
import com.jpm.show.data.InMemoryDataStorage;
import com.jpm.show.entity.Show;
import com.jpm.show.exception.InvalidShowException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;


// Services are usable for different interfaces (i.e. both command line and REST api can use the same service)
@Slf4j
@Service
@AllArgsConstructor
public class AdminService {
    private InMemoryDataStorage inMemoryDataStorage;
    public SeatConstrainsConfig seatConstrainsConfig;

    public void addShow(String showNumber, int numRows, int numSeatsPerRow, long cancellationWindowMinutes) throws Exception {
        if (numRows > seatConstrainsConfig.getMaxRows() || numSeatsPerRow > seatConstrainsConfig.getMaxSeatsPerRow()
                || numRows <= 0 || numSeatsPerRow <= 0) {
            throw new InvalidShowException("Number of seats more than the allowed limit");
        }

        // flush to in-memory data
        Show newShow = new Show(showNumber, numRows, numSeatsPerRow, cancellationWindowMinutes * 60 * 1000);
        inMemoryDataStorage.createShow(newShow);
    }

    public Show viewShow(String showNumber) throws Exception {
        return inMemoryDataStorage.getShow(showNumber);
    }
}
