package net.zephyrlink.zephyrmail.mail.outbound;

import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import net.zephyrlink.zephyrmail.mail.outbound.OutboxQueue;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class OutboundMailService {

    private final JavaMailSender mailSender;

    public OutboundMailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public boolean dispatch(OutboxQueue queuedEmail) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(new InternetAddress(queuedEmail.getOwner().getEmailAddress(), "ZephyrMail"));
            helper.setTo(queuedEmail.getRecipientAddress());
            helper.setSubject(queuedEmail.getSubject());
            helper.setText(queuedEmail.getBody(), true);

            if (queuedEmail.getCcAddresses() != null && !queuedEmail.getCcAddresses().isBlank()) {
                helper.setCc(queuedEmail.getCcAddresses());
            }

            if (queuedEmail.getBccAddresses() != null && !queuedEmail.getBccAddresses().isBlank()) {
                helper.setBcc(queuedEmail.getBccAddresses());
            }

            mailSender.send(message);
            System.out.println("OUTBOUND MAIL SUCCESS: Sent to " + queuedEmail.getRecipientAddress());
            return true;

        } catch (Exception e) {
            System.err.println("OUTBOUND MAIL ERROR: " + e.getMessage());
            return false;
        }
    }
}