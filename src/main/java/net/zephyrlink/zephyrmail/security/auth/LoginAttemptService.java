package net.zephyrlink.zephyrmail.security.auth;

import net.zephyrlink.zephyrmail.security.auth.LoginAttempt;
import net.zephyrlink.zephyrmail.security.auth.LoginAttemptRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class LoginAttemptService {

    private final int MAX_ATTEMPTS = 5;
    private final int LOCKOUT_MINUTES = 15;

    private final LoginAttemptRepository loginAttemptRepository;

    public LoginAttemptService(LoginAttemptRepository loginAttemptRepository) {
        this.loginAttemptRepository = loginAttemptRepository;
    }

    // --- 1. THE IP TRACKER ---
    // Deliberately does NOT reset on a successful login the way the account tracker does:
    // this check is IP-wide across all accounts, so an attacker who owns one working account
    // could otherwise keep logging into it to reset the failure window while brute-forcing a
    // different account from the same IP.
    public int getRecentFailuresByIp(String ipAddress) {
        LocalDateTime timeLimit = LocalDateTime.now().minusMinutes(LOCKOUT_MINUTES);
        return loginAttemptRepository.countByIpAddressAndIsSuccessfulFalseAndTimestampAfter(ipAddress, timeLimit);
    }

    public boolean isIpBlocked(String ipAddress) {
        return getRecentFailuresByIp(ipAddress) >= MAX_ATTEMPTS;
    }

    public int getIpAttemptsLeft(String ipAddress) {
        return MAX_ATTEMPTS - getRecentFailuresByIp(ipAddress);
    }

    // --- 2. NEW: THE ACCOUNT TRACKER ---
    public int getRecentFailuresByAccount(String emailAddress) {
        LocalDateTime timeLimit = LocalDateTime.now().minusMinutes(LOCKOUT_MINUTES);
        Optional<LoginAttempt> lastSuccess = loginAttemptRepository.findTopByEmailAddressAndIsSuccessfulTrueOrderByTimestampDesc(emailAddress);

        if (lastSuccess.isPresent() && lastSuccess.get().getTimestamp().isAfter(timeLimit)) {
            timeLimit = lastSuccess.get().getTimestamp();
        }
        return loginAttemptRepository.countByEmailAddressAndIsSuccessfulFalseAndTimestampAfter(emailAddress, timeLimit);
    }

    public boolean isAccountBlocked(String emailAddress) {
        return getRecentFailuresByAccount(emailAddress) >= MAX_ATTEMPTS;
    }
}