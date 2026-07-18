package net.zephyrlink.zephyrmail.admin;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "system_themes")
public class SystemTheme {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String themeName;

    // The primary accent color for buttons and highlights (e.g., "#00FF00" for a hacker terminal vibe)
    @Column(nullable = false)
    private String primaryColorHex;

    // The background/navbar color
    @Column(nullable = false)
    private String secondaryColorHex;

    // Path or URL to the company logo
    private String logoUrl;

    // The absolute active state. Only ONE theme should ever have this set to true.
    @Column(nullable = false)
    private boolean isActive = false;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    public SystemTheme() {
        this.createdAt = LocalDateTime.now();
    }

    // ==========================================
    // GETTERS AND SETTERS
    // ==========================================

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getThemeName() { return themeName; }
    public void setThemeName(String themeName) { this.themeName = themeName; }

    public String getPrimaryColorHex() { return primaryColorHex; }
    public void setPrimaryColorHex(String primaryColorHex) { this.primaryColorHex = primaryColorHex; }

    public String getSecondaryColorHex() { return secondaryColorHex; }
    public void setSecondaryColorHex(String secondaryColorHex) { this.secondaryColorHex = secondaryColorHex; }

    public String getLogoUrl() { return logoUrl; }
    public void setLogoUrl(String logoUrl) { this.logoUrl = logoUrl; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}