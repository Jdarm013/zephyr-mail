package net.zephyrlink.zephyrmail.security.defense;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import net.zephyrlink.zephyrmail.security.audit.AuditLog;
import net.zephyrlink.zephyrmail.user.User;
import net.zephyrlink.zephyrmail.security.audit.AuditLogRepository;
import net.zephyrlink.zephyrmail.security.defense.EmergencyAccessService;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
public class EmergencyAccessController {

    private final EmergencyAccessService emergencyAccessService;
    private final AuditLogRepository auditLogRepository;

    public EmergencyAccessController(EmergencyAccessService emergencyAccessService,
                                     AuditLogRepository auditLogRepository) {
        this.emergencyAccessService = emergencyAccessService;
        this.auditLogRepository = auditLogRepository;
    }

    @PostMapping("/emergency-access")
    public String emergencyAccess(@RequestParam String token,
                                  HttpServletRequest request,
                                  RedirectAttributes redirectAttributes) {

        if (!emergencyAccessService.verify(token)) {
            AuditLog log = new AuditLog();
            log.setAction("EMERGENCY_ACCESS_FAILED");
            log.setActorEmail("unknown");
            log.setSeverity(AuditLog.Severity.BREACH_ATTEMPT);
            log.setDetails("Invalid emergency access token submitted from: "
                    + request.getRemoteAddr());
            auditLogRepository.save(log);

            System.err.println("[EMERGENCY ACCESS] FAILED attempt from: "
                    + request.getRemoteAddr());
            redirectAttributes.addFlashAttribute("error", "Invalid emergency access token.");
            return "redirect:/login?error";
        }

        Optional<User> adminOpt = emergencyAccessService.getAdminUser();
        if (adminOpt.isEmpty()) {
            System.err.println("[EMERGENCY ACCESS] No admin user found.");
            return "redirect:/login?error";
        }

        User admin = adminOpt.get();
        emergencyAccessService.consume();

        // ESTABLISH SESSION
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(
                        admin, null, admin.getAuthorities());

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);

        HttpSession session = request.getSession(true);
        session.setAttribute(
                HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, context);

        AuditLog log = new AuditLog();
        log.setAction("EMERGENCY_ACCESS_USED");
        log.setActorEmail(admin.getEmailAddress());
        log.setSeverity(AuditLog.Severity.BREACH_ATTEMPT);
        log.setDetails("Emergency access token used from: " + request.getRemoteAddr());
        auditLogRepository.save(log);

        System.err.println("[EMERGENCY ACCESS] Token used successfully from: "
                + request.getRemoteAddr());

        return "redirect:/";
    }
}