package net.zephyrlink.zephyrmail.mail.web;

import net.zephyrlink.zephyrmail.mail.Attachment;
import net.zephyrlink.zephyrmail.mail.Email;
import net.zephyrlink.zephyrmail.mail.EmailRepository;
import net.zephyrlink.zephyrmail.mail.AttachmentService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/attachments")
public class AttachmentApiController {

    private final AttachmentService attachmentService;
    private final EmailRepository emailRepository;

    public AttachmentApiController(AttachmentService attachmentService,
                                   EmailRepository emailRepository) {
        this.attachmentService = attachmentService;
        this.emailRepository = emailRepository;
    }

    @PostMapping("/upload/{emailId}")
    public ResponseEntity<?> upload(@PathVariable Long emailId,
                                    @RequestParam MultipartFile file,
                                    @AuthenticationPrincipal UserDetails userDetails) {
        Email email = emailRepository.findById(emailId).orElseThrow();
        if (!email.getOwner().getEmailAddress().equals(userDetails.getUsername())) {
            return ResponseEntity.status(403).build();
        }
        try {
            Attachment attachment = attachmentService.save(file, email);
            return ResponseEntity.ok(attachment.getId());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/email/{emailId}")
    public ResponseEntity<List<Attachment>> getByEmail(@PathVariable Long emailId,
                                                       @AuthenticationPrincipal UserDetails userDetails) {
        Email email = emailRepository.findById(emailId).orElseThrow();
        if (!email.getOwner().getEmailAddress().equals(userDetails.getUsername())) {
            return ResponseEntity.status(403).build();
        }
        return ResponseEntity.ok(attachmentService.getByEmail(email));
    }
}