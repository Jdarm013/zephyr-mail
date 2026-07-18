package net.zephyrlink.zephyrmail.mail.web;

import net.zephyrlink.zephyrmail.user.User;
import net.zephyrlink.zephyrmail.security.threat.DataLeakRadar;
import net.zephyrlink.zephyrmail.mail.outbound.OutboxQueueService;
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

import java.util.List;

@Controller
@RequestMapping("/compose")
public class ComposeController {

    private final OutboxQueueService outboxQueueService;
    private final UserService userService;
    private final DataLeakRadar dataLeakRadar;

    public ComposeController(OutboxQueueService outboxQueueService,
                             UserService userService,
                             DataLeakRadar dataLeakRadar) {
        this.outboxQueueService = outboxQueueService;
        this.userService = userService;
        this.dataLeakRadar = dataLeakRadar;
    }

    @GetMapping
    public String composePage(Model model) {
        return "compose";
    }

    @PostMapping("/send")
    public String send(@RequestParam String to,
                       @RequestParam(required = false) String cc,
                       @RequestParam(required = false) String bcc,
                       @RequestParam String subject,
                       @RequestParam String body,
                       @AuthenticationPrincipal UserDetails userDetails,
                       RedirectAttributes redirectAttributes) {

        List<String> leaks = dataLeakRadar.scan(body);
        if (!leaks.isEmpty()) {
            redirectAttributes.addFlashAttribute("leakWarnings", leaks);
            redirectAttributes.addFlashAttribute("leakWarningActive", true);
            redirectAttributes.addFlashAttribute("draft_to", to);
            redirectAttributes.addFlashAttribute("draft_subject", subject);
            redirectAttributes.addFlashAttribute("draft_body", body);
            return "redirect:/compose";
        }

        User user = userService.findActiveByEmail(userDetails.getUsername()).orElseThrow();
        outboxQueueService.queueWithUndoWindow(user, to, cc, bcc, subject, body);
        return "redirect:/inbox";
    }
}