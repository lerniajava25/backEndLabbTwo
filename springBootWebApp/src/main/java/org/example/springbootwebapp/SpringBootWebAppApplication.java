package org.example.springbootwebapp;

import org.example.springbootwebapp.entity.Ticket;
import org.example.springbootwebapp.entity.User;
import org.example.springbootwebapp.repository.TicketRepository;
import org.example.springbootwebapp.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class SpringBootWebAppApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringBootWebAppApplication.class, args);
    }

    @Bean
    CommandLineRunner createTestUser(UserRepository userRepository) {
        return args -> {
            if (userRepository.findByUsername("testuser").isEmpty()) {
                // This test user has no usable password until login is added.
                userRepository.save(new User("testuser", "!", "USER"));
            }
        };
    }

    @Bean
    CommandLineRunner createTestData(UserRepository userRepository,
                                     TicketRepository ticketRepository) {
        return args -> {
            User testUser = userRepository.findByUsername("testuser").orElse(null);

            if (testUser == null) {
                testUser = userRepository.save(new User("testuser", "!", "USER"));
            }

            if (ticketRepository.count() == 0) {
                Ticket ticket = new Ticket(
                        "Kan inte logga in",
                        "Jag får ett felmeddelande när jag försöker logga in.",
                        testUser
                );
                ticketRepository.save(ticket);
            }
        };
    }
}
