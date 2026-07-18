package net.zephyrlink.zephyrmail.security.defense;

import jakarta.annotation.PostConstruct;
import net.zephyrlink.zephyrmail.user.User;
import net.zephyrlink.zephyrmail.user.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.Optional;

@Service
public class EmergencyAccessService {

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;

    private String hashedToken;
    private boolean tokenConsumed = false;

    public EmergencyAccessService(PasswordEncoder passwordEncoder,
                                  UserRepository userRepository) {
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
    }

    // GENERATE ON STARTUP
    @PostConstruct
    public void generateToken() {
        byte[] tokenBytes = new byte[32];
        new SecureRandom().nextBytes(tokenBytes);
        String plainToken = Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
        this.hashedToken = passwordEncoder.encode(plainToken);
        this.tokenConsumed = false;

        System.err.println("\n");
        System.err.println("╔══════════════════════════════════════════════════════════════╗");
        System.err.println("║          ZEPHYRMAIL EMERGENCY ACCESS TOKEN                   ║");
        System.err.println("║  COPY THIS TOKEN AND STORE IT ON AN EXTERNAL DEVICE NOW.     ║");
        System.err.println("║  IT WILL NOT BE SHOWN AGAIN UNTIL NEXT RESTART.              ║");
        System.err.println("╠══════════════════════════════════════════════════════════════╣");
        System.err.println("║  TOKEN: " + plainToken + "  ║");
        System.err.println("╠══════════════════════════════════════════════════════════════╣");
        System.err.println("║  ENDPOINT: POST /emergency-access                            ║");
        System.err.println("║  PARAM:    token=<TOKEN>                                     ║");
        System.err.println("╚══════════════════════════════════════════════════════════════╝");
        System.err.println("\n");
    }

    public boolean verify(String token) {
        if (tokenConsumed || token == null || token.isBlank()) return false;
        return passwordEncoder.matches(token, hashedToken);
    }

    public void consume() {
        this.tokenConsumed = true;
        this.hashedToken = null;
    }

    public Optional<User> getAdminUser() {
        return userRepository.findAll().stream()
                .filter(u -> u.getAuthorities().stream()
                        .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")))
                .filter(u -> !u.isDeleted())
                .findFirst();
    }
}