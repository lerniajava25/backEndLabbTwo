package org.example.springbootwebapp.repository;

import org.example.springbootwebapp.entity.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TicketRepository extends JpaRepository<Ticket, Long> {
    List<Ticket> findByCreator_Id(Long creatorId);
}
