package net.zephyrlink.zephyrmail.admin;

import net.zephyrlink.zephyrmail.security.defense.PanicButtonService;
import net.zephyrlink.zephyrmail.security.threat.ThreatLevelService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminApiController {

    private final PanicButtonService panicButtonService;
    private final ThreatLevelService threatLevelService;

    public AdminApiController(PanicButtonService panicButtonService,
                              ThreatLevelService threatLevelService) {
        this.panicButtonService = panicButtonService;
        this.threatLevelService = threatLevelService;
    }

    @PostMapping("/panic")
    public ResponseEntity<?> panic(@AuthenticationPrincipal UserDetails userDetails) {
        panicButtonService.trigger(userDetails.getUsername());
        return ResponseEntity.ok("All sessions invalidated. Manual restart required.");
    }

    @GetMapping("/defcon")
    public ResponseEntity<?> defconLevel() {
        return ResponseEntity.ok(threatLevelService.getCurrentLevel());
    }

    @PostMapping("/defcon/reset")
    public ResponseEntity<?> resetDefcon() {
        threatLevelService.reset();
        return ResponseEntity.ok("Threat level reset to DEFCON ONE.");
    }
}