package org.example.springbootwebapp.repository;

import org.example.springbootwebapp.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByTicket_IdOrderByIdAsc(Long ticketId);
}
