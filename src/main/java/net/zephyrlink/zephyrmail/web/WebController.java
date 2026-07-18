package net.zephyrlink.zephyrmail.web;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.SecureRandom;
import java.util.HexFormat;

@Controller
public class WebController {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    @Value("${cloudflare.turnstile.sitekey}")
    private String turnstileSiteKey;

    @GetMapping("/login")
    public String loginPage(Model model, HttpSession session) {
        byte[] challengeBytes = new byte[16];
        SECURE_RANDOM.nextBytes(challengeBytes);
        String challenge = HexFormat.of().formatHex(challengeBytes);
        session.setAttribute("pow_challenge", challenge);
        model.addAttribute("powChallenge", challenge);
        model.addAttribute("turnstileSiteKey", turnstileSiteKey);
        return "login";
    }

    @GetMapping("/")
    public String dashboardRouter(Authentication authentication) {
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_ADMIN"));
        if (isAdmin) {
            return "admin-dashboard";
        } else {
            return "redirect:/inbox";
        }
    }
}