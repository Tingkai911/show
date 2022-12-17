package com.jpm.show.executor;

import com.jpm.show.controller.AdminController;
import com.jpm.show.controller.BuyerController;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

@Slf4j
@Profile("!test") // don't run during unit test
@Component
@AllArgsConstructor
public class CommandLineTaskExecutor implements CommandLineRunner {
    private AdminController adminController;
    private BuyerController buyerController;

    @Override
    public void run(String... args) throws Exception {
        while (true) {
            System.out.println("Enter Command:");
            Scanner scanner = new Scanner(System.in);
            String line = scanner.nextLine().trim();

            List<String> params = Arrays.asList(line.split("\\s+"));
            if (params.size() == 0) {
                System.out.println("Invalid command");
                log.error("Invalid command");
                continue;
            }

            String user;
            switch (params.get(0)) {
                case "setup":
                    if (params.size() != 5) {
                        System.out.println("Invalid command, required 5 parameters, provided " + params.size());
                        log.error("Invalid command, required 5 parameters, provided " + params.size());
                        break;
                    }
                    System.out.println("Password:");
                    user = scanner.nextLine().trim();
                    adminController.setup(user, params.get(1), params.get(2), params.get(3), params.get(4));
                    break;
                case "view":
                    if (params.size() != 2) {
                        System.out.println("Invalid command, require 2 parameters, provided " + params.size());
                        log.error("Invalid command, require 2 parameters, provided " + params.size());
                        break;
                    }
                    System.out.println("Password:");
                    user = scanner.nextLine().trim();
                    adminController.view(user, params.get(1));
                    break;
                case "availability":
                    if (params.size() != 2) {
                        System.out.println("Invalid command, require 2 parameters, provided " + params.size());
                        log.error("Invalid command, require 2 parameters, provided " + params.size());
                        break;
                    }
                    buyerController.availability(params.get(1));
                    break;
                case "book":
                    if (params.size() != 4) {
                        System.out.println("Invalid command, require 4 parameters, provided " + params.size());
                        log.error("Invalid command, require 4 parameters, provided " + params.size());
                        break;
                    }
                    buyerController.book(params.get(1), params.get(2), params.get(3));
                    break;
                case "cancel":
                    if (params.size() != 3) {
                        System.out.println("Invalid command, require 3 parameters, provided " + params.size());
                        log.error("Invalid command, require 3 parameters, provided " + params.size());
                        break;
                    }
                    buyerController.cancel(params.get(1), params.get(2));
                    break;
                case "exit":
                    // WARNING! This will clear all in-memory data.
                    System.out.println("Are you sure? (y/n)");
                    String choice = scanner.nextLine().trim();
                    if (choice.equalsIgnoreCase("y")) {
                        System.exit(1);
                    }
                    break;
                default:
                    System.out.println("Invalid command, refer to README.md");
                    log.error("Invalid command, refer to README.md");
            }
        }
    }
}
