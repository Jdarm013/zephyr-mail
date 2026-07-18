package net.zephyrlink.zephyrmail.security.auth;

import net.zephyrlink.zephyrmail.security.auth.LoginAttempt;
import net.zephyrlink.zephyrmail.security.auth.LoginAttemptRepository;
import net.zephyrlink.zephyrmail.security.threat.ThreatLevelService;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AbstractAuthenticationEvent;
import org.springframework.security.authentication.event.AbstractAuthenticationFailureEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class SecurityEventListener {

    private final LoginAttemptRepository loginAttemptRepository;
    private final ThreatLevelService threatLevelService;

    public SecurityEventListener(LoginAttemptRepository loginAttemptRepository,
                                 ThreatLevelService threatLevelService) {
        this.loginAttemptRepository = loginAttemptRepository;
        this.threatLevelService = threatLevelService;
    }

    // Tripwire 1: Triggers the millisecond a password matches
    @EventListener
    public void onSuccess(AuthenticationSuccessEvent event) {
        String email = event.getAuthentication().getName();
        String ipAddress = extractIpAddress(event);

        LoginAttempt attempt = new LoginAttempt(ipAddress, email, true);
        loginAttemptRepository.save(attempt);

        System.out.println("[SOC ALERT] SUCCESSFUL login from IP: " + ipAddress + " (Account: " + email + ")");
    }

    // Tripwire 2: Triggers the millisecond a password fails or user isn't found
    @EventListener
    public void onFailure(AbstractAuthenticationFailureEvent event) {
        String email = event.getAuthentication().getName();
        String ipAddress = extractIpAddress(event);

        LoginAttempt attempt = new LoginAttempt(ipAddress, email, false);
        loginAttemptRepository.save(attempt);

        // Count recent failures for this IP and update threat level
        int recentFailures = loginAttemptRepository
                .countByIpAddressAndIsSuccessfulFalseAndTimestampAfter(
                        ipAddress, LocalDateTime.now().minusMinutes(15));
        threatLevelService.recordFailedLogins(recentFailures);

        System.out.println("[SOC WARNING] FAILED login attempt from IP: " + ipAddress + " (Target: " + email + ")");
    }

    // Utility: Digs into the incoming internet packet to pull the exact IP Address
    private String extractIpAddress(AbstractAuthenticationEvent event) {
        Object details = event.getAuthentication().getDetails();
        if (details instanceof WebAuthenticationDetails) {
            return ((WebAuthenticationDetails) details).getRemoteAddress();
        }
        return "UNKNOWN_IP";
    }
}