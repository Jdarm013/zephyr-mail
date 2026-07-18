package net.zephyrlink.zephyrmail.mail.inbound;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.mail.BodyPart;
import jakarta.mail.Multipart;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.subethamail.smtp.helper.SimpleMessageListener;
import org.subethamail.smtp.helper.SimpleMessageListenerAdapter;
import org.subethamail.smtp.server.SMTPServer;

import java.io.InputStream;
import java.util.Properties;

@Service
public class IncomingSmtpService implements SimpleMessageListener {

    private final InboundMailProcessor inboundMailProcessor;
    private final SmtpFingerprintInterceptor smtpFingerprintInterceptor;
    private SMTPServer smtpServer;

    @Value("${zephyr.mail.inbound.port:2525}")
    private int port;

    public IncomingSmtpService(InboundMailProcessor inboundMailProcessor,
                               SmtpFingerprintInterceptor smtpFingerprintInterceptor) {
        this.inboundMailProcessor = inboundMailProcessor;
        this.smtpFingerprintInterceptor = smtpFingerprintInterceptor;
    }

    @PostConstruct
    public void startServer() {
        smtpServer = SMTPServer.port(port)
                .messageHandlerFactory(new SimpleMessageListenerAdapter(this))
                .build();
        smtpServer.start();
        System.out.println("====== ZEPHYRMAIL SMTP SERVER LISTENING ON PORT " + port + " ======");
    }

    @PreDestroy
    public void stopServer() {
        if (smtpServer != null) {
            smtpServer.stop();
        }
    }

    @Override
    public boolean accept(String from, String recipient) {
        // Record connection timestamp for fingerprint timing check
        smtpFingerprintInterceptor.recordConnection(from);
        return inboundMailProcessor.accept(from, recipient);
    }

    @Override
    public void deliver(String from, String recipient, InputStream data) {
        // Reject mechanized SMTP clients — too fast = script, not a real mail server
        if (!smtpFingerprintInterceptor.isHumanBehavior(from)) {
            System.out.println("[SMTP FINGERPRINT] Rejected mechanized sender: " + from);
            return;
        }

        try {
            Session session = Session.getDefaultInstance(new Properties());
            MimeMessage mimeMessage = new MimeMessage(session, data);
            String body = extractBody(mimeMessage);
            inboundMailProcessor.process(from, recipient, mimeMessage.getSubject(), body);
        } catch (Exception e) {
            System.err.println("CRITICAL ERROR: Failed to process incoming mail: " + e.getMessage());
        }
    }

    private String extractBody(MimeMessage message) {
        try {
            Object content = message.getContent();
            if (content instanceof String) {
                return (String) content;
            }
            if (content instanceof Multipart) {
                Multipart multipart = (Multipart) content;
                StringBuilder result = new StringBuilder();
                for (int i = 0; i < multipart.getCount(); i++) {
                    BodyPart part = multipart.getBodyPart(i);
                    if (part.isMimeType("text/html")) {
                        return part.getContent().toString();
                    }
                    if (part.isMimeType("text/plain")) {
                        result.append(part.getContent().toString());
                    }
                }
                return result.toString();
            }
            return content.toString();
        } catch (Exception e) {
            return "[BODY PARSE ERROR: " + e.getMessage() + "]";
        }
    }
}
