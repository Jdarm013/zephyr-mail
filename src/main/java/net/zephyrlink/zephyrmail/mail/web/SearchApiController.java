package net.zephyrlink.zephyrmail.mail.web;

import net.zephyrlink.zephyrmail.mail.Email;
import net.zephyrlink.zephyrmail.user.User;
import net.zephyrlink.zephyrmail.mail.EmailRepository;
import net.zephyrlink.zephyrmail.user.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/search")
public class SearchApiController {

    private final EmailRepository emailRepository;
    private final UserService userService;

    public SearchApiController(EmailRepository emailRepository,
                               UserService userService) {
        this.emailRepository = emailRepository;
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<List<Email>> search(
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String subject,
            @RequestParam(required = false) String body,
            @AuthenticationPrincipal UserDetails userDetails) {

        User user = userService.findActiveByEmail(userDetails.getUsername()).orElseThrow();

        List<Email> results = emailRepository
                .findByOwnerAndStateOrderByReceivedAtDesc(user, Email.EmailState.INBOX)
                .stream()
                .filter(e -> from == null || e.getSenderAddress().contains(from))
                .filter(e -> subject == null || (e.getSubject() != null &&
                        e.getSubject().contains(subject)))
                .filter(e -> body == null || (e.getBody() != null &&
                        e.getBody().contains(body)))
                .collect(Collectors.toList());

        return ResponseEntity.ok(results);
    }

    @GetMapping("/timeslider")
    public ResponseEntity<List<Email>> timeSlider(
            @RequestParam String timestamp,
            @AuthenticationPrincipal UserDetails userDetails) {

        User user = userService.findActiveByEmail(userDetails.getUsername()).orElseThrow();
        LocalDateTime pointInTime = LocalDateTime.parse(timestamp);

        List<Email> results = emailRepository
                .findByOwnerAndStateOrderByReceivedAtDesc(user, Email.EmailState.INBOX)
                .stream()
                .filter(e -> e.getReceivedAt().isBefore(pointInTime))
                .collect(Collectors.toList());

        return ResponseEntity.ok(results);
    }
}