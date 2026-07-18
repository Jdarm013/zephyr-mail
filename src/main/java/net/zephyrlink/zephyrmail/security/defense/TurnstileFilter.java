package net.zephyrlink.zephyrmail.security.defense;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

@Component
public class TurnstileFilter extends OncePerRequestFilter {

    private static final String SITEVERIFY_URL = "https://challenges.cloudflare.com/turnstile/v0/siteverify";
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    @Value("${cloudflare.turnstile.secret}")
    private String turnstileSecret;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();

        if ("POST".equalsIgnoreCase(request.getMethod())
                && (path.equals("/login") || path.equals("/register"))) {

            String turnstileToken = request.getHeader("X-Turnstile-Token");

            if (turnstileToken == null || turnstileToken.isBlank()) {
                System.out.println("[PERIMETER DEFENSE] Blocked — missing Turnstile token.");
                response.sendError(HttpServletResponse.SC_FORBIDDEN,
                        "Missing Cloudflare verification token.");
                return;
            }

            if (!verifyWithCloudflare(turnstileToken)) {
                System.out.println("[PERIMETER DEFENSE] Blocked — invalid Turnstile token.");
                response.sendError(HttpServletResponse.SC_FORBIDDEN,
                        "Cloudflare verification failed.");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private boolean verifyWithCloudflare(String token) {
        try {
            String formBody = "secret=" + URLEncoder.encode(turnstileSecret, StandardCharsets.UTF_8)
                    + "&response=" + URLEncoder.encode(token, StandardCharsets.UTF_8);

            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(SITEVERIFY_URL))
                    .timeout(Duration.ofSeconds(5))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(formBody))
                    .build();

            HttpResponse<String> httpResponse = HTTP_CLIENT.send(httpRequest,
                    HttpResponse.BodyHandlers.ofString());

            JsonNode json = MAPPER.readTree(httpResponse.body());
            boolean success = json.path("success").asBoolean(false);

            if (!success) {
                System.out.println("[TURNSTILE] Verification failed: " + json.path("error-codes"));
            }

            return success;

        } catch (Exception e) {
            System.err.println("[TURNSTILE] Verification error: " + e.getMessage());
            return false;
        }
    }
}