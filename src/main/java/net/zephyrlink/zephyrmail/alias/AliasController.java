package net.zephyrlink.zephyrmail.alias;

import net.zephyrlink.zephyrmail.alias.ShadowRoute;
import net.zephyrlink.zephyrmail.user.User;
import net.zephyrlink.zephyrmail.alias.ShadowRouteService;
import net.zephyrlink.zephyrmail.user.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequestMapping("/aliases")
public class AliasController {

    private final ShadowRouteService shadowRouteService;
    private final UserService userService;

    public AliasController(ShadowRouteService shadowRouteService,
                           UserService userService) {
        this.shadowRouteService = shadowRouteService;
        this.userService = userService;
    }

    @GetMapping
    public String aliases(@AuthenticationPrincipal UserDetails userDetails,
                          Model model) {
        User user = userService.findActiveByEmail(userDetails.getUsername()).orElseThrow();
        List<ShadowRoute> routes = shadowRouteService.getByOwner(user);
        model.addAttribute("routes", routes);
        return "aliases";
    }

    @PostMapping("/create")
    public String create(@RequestParam String aliasAddress,
                         @RequestParam(required = false) String memo,
                         @AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.findActiveByEmail(userDetails.getUsername()).orElseThrow();
        shadowRouteService.create(user, aliasAddress, memo);
        return "redirect:/aliases";
    }

    @PostMapping("/nuke/{id}")
    public String nuke(@PathVariable Long id) {
        shadowRouteService.nuke(id);
        return "redirect:/aliases";
    }
}