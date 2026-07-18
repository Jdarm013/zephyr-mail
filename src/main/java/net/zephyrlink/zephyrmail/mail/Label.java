package net.zephyrlink.zephyrmail.mail;

import net.zephyrlink.zephyrmail.user.User;

import jakarta.persistence.*;

@Entity
@Table(name = "labels")
public class Label {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Every label belongs to a specific user. We don't want User A seeing User B's tags.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @Column(nullable = false)
    private String name;

    // Stores the UI color for the dashboard (e.g., "#FF0000" for a red "Urgent" tag)
    private String colorHex;

    // Default constructor for Spring Boot
    public Label() {}

    // ==========================================
    // GETTERS AND SETTERS
    // ==========================================

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getOwner() { return owner; }
    public void setOwner(User owner) { this.owner = owner; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getColorHex() { return colorHex; }
    public void setColorHex(String colorHex) { this.colorHex = colorHex; }
}