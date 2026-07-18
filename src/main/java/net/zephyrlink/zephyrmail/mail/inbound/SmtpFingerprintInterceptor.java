package net.zephyrlink.zephyrmail.mail.inbound;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SmtpFingerprintInterceptor {

    // Thread-safe map to store the exact millisecond a specific IP connected
    private final Map<String, Long> connectionTimestamps = new ConcurrentHashMap<>();

    // 1. Called by our SMTP Service the millisecond an IP connects
    public void recordConnection(String ipAddress) {
        connectionTimestamps.put(ipAddress, System.currentTimeMillis());
    }

    // 2. Called right before we accept the actual email data
    public boolean isHumanBehavior(String ipAddress) {
        Long connectedAt = connectionTimestamps.get(ipAddress);

        // If they try to send data without properly initiating a connection, drop them.
        if (connectedAt == null) {
            return false;
        }

        long timeElapsed = System.currentTimeMillis() - connectedAt;

        // Clean up RAM so we don't cause a memory leak
        connectionTimestamps.remove(ipAddress);

        // 3. The Fingerprint Check
        // If the entire conversation from HELO to data transfer happened in under 500ms,
        // it's a script blasting commands, not a real mail server.
        if (timeElapsed < 500) {
            System.out.println("🛡️ PERIMETER DEFENSE: Dropped SMTP connection from " + ipAddress + " - Mechanized timing detected (" + timeElapsed + "ms).");
            return false;
        }

        return true;
    }
}