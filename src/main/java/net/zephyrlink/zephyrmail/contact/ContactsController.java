package net.zephyrlink.zephyrmail.contact;

import net.zephyrlink.zephyrmail.contact.Contact;
import net.zephyrlink.zephyrmail.user.User;
import net.zephyrlink.zephyrmail.contact.ContactService;
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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/contacts")
public class ContactsController {

    private final ContactService contactService;
    private final UserService userService;

    public ContactsController(ContactService contactService,
                              UserService userService) {
        this.contactService = contactService;
        this.userService = userService;
    }

    @GetMapping
    public String contacts(@AuthenticationPrincipal UserDetails userDetails,
                           Model model) {
        User user = userService.findActiveByEmail(userDetails.getUsername()).orElseThrow();
        List<Contact> contacts = contactService.getByOwner(user);
        model.addAttribute("contacts", contacts);
        return "contacts";
    }

    @PostMapping("/add")
    public String add(@RequestParam String emailAddress,
                      @RequestParam(required = false) String displayName,
                      @RequestParam(required = false) String notes,
                      @AuthenticationPrincipal UserDetails userDetails,
                      RedirectAttributes redirectAttributes) {
        User user = userService.findActiveByEmail(userDetails.getUsername()).orElseThrow();
        try {
            contactService.save(user, emailAddress, displayName, notes);
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/contacts";
    }

    @PostMapping("/update/{id}")
    public String update(@PathVariable Long id,
                         @RequestParam(required = false) String displayName,
                         @RequestParam(required = false) String notes,
                         @AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.findActiveByEmail(userDetails.getUsername()).orElseThrow();
        contactService.update(id, displayName, notes, user);
        return "redirect:/contacts";
    }

    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Long id,
                         @AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.findActiveByEmail(userDetails.getUsername()).orElseThrow();
        contactService.delete(id, user);
        return "redirect:/contacts";
    }

    @GetMapping("/search")
    public String search(@RequestParam String query,
                         @AuthenticationPrincipal UserDetails userDetails,
                         Model model) {
        User user = userService.findActiveByEmail(userDetails.getUsername()).orElseThrow();
        List<Contact> results = contactService.search(user, query);
        model.addAttribute("contacts", results);
        model.addAttribute("query", query);
        return "contacts";
    }
}