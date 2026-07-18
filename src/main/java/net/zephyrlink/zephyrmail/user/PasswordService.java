package net.zephyrlink.zephyrmail.user;

import net.zephyrlink.zephyrmail.user.PasswordHistory;
import net.zephyrlink.zephyrmail.user.User;
import net.zephyrlink.zephyrmail.user.PasswordHistoryRepository;
import net.zephyrlink.zephyrmail.user.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.regex.Pattern;

@Service
public class PasswordService {

    private static final int HISTORY_DEPTH = 5;
    private static final String PASSWORD_PATTERN =
            "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!_])(?=\\S+$).{8,}$";

    private final UserRepository userRepository;
    private final PasswordHistoryRepository passwordHistoryRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserService userService;

    public PasswordService(UserRepository userRepository,
                           PasswordHistoryRepository passwordHistoryRepository,
                           PasswordEncoder passwordEncoder,
                           UserService userService) {
        this.userRepository = userRepository;
        this.passwordHistoryRepository = passwordHistoryRepository;
        this.passwordEncoder = passwordEncoder;
        this.userService = userService;
    }

    // Returns null on success, error message on failure
    public String changePassword(User user, String currentPassword,
                                 String newPassword, String confirmPassword) {

        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            return "Current password is incorrect.";
        }

        if (!newPassword.equals(confirmPassword)) {
            return "New passwords do not match.";
        }

        if (!Pattern.compile(PASSWORD_PATTERN).matcher(newPassword).matches()) {
            return "Password must be at least 8 characters and include uppercase, lowercase, number, and special character.";
        }

        if (passwordEncoder.matches(newPassword, user.getPassword())) {
            return "New password cannot be the same as your current password.";
        }

        List<PasswordHistory> history = passwordHistoryRepository
                .findByOwnerOrderByCreatedAtDesc(user);

        for (PasswordHistory entry : history) {
            if (passwordEncoder.matches(newPassword, entry.getHashedPassword())) {
                return "Password was used recently. Choose a different password.";
            }
        }

        // Save current password to history before changing
        PasswordHistory historyEntry = new PasswordHistory();
        historyEntry.setOwner(user);
        historyEntry.setHashedPassword(user.getPassword());
        passwordHistoryRepository.save(historyEntry);

        // Trim history to HISTORY_DEPTH
        if (history.size() >= HISTORY_DEPTH) {
            List<PasswordHistory> toDelete = history.subList(HISTORY_DEPTH - 1, history.size());
            passwordHistoryRepository.deleteAll(toDelete);
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // Evict cache so next login uses the new password hash
        userService.evictUser(user.getEmailAddress());

        return null;
    }
}