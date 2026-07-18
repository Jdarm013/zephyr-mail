package net.zephyrlink.zephyrmail.mail.web;

import net.zephyrlink.zephyrmail.mail.Attachment;
import net.zephyrlink.zephyrmail.user.User;
import net.zephyrlink.zephyrmail.mail.AttachmentService;
import net.zephyrlink.zephyrmail.user.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/vault")
public class VaultController {

    private final AttachmentService attachmentService;
    private final UserService userService;

    public VaultController(AttachmentService attachmentService,
                           UserService userService) {
        this.attachmentService = attachmentService;
        this.userService = userService;
    }

    @GetMapping
    public String vault(@AuthenticationPrincipal UserDetails userDetails,
                        Model model) {
        User user = userService.findActiveByEmail(userDetails.getUsername()).orElseThrow();
        List<Attachment> attachments = attachmentService.getVaultByOwner(user);
        model.addAttribute("attachments", attachments);
        model.addAttribute("user", user);
        return "vault";
    }
}