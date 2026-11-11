package org.example.springbootwebapp.controller;

import org.example.springbootwebapp.entity.Ticket;
import org.example.springbootwebapp.entity.User;
import org.example.springbootwebapp.repository.TicketRepository;
import org.example.springbootwebapp.repository.UserRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
public class TicketController {
    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;

    public TicketController(TicketRepository ticketRepository, UserRepository userRepository) {
        this.ticketRepository = ticketRepository;
        this.userRepository = userRepository;
    }

    @GetMapping("/tickets")
    public String showTickets(Model model) {
        List<Ticket> tickets = ticketRepository.findAll();
        model.addAttribute("tickets", tickets);
        return "tickets";
    }

    @GetMapping("/tickets/new")
    public String showNewTicketForm() {
        return "ticket-new";
    }

    @PostMapping("/tickets")
    public String createTicket(@RequestParam(name = "title", defaultValue = "") String title,
                               @RequestParam(name = "description", defaultValue = "") String description,
                               Model model, RedirectAttributes redirectAttributes) {
        model.addAttribute("title", title);
        model.addAttribute("description", description);

        if (title.isBlank() || description.isBlank()) {
            model.addAttribute("error", "Fyll i både rubrik och beskrivning.");
            return "ticket-new";
        }

        if (title.length() > 255 || description.length() > 4000) {
            model.addAttribute("error", "Rubriken får ha högst 255 tecken och beskrivningen högst 4 000 tecken.");
            return "ticket-new";
        }

        User creator = userRepository.findByUsername("testuser").orElse(null);
        if (creator == null) {
            model.addAttribute("error", "Testanvändaren saknas. Ärendet kunde inte sparas.");
            return "ticket-new";
        }

        Ticket ticket = new Ticket(title.strip(), description.strip(), creator);
        ticketRepository.save(ticket);
        redirectAttributes.addFlashAttribute("message", "Ärendet skapades.");
        return "redirect:/tickets";
    }
}
