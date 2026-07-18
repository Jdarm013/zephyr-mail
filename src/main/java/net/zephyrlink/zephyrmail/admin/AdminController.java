package net.zephyrlink.zephyrmail.admin;

import net.zephyrlink.zephyrmail.mail.Email;
import net.zephyrlink.zephyrmail.security.auth.LoginAttempt;
import net.zephyrlink.zephyrmail.user.User;
import net.zephyrlink.zephyrmail.mail.EmailRepository;
import net.zephyrlink.zephyrmail.security.auth.LoginAttemptRepository;
import net.zephyrlink.zephyrmail.user.UserRepository;
import net.zephyrlink.zephyrmail.user.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import java.util.List;
import java.util.regex.Pattern;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final UserRepository userRepository;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final LoginAttemptRepository loginAttemptRepository;
    private final EmailRepository emailRepository;

    private static final String PASSWORD_PATTERN = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{8,}$";

    public AdminController(UserRepository userRepository,
                           UserService userService,
                           PasswordEncoder passwordEncoder,
                           LoginAttemptRepository loginAttemptRepository,
                           EmailRepository emailRepository) {
        this.userRepository = userRepository;
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.loginAttemptRepository = loginAttemptRepository;
        this.emailRepository = emailRepository;
    }

    @GetMapping({"/users"})
    public String viewUserManagement(Model model) {
        List<User> allUsers = userService.findAllActive();
        model.addAttribute("users", allUsers);
        return "admin-users";
    }

    @PostMapping({"/users/create"})
    public String createUser(@RequestParam String emailAddress,
                             @RequestParam String password,
                             @RequestParam String role) {
        if (userRepository.findByEmailAddress(emailAddress).isPresent()) {
            return "redirect:/admin/users?error=duplicate";
        }
        if (!Pattern.compile(PASSWORD_PATTERN).matcher(password).matches()) {
            return "redirect:/admin/users?error=weak_password";
        }
        User newUser = new User();
        newUser.setEmailAddress(emailAddress);
        newUser.setPassword(passwordEncoder.encode(password));
        newUser.setRole(role);
        newUser.setDeleted(false);
        userRepository.save(newUser);
        return "redirect:/admin/users?success=created";
    }

    @PostMapping({"/users/delete"})
    public String deleteUser(@RequestParam Long id) {
        User user = userRepository.findById(id).orElseThrow();
        if (user.getEmailAddress().equals("admin@zephyrlink.net")) {
            return "redirect:/admin/users?error=admin_delete";
        }
        user.setDeleted(true);
        userRepository.save(user);
        // Evict cache so deleted user is not accepted for incoming mail
        userService.evictUser(user.getEmailAddress());
        return "redirect:/admin/users?success=deleted";
    }

    @GetMapping({"/threats"})
    public String viewThreatLogs(Model model, @RequestParam(defaultValue = "0") int page) {
        Page<LoginAttempt> threatPage = loginAttemptRepository.findAll(
                PageRequest.of(page, 20, Sort.by("timestamp").descending())
        );
        model.addAttribute("threats", threatPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", threatPage.getTotalPages());
        return "admin-threats";
    }

    @GetMapping("/inbox")
    public String viewAdminInbox(Model model) {
        List<Email> alerts = emailRepository.findByState(Email.EmailState.ADMIN_ALERT);
        model.addAttribute("emails", alerts);
        return "admin-inbox";
    }
}