package net.zephyrlink.zephyrmail.admin;

import net.zephyrlink.zephyrmail.security.threat.ChaosEngineService;
import net.zephyrlink.zephyrmail.security.threat.TracerouteService;
import net.zephyrlink.zephyrmail.security.threat.ThreatLevelService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@Controller
@RequestMapping("/soc")
@PreAuthorize("hasRole('ADMIN')")
public class SocController {

    private final ThreatLevelService threatLevelService;
    private final ChaosEngineService chaosEngineService;
    private final TracerouteService tracerouteService;

    public SocController(ThreatLevelService threatLevelService,
                         ChaosEngineService chaosEngineService,
                         TracerouteService tracerouteService) {
        this.threatLevelService = threatLevelService;
        this.chaosEngineService = chaosEngineService;
        this.tracerouteService = tracerouteService;
    }

    @GetMapping
    public String dashboard(Model model) {
        model.addAttribute("defconLevel", threatLevelService.getCurrentLevel());
        return "soc-dashboard";
    }

    @PostMapping("/investigate")
    public String investigate(@RequestParam String senderEmail,
                              @RequestParam String senderIp,
                              Model model) {
        Map<String, Object> report = chaosEngineService.investigate(senderEmail, senderIp);
        model.addAttribute("report", report);
        model.addAttribute("defconLevel", threatLevelService.getCurrentLevel());
        return "soc-dashboard";
    }

    @PostMapping("/traceroute")
    public String traceroute(@RequestParam String ipAddress, Model model) {
        model.addAttribute("hops", tracerouteService.trace(ipAddress));
        model.addAttribute("defconLevel", threatLevelService.getCurrentLevel());
        return "soc-dashboard";
    }
}