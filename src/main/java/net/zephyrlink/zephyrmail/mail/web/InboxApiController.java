package net.zephyrlink.zephyrmail.mail.web;

import net.zephyrlink.zephyrmail.mail.Email;
import net.zephyrlink.zephyrmail.mail.EmailRepository;
import net.zephyrlink.zephyrmail.user.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/inbox")
public class InboxApiController {

    private final EmailRepository emailRepository;
    private final UserService userService;

    public InboxApiController(EmailRepository emailRepository,
                              UserService userService) {
        this.emailRepository = emailRepository;
        this.userService = userService;
    }

    // MARK READ
    @PostMapping("/read/{id}")
    public ResponseEntity<?> markRead(@PathVariable Long id) {
        // READ IS NOT A STATE CHANGE — email stays in its current folder
        // endpoint exists for frontend calls, no state mutation needed
        return ResponseEntity.ok().build();
    }

    // TRASH SINGLE
    @PostMapping("/trash/{id}")
    public ResponseEntity<?> trash(@PathVariable Long id,
                                   @AuthenticationPrincipal UserDetails userDetails) {
        Optional<Email> emailOpt = emailRepository.findById(id);
        if (emailOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Email email = emailOpt.get();
        if (!email.getOwner().getEmailAddress().equals(userDetails.getUsername())) {
            return ResponseEntity.status(403).build();
        }
        email.setDeleted(true);
        emailRepository.save(email);
        return ResponseEntity.ok().build();
    }

    // TRASH BULK
    @PostMapping("/trash/bulk")
    public ResponseEntity<?> trashBulk(@RequestBody List<Long> ids,
                                       @AuthenticationPrincipal UserDetails userDetails) {
        for (Long id : ids) {
            Optional<Email> emailOpt = emailRepository.findById(id);
            if (emailOpt.isEmpty()) continue;
            Email email = emailOpt.get();
            if (!email.getOwner().getEmailAddress().equals(userDetails.getUsername())) continue;
            email.setDeleted(true);
            emailRepository.save(email);
        }
        return ResponseEntity.ok().build();
    }

    // BURN AFTER READ
    @DeleteMapping("/burn/{id}")
    public ResponseEntity<?> burn(@PathVariable Long id,
                                  @AuthenticationPrincipal UserDetails userDetails) {
        Optional<Email> emailOpt = emailRepository.findById(id);
        if (emailOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Email email = emailOpt.get();
        if (!email.getOwner().getEmailAddress().equals(userDetails.getUsername())) {
            return ResponseEntity.status(403).build();
        }
        if (email.isBurnAfterRead()) {
            emailRepository.delete(email);
        }
        return ResponseEntity.ok().build();
    }
}