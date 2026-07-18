package net.zephyrlink.zephyrmail.security.auth;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import net.zephyrlink.zephyrmail.security.auth.LoginAttemptService;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomAuthenticationFailureHandler implements AuthenticationFailureHandler {

    private final LoginAttemptService loginAttemptService;

    public CustomAuthenticationFailureHandler(LoginAttemptService loginAttemptService) {
        this.loginAttemptService = loginAttemptService;
    }

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {
        String ipAddress = request.getRemoteAddr();

        if (exception instanceof LockedException) {
            response.sendRedirect("/login?locked=true");
        } else {
            // Updated to use the new specific IP method name
            int attemptsLeft = loginAttemptService.getIpAttemptsLeft(ipAddress);

            if (attemptsLeft <= 0) {
                response.sendRedirect("/login?locked=true");
            } else {
                response.sendRedirect("/login?error=true&attempts=" + attemptsLeft);
            }
        }
    }
}