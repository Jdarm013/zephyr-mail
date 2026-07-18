package net.zephyrlink.zephyrmail.security.auth;

import jakarta.servlet.http.HttpServletRequest;
import net.zephyrlink.zephyrmail.user.User;
import net.zephyrlink.zephyrmail.user.UserService;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import net.zephyrlink.zephyrmail.security.auth.LoginAttemptService;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserService userService;
    private final LoginAttemptService loginAttemptService;
    private final HttpServletRequest request;

    public CustomUserDetailsService(UserService userService,
                                    LoginAttemptService loginAttemptService,
                                    HttpServletRequest request) {
        this.userService = userService;
        this.loginAttemptService = loginAttemptService;
        this.request = request;
    }

    @Override
    public UserDetails loadUserByUsername(String emailAddress) throws UsernameNotFoundException {
        String ipAddress = request.getRemoteAddr();

        if (loginAttemptService.isIpBlocked(ipAddress)) {
            System.out.println("[SOC ALERT] BLOCKED IP ATTEMPTED BREACH: " + ipAddress);
            throw new LockedException("IP Address is temporarily locked out due to multiple failed attempts.");
        }

        if (loginAttemptService.isAccountBlocked(emailAddress)) {
            System.out.println("[SOC ALERT] ACCOUNT LOCKOUT TRIGGERED FOR: " + emailAddress);
            throw new LockedException("Account is temporarily locked out due to multiple failed attempts.");
        }

        User user = (User) userService.findActiveByEmail(emailAddress)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Account not found or access revoked: " + emailAddress));

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmailAddress())
                .password(user.getPassword())
                .roles(user.getRole())
                .build();
    }
}