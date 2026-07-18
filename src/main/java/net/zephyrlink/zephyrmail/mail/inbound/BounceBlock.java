package net.zephyrlink.zephyrmail.mail.inbound;

import net.zephyrlink.zephyrmail.user.User;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "bounce_blocks")
public class BounceBlock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @Column(nullable = false)
    private String blockedAddress;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    public BounceBlock() {
        this.createdAt = LocalDateTime.now();
    }

    // GETTERS AND SETTERS
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getVersion() { return version; }

    public User getOwner() { return owner; }
    public void setOwner(User owner) { this.owner = owner; }

    public String getBlockedAddress() { return blockedAddress; }
    public void setBlockedAddress(String blockedAddress) { this.blockedAddress = blockedAddress; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}