package org.example.springbootwebapp.repository;

import org.example.springbootwebapp.entity.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TicketRepository extends JpaRepository<Ticket, Long> {
}
