package net.zephyrlink.zephyrmail.mail.web;

import net.zephyrlink.zephyrmail.mail.outbound.OutboxQueue;
import net.zephyrlink.zephyrmail.user.User;
import net.zephyrlink.zephyrmail.mail.outbound.OutboxQueueService;
import net.zephyrlink.zephyrmail.user.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/draft")
public class DraftApiController {

    private final OutboxQueueService outboxQueueService;
    private final UserService userService;

    public DraftApiController(OutboxQueueService outboxQueueService,
                              UserService userService) {
        this.outboxQueueService = outboxQueueService;
        this.userService = userService;
    }

    @PostMapping("/autosave")
    public ResponseEntity<?> autosave(
            @RequestParam(required = false) Long draftId,
            @RequestParam(required = false) String to,
            @RequestParam(required = false) String cc,
            @RequestParam(required = false) String bcc,
            @RequestParam(required = false) String subject,
            @RequestParam(required = false) String body,
            @AuthenticationPrincipal UserDetails userDetails) {

        User user = userService.findActiveByEmail(userDetails.getUsername()).orElseThrow();

        // NULL SAFE
        String safeTo      = (to      != null) ? to      : "";
        String safeCc      = (cc      != null) ? cc      : "";
        String safeBcc     = (bcc     != null) ? bcc     : "";
        String safeSubject = (subject != null) ? subject : "";
        String safeBody    = (body    != null) ? body    : "";

        OutboxQueue saved = outboxQueueService.saveDraft(user, draftId, safeTo, safeCc, safeBcc,
                safeSubject, safeBody);

        return ResponseEntity.ok(Map.of("id", saved.getId()));
    }

    @PostMapping("/cancel/{id}")
    public ResponseEntity<?> cancel(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.findActiveByEmail(userDetails.getUsername()).orElseThrow();
        boolean cancelled = outboxQueueService.cancel(id, user);
        return cancelled ? ResponseEntity.ok().build() : ResponseEntity.badRequest().build();
    }
}