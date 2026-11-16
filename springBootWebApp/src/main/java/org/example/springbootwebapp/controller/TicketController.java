package org.example.springbootwebapp.controller;

import org.example.springbootwebapp.entity.Comment;
import org.example.springbootwebapp.entity.Ticket;
import org.example.springbootwebapp.entity.User;
import org.example.springbootwebapp.repository.CommentRepository;
import org.example.springbootwebapp.repository.TicketRepository;
import org.example.springbootwebapp.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
public class TicketController {
    private static final List<String> STATUSES = List.of(
            "NEW", "IN_PROGRESS", "WAITING_FOR_USER", "RESOLVED", "CLOSED");

    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;

    public TicketController(TicketRepository ticketRepository, UserRepository userRepository,
                            CommentRepository commentRepository) {
        this.ticketRepository = ticketRepository;
        this.userRepository = userRepository;
        this.commentRepository = commentRepository;
    }

    @GetMapping("/tickets")
    public String showTickets(Authentication authentication, Model model) {
        User user = findLoggedInUser(authentication);
        List<Ticket> tickets = isAgent(user)
                ? ticketRepository.findAll()
                : ticketRepository.findByCreator_Id(user.getId());
        model.addAttribute("tickets", tickets);
        return "tickets";
    }

    @GetMapping("/tickets/new")
    public String showNewTicketForm() {
        return "ticket-new";
    }

    @GetMapping("/tickets/{id}")
    public String showTicketDetails(@PathVariable("id") Long id, Authentication authentication, Model model) {
        User user = findLoggedInUser(authentication);
        model.addAttribute("ticket", findAccessibleTicket(id, user));
        model.addAttribute("agent", isAgent(user));
        model.addAttribute("comments", commentRepository.findByTicket_IdOrderByIdAsc(id));
        model.addAttribute("statuses", STATUSES);
        return "ticket-details";
    }

    @PostMapping("/tickets/{id}/assign")
    public String assignTicket(@PathVariable("id") Long id, Authentication authentication) {
        User agent = requireAgent(authentication);

        Ticket ticket = findTicket(id);
        ticket.setAssignedAgent(agent);
        ticketRepository.save(ticket);
        return "redirect:/tickets/" + id;
    }

    @PostMapping("/tickets/{id}/status")
    public String changeStatus(@PathVariable("id") Long id,
                               @RequestParam("status") String status, Authentication authentication) {
        requireAgent(authentication);
        Ticket ticket = findTicket(id);
        if (!STATUSES.contains(status)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ogiltig status.");
        }
        ticket.setStatus(status);
        ticketRepository.save(ticket);
        return "redirect:/tickets/" + id;
    }

    @PostMapping("/tickets/{id}/comments")
    public String addComment(@PathVariable("id") Long id,
                             @RequestParam(name = "text", defaultValue = "") String text,
                             Authentication authentication, RedirectAttributes redirectAttributes) {
        User author = findLoggedInUser(authentication);
        Ticket ticket = findAccessibleTicket(id, author);
        String redirect = "redirect:/tickets/" + id;
        String commentText = text.strip();

        if (commentText.isEmpty() || commentText.length() > 4000) {
            redirectAttributes.addFlashAttribute("commentError", "Skriv en kommentar på högst 4 000 tecken.");
            redirectAttributes.addFlashAttribute("commentText", text);
            return redirect;
        }

        commentRepository.save(new Comment(commentText, author, ticket));
        return redirect;
    }

    private Ticket findTicket(Long id) {
        return ticketRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ärendet finns inte."));
    }

    private Ticket findAccessibleTicket(Long id, User user) {
        Ticket ticket = findTicket(id);
        if (!isAgent(user) && !user.getId().equals(ticket.getCreator().getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Du har inte åtkomst till ärendet.");
        }
        return ticket;
    }

    private boolean isAgent(User user) {
        return "AGENT".equals(user.getRole());
    }

    private User requireAgent(Authentication authentication) {
        User user = findLoggedInUser(authentication);
        if (!isAgent(user)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Åtgärden kräver rollen AGENT.");
        }
        return user;
    }

    @PostMapping("/tickets")
    public String createTicket(@RequestParam(name = "title", defaultValue = "") String title,
                               @RequestParam(name = "description", defaultValue = "") String description,
                               Authentication authentication, Model model, RedirectAttributes redirectAttributes) {
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

        User creator = findLoggedInUser(authentication);
        Ticket ticket = new Ticket(title.strip(), description.strip(), creator);
        ticketRepository.save(ticket);
        redirectAttributes.addFlashAttribute("message", "Ärendet skapades.");
        return "redirect:/tickets";
    }

    private User findLoggedInUser(Authentication authentication) {
        return userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Användarkontot finns inte."));
    }
}
