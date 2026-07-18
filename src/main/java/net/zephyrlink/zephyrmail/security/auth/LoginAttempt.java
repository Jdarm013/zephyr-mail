package net.zephyrlink.zephyrmail.security.auth;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class LoginAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String ipAddress;
    private String emailAddress; // Standardized to match User.java
    private boolean isSuccessful;
    private LocalDateTime timestamp;

    public LoginAttempt() {
        this.timestamp = LocalDateTime.now();
    }

    // The exact constructor used by your SecurityEventListener
    public LoginAttempt(String ipAddress, String emailAddress, boolean isSuccessful) {
        this.ipAddress = ipAddress;
        this.emailAddress = emailAddress;
        this.isSuccessful = isSuccessful;
        this.timestamp = LocalDateTime.now();
    }

    // --- GETTERS & SETTERS (This is what Thymeleaf needs to read the data!) ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }

    public String getEmailAddress() { return emailAddress; }
    public void setEmailAddress(String emailAddress) { this.emailAddress = emailAddress; }

    public boolean getIsSuccessful() { return isSuccessful; }
    public void setIsSuccessful(boolean successful) { isSuccessful = successful; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}