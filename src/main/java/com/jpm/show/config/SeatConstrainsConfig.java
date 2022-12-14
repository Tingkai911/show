package com.jpm.show.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties("constrains.seats")
public class SeatConstrainsConfig {
    private int maxRows;
    private int maxSeatsPerRow;
}
