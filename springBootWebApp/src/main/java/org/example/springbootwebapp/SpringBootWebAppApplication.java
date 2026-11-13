package org.example.springbootwebapp;

import org.example.springbootwebapp.entity.Ticket;
import org.example.springbootwebapp.entity.User;
import org.example.springbootwebapp.repository.TicketRepository;
import org.example.springbootwebapp.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
public class SpringBootWebAppApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringBootWebAppApplication.class, args);
    }

    @Bean
    CommandLineRunner createTestData(UserRepository userRepository,
                                     TicketRepository ticketRepository,
                                     PasswordEncoder passwordEncoder) {
        return args -> {
            User testUser = prepareTestAccount(userRepository, passwordEncoder, "testuser", "USER", "user-test-123");
            prepareTestAccount(userRepository, passwordEncoder, "testagent", "AGENT", "agent-test-123");

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

    private User prepareTestAccount(UserRepository userRepository, PasswordEncoder passwordEncoder,
                                    String username, String role, String password) {
        User account = userRepository.findByUsername(username).orElse(null);
        if (account == null) {
            return userRepository.save(new User(username, passwordEncoder.encode(password), role));
        }
        if ("!".equals(account.getPasswordHash())) {
            account.setPasswordHash(passwordEncoder.encode(password));
            return userRepository.save(account);
        }
        return account;
    }
}
