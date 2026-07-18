package net.zephyrlink.zephyrmail.config;

import net.zephyrlink.zephyrmail.user.User;
import net.zephyrlink.zephyrmail.user.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.Base64;

@Component
public class DatabaseSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DatabaseSeeder(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        if (userRepository.count() == 0) {
            byte[] passwordBytes = new byte[18];
            new SecureRandom().nextBytes(passwordBytes);
            String generatedPassword = Base64.getUrlEncoder().withoutPadding().encodeToString(passwordBytes);

            User masterAdmin = new User();
            masterAdmin.setEmailAddress("admin@zephyrlink.net");
            masterAdmin.setPassword(passwordEncoder.encode(generatedPassword));
            masterAdmin.setRole("ADMIN");
            userRepository.save(masterAdmin);

            System.out.println("\n");
            System.out.println("╔══════════════════════════════════════════════════════════════╗");
            System.out.println("║          ZEPHYRMAIL MASTER ADMIN ACCOUNT CREATED             ║");
            System.out.println("║  COPY THIS PASSWORD AND STORE IT SOMEWHERE SAFE NOW.         ║");
            System.out.println("║  IT WILL NOT BE SHOWN AGAIN.                                 ║");
            System.out.println("╠══════════════════════════════════════════════════════════════╣");
            System.out.println("║  ACCOUNT:  admin@zephyrlink.net");
            System.out.println("║  PASSWORD: " + generatedPassword);
            System.out.println("╚══════════════════════════════════════════════════════════════╝");
            System.out.println("\n");
        } else {
            System.out.println("====== SYSTEM ALERT: DATABASE ALREADY POPULATED. SEEDER BYPASSED. ======");
        }
    }
}