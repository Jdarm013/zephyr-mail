package net.zephyrlink.zephyrmail.alias;

import net.zephyrlink.zephyrmail.user.User;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "shadow_routes")
public class ShadowRoute {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @Column(nullable = false, unique = true)
    private String aliasAddress;

    private String memo;

    @Column(nullable = false)
    private boolean isActive = true;

    @Column(nullable = false)
    private int forwardCount = 0;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    public ShadowRoute() {
        this.createdAt = LocalDateTime.now();
    }

    // GETTERS AND SETTERS
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getVersion() { return version; }

    public User getOwner() { return owner; }
    public void setOwner(User owner) { this.owner = owner; }

    public String getAliasAddress() { return aliasAddress; }
    public void setAliasAddress(String aliasAddress) { this.aliasAddress = aliasAddress; }

    public String getMemo() { return memo; }
    public void setMemo(String memo) { this.memo = memo; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    public int getForwardCount() { return forwardCount; }
    public void setForwardCount(int forwardCount) { this.forwardCount = forwardCount; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}