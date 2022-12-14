package com.jpm.show;

import lombok.AllArgsConstructor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@AllArgsConstructor
public class ShowApplication {
    public static void main(String[] args) {
        SpringApplication.run(ShowApplication.class, args);
    }
}
