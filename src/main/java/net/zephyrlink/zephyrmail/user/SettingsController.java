package net.zephyrlink.zephyrmail.user;

import net.zephyrlink.zephyrmail.user.User;
import net.zephyrlink.zephyrmail.user.PasswordService;
import net.zephyrlink.zephyrmail.user.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/settings")
public class SettingsController {

    private final UserService userService;
    private final PasswordService passwordService;

    public SettingsController(UserService userService,
                              PasswordService passwordService) {
        this.userService = userService;
        this.passwordService = passwordService;
    }

    @GetMapping
    public String settingsPage(@AuthenticationPrincipal UserDetails userDetails,
                               Model model) {
        User user = userService.findActiveByEmail(userDetails.getUsername()).orElseThrow();
        model.addAttribute("user", user);
        return "settings";
    }

    @PostMapping("/password")
    public String changePassword(@RequestParam String currentPassword,
                                 @RequestParam String newPassword,
                                 @RequestParam String confirmPassword,
                                 @AuthenticationPrincipal UserDetails userDetails,
                                 RedirectAttributes redirectAttributes) {

        User user = userService.findActiveByEmail(userDetails.getUsername()).orElseThrow();
        String error = passwordService.changePassword(user, currentPassword, newPassword, confirmPassword);

        if (error != null) {
            redirectAttributes.addFlashAttribute("error", error);
        } else {
            redirectAttributes.addFlashAttribute("success", "Password updated successfully.");
        }

        return "redirect:/settings";
    }
}