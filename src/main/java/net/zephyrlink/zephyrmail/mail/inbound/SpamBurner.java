package net.zephyrlink.zephyrmail.mail.inbound;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "spam_burner")
public class SpamBurner {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String burnedAddress;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    public SpamBurner() {
        this.createdAt = LocalDateTime.now();
    }

    // GETTERS AND SETTERS
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getBurnedAddress() { return burnedAddress; }
    public void setBurnedAddress(String burnedAddress) { this.burnedAddress = burnedAddress; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}