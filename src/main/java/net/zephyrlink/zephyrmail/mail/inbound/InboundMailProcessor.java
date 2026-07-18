package net.zephyrlink.zephyrmail.mail.inbound;

import net.zephyrlink.zephyrmail.alias.ShadowRoute;
import net.zephyrlink.zephyrmail.alias.ShadowRouteService;
import net.zephyrlink.zephyrmail.mail.Email;
import net.zephyrlink.zephyrmail.mail.EmailRepository;
import net.zephyrlink.zephyrmail.mail.flow.MailFlowRuleEngine;
import net.zephyrlink.zephyrmail.security.threat.ThreatGrader;
import net.zephyrlink.zephyrmail.security.threat.ThreatLevelService;
import net.zephyrlink.zephyrmail.user.User;
import net.zephyrlink.zephyrmail.user.UserService;
import org.springframework.stereotype.Service;

import java.util.Optional;

// Shared acceptance/delivery logic for every inbound mail source (SMTP on 2525, and the
// Resend inbound webhook). Each source is responsible for its own transport-specific concerns
// (MIME parsing for SMTP, signature verification for the webhook) and calls in here once it
// has a plain from/recipient/subject/body.
@Service
public class InboundMailProcessor {

    private final EmailRepository emailRepository;
    private final UserService userService;
    private final BounceBlockService bounceBlockService;
    private final SpamBurnerService spamBurnerService;
    private final ShadowRouteService shadowRouteService;
    private final ThreatGrader threatGrader;
    private final TrackerDefanger trackerDefanger;
    private final MailFlowRuleEngine mailFlowRuleEngine;
    private final ThreatLevelService threatLevelService;

    public InboundMailProcessor(EmailRepository emailRepository,
                                UserService userService,
                                BounceBlockService bounceBlockService,
                                SpamBurnerService spamBurnerService,
                                ShadowRouteService shadowRouteService,
                                ThreatGrader threatGrader,
                                TrackerDefanger trackerDefanger,
                                MailFlowRuleEngine mailFlowRuleEngine,
                                ThreatLevelService threatLevelService) {
        this.emailRepository = emailRepository;
        this.userService = userService;
        this.bounceBlockService = bounceBlockService;
        this.spamBurnerService = spamBurnerService;
        this.shadowRouteService = shadowRouteService;
        this.threatGrader = threatGrader;
        this.trackerDefanger = trackerDefanger;
        this.mailFlowRuleEngine = mailFlowRuleEngine;
        this.threatLevelService = threatLevelService;
    }

    public boolean accept(String from, String recipient) {
        if (bounceBlockService.isBlocked(from)) {
            return false;
        }
        if (spamBurnerService.isBurned(from)) {
            return false;
        }
        if (shadowRouteService.resolve(recipient).isPresent()) {
            return true;
        }
        return userService.findActiveByEmail(recipient).isPresent();
    }

    public void process(String from, String recipient, String subject, String rawBody) {
        Optional<ShadowRoute> shadowRoute = shadowRouteService.resolve(recipient);
        User owner;
        if (shadowRoute.isPresent()) {
            owner = shadowRoute.get().getOwner();
            shadowRouteService.incrementForwardCount(shadowRoute.get());
        } else {
            owner = userService.findActiveByEmail(recipient).orElseThrow();
        }

        Email incomingEmail = new Email();
        incomingEmail.setOwner(owner);
        incomingEmail.setSenderAddress(from);
        incomingEmail.setRecipientAddress(recipient);
        incomingEmail.setSubject(subject);

        String body = rawBody;

        // TRACKER — separate from threat, blocked but not scored
        if (trackerDefanger.containsTracker(body)) {
            body = trackerDefanger.defang(body);
            incomingEmail.setTrackerDetected(true);
            threatLevelService.recordTrackerDetected();
            System.out.println("[TRACKER BLOCKED] Tracking pixel blocked in email from " + from);
        } else {
            incomingEmail.setTrackerDetected(false);
        }

        // THREAT SCORE — only real security threats
        if (threatGrader.isSuspicious(from)) {
            incomingEmail.setThreatScore(1.0);
            threatLevelService.recordThreatGradeHit();
        }

        incomingEmail.setBody(body);
        incomingEmail.setState(Email.EmailState.INBOX);
        emailRepository.save(incomingEmail);

        mailFlowRuleEngine.evaluate(incomingEmail);

        System.out.println("SUCCESS: Incoming mail from " + from + " routed to " + owner.getEmailAddress());
    }
}
