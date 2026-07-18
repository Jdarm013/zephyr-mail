package net.zephyrlink.zephyrmail.mail.web;

import net.zephyrlink.zephyrmail.mail.Email;
import net.zephyrlink.zephyrmail.user.User;
import net.zephyrlink.zephyrmail.mail.EmailRepository;
import net.zephyrlink.zephyrmail.user.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/inbox")
public class InboxController {

    private final EmailRepository emailRepository;
    private final UserService userService;

    public InboxController(EmailRepository emailRepository,
                           UserService userService) {
        this.emailRepository = emailRepository;
        this.userService = userService;
    }

    @GetMapping
    public String inbox(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = userService.findActiveByEmail(userDetails.getUsername()).orElseThrow();
        List<Email> emails = emailRepository.findByOwnerAndStateAndIsDeletedFalseOrderByReceivedAtDesc(
                user, Email.EmailState.INBOX);
        model.addAttribute("emails", emails);
        model.addAttribute("user", user);
        return "user-inbox";
    }

    @GetMapping("/folder/{state}")
    public String folder(@PathVariable String state,
                         @AuthenticationPrincipal UserDetails userDetails,
                         Model model) {
        User user = userService.findActiveByEmail(userDetails.getUsername()).orElseThrow();
        List<Email> emails;
        if ("trash".equalsIgnoreCase(state)) {
            // Trash isn't a state — it's the isDeleted flag set by InboxApiController.trash()
            emails = emailRepository.findByOwnerAndIsDeletedTrueOrderByReceivedAtDesc(user);
        } else {
            Email.EmailState emailState = Email.EmailState.valueOf(state.toUpperCase());
            emails = emailRepository.findByOwnerAndStateAndIsDeletedFalseOrderByReceivedAtDesc(
                    user, emailState);
        }
        model.addAttribute("emails", emails);
        model.addAttribute("folder", state);
        model.addAttribute("user", user);
        return "user-inbox";
    }
}