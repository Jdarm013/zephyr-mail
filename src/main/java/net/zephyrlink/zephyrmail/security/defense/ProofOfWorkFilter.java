package net.zephyrlink.zephyrmail.security.defense;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

@Component
public class ProofOfWorkFilter extends OncePerRequestFilter {

    private static final String DIFFICULTY_PREFIX = "0000";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();

        if ("POST".equalsIgnoreCase(request.getMethod()) && path.equals("/login")) {

            String powHash = request.getHeader("X-PoW-Hash");
            String powNonce = request.getHeader("X-PoW-Nonce");

            // MISSING HEADERS
            if (powHash == null || powNonce == null || powHash.isBlank() || powNonce.isBlank()) {
                System.out.println("[PERIMETER DEFENSE] Blocked — missing PoW headers.");
                response.sendError(HttpServletResponse.SC_FORBIDDEN,
                        "Missing cryptographic Proof of Work.");
                return;
            }

            // RETRIEVE CHALLENGE FROM SESSION
            HttpSession session = request.getSession(false);
            if (session == null) {
                System.out.println("[PERIMETER DEFENSE] Blocked — no session for PoW challenge.");
                response.sendError(HttpServletResponse.SC_FORBIDDEN,
                        "Invalid session state.");
                return;
            }

            String challenge = (String) session.getAttribute("pow_challenge");
            if (challenge == null) {
                System.out.println("[PERIMETER DEFENSE] Blocked — PoW challenge missing or already used.");
                response.sendError(HttpServletResponse.SC_FORBIDDEN,
                        "Proof of Work challenge expired or replayed.");
                return;
            }

            // INVALIDATE CHALLENGE — single use, prevents replay
            session.removeAttribute("pow_challenge");

            // RE-HASH SERVER SIDE
            String input = challenge + powNonce;
            String serverHash = sha256Hex(input);

            if (serverHash == null) {
                System.err.println("[PERIMETER DEFENSE] SHA-256 unavailable.");
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                        "Security subsystem error.");
                return;
            }

            // VERIFY HASH MATCHES AND MEETS DIFFICULTY
            if (!serverHash.startsWith(DIFFICULTY_PREFIX) || !serverHash.equals(powHash)) {
                System.out.println("[PERIMETER DEFENSE] Blocked — PoW verification failed.");
                response.sendError(HttpServletResponse.SC_FORBIDDEN,
                        "Invalid Proof of Work computation.");
                return;
            }

            System.out.println("[PERIMETER DEFENSE] PoW verified. CPU check passed.");
        }

        filterChain.doFilter(request, response);
    }

    private String sha256Hex(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(input.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            return null;
        }
    }
}