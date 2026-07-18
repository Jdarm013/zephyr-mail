package net.zephyrlink.zephyrmail.mail.inbound;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.Iterator;

// Receives Resend's "email.received" webhook so this server can accept real internet mail
// without an internet-reachable port 25 — Resend receives on their infrastructure and hands
// the parsed metadata to us here. The webhook payload itself carries metadata only (Resend's
// docs are explicit: "Webhooks do not include the email body, headers, or attachments, only
// their metadata"), so the body is fetched with a follow-up call to Resend's Received Emails
// API using the email_id from the payload.
@RestController
@RequestMapping("/api/webhooks/resend")
public class ResendWebhookController {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();
    private static final Duration MAX_TIMESTAMP_SKEW = Duration.ofMinutes(5);

    private final InboundMailProcessor inboundMailProcessor;

    @Value("${resend.webhook.secret}")
    private String webhookSecret;

    @Value("${resend.api.key}")
    private String resendApiKey;

    public ResendWebhookController(InboundMailProcessor inboundMailProcessor) {
        this.inboundMailProcessor = inboundMailProcessor;
    }

    @PostMapping
    public ResponseEntity<?> receive(@RequestBody String rawBody,
                                     @RequestHeader("svix-id") String svixId,
                                     @RequestHeader("svix-timestamp") String svixTimestamp,
                                     @RequestHeader("svix-signature") String svixSignature) {

        if (!isValidSignature(svixId, svixTimestamp, svixSignature, rawBody)) {
            System.out.println("[RESEND WEBHOOK] Rejected — signature verification failed.");
            return ResponseEntity.status(401).build();
        }

        JsonNode event;
        try {
            event = MAPPER.readTree(rawBody);
        } catch (Exception e) {
            System.out.println("[RESEND WEBHOOK] Rejected — malformed JSON: " + e.getMessage());
            return ResponseEntity.badRequest().build();
        }

        String type = event.path("type").asText("");
        if (!"email.received".equals(type)) {
            // Resend sends every subscribed event type (sent, delivered, bounced, ...) to the
            // same webhook URL — anything that isn't inbound mail is simply not our concern.
            return ResponseEntity.ok().build();
        }

        JsonNode data = event.path("data");
        String emailId = data.path("email_id").asText(null);
        String from = data.path("from").asText(null);

        if (emailId == null || from == null || !data.path("to").isArray()) {
            System.out.println("[RESEND WEBHOOK] Rejected — missing required fields in payload.");
            return ResponseEntity.badRequest().build();
        }

        String subject = data.path("subject").asText("");
        String body = null;

        for (Iterator<JsonNode> it = data.path("to").elements(); it.hasNext(); ) {
            String recipient = it.next().asText();
            if (!inboundMailProcessor.accept(from, recipient)) {
                continue;
            }
            if (body == null) {
                body = fetchBody(emailId);
            }
            inboundMailProcessor.process(from, recipient, subject, body);
        }

        return ResponseEntity.ok().build();
    }

    private String fetchBody(String emailId) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.resend.com/emails/receiving/" + emailId))
                    .timeout(Duration.ofSeconds(10))
                    .header("Authorization", "Bearer " + resendApiKey)
                    .GET()
                    .build();

            HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
            JsonNode email = MAPPER.readTree(response.body());

            String html = email.path("html").asText(null);
            if (html != null) {
                return html;
            }
            String text = email.path("text").asText(null);
            return text != null ? text : "";

        } catch (Exception e) {
            System.err.println("[RESEND WEBHOOK] Failed to fetch email body for " + emailId + ": " + e.getMessage());
            return "";
        }
    }

    private boolean isValidSignature(String svixId, String svixTimestamp, String svixSignature, String rawBody) {
        try {
            long timestampSeconds = Long.parseLong(svixTimestamp);
            long ageSeconds = Math.abs(System.currentTimeMillis() / 1000 - timestampSeconds);
            if (ageSeconds > MAX_TIMESTAMP_SKEW.getSeconds()) {
                System.out.println("[RESEND WEBHOOK] Rejected — timestamp outside allowed window.");
                return false;
            }

            String secretBase64 = webhookSecret.startsWith("whsec_")
                    ? webhookSecret.substring("whsec_".length())
                    : webhookSecret;
            byte[] secretBytes = Base64.getDecoder().decode(secretBase64);

            String signedContent = svixId + "." + svixTimestamp + "." + rawBody;
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secretBytes, "HmacSHA256"));
            String expected = Base64.getEncoder().encodeToString(mac.doFinal(signedContent.getBytes(StandardCharsets.UTF_8)));

            // svix-signature can carry multiple space-separated "v1,<sig>" values (secret rotation)
            for (String candidate : svixSignature.split(" ")) {
                String[] parts = candidate.split(",", 2);
                if (parts.length == 2 && parts[1].equals(expected)) {
                    return true;
                }
            }
            return false;

        } catch (Exception e) {
            System.err.println("[RESEND WEBHOOK] Signature verification error: " + e.getMessage());
            return false;
        }
    }
}
