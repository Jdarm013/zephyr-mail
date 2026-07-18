package net.zephyrlink.zephyrmail.mail;

import net.zephyrlink.zephyrmail.user.User;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "emails")
public class Email {

    public enum EmailState {
        INBOX, SENT, DRAFTS, TRASH, SPAM, ADMIN_ALERT
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;

    // EAGER: ownership checks (e.g. InboxApiController, AttachmentApiController) read
    // owner.getEmailAddress() right after a plain repository fetch, outside any open
    // Hibernate session (spring.jpa.open-in-view=false) — LAZY here threw
    // LazyInitializationException on every such call.
    @JsonIgnore
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EmailState state;

    @Column(nullable = false)
    private String senderAddress;

    @Column(nullable = false)
    private String recipientAddress;

    private String ccAddresses;
    private String bccAddresses;
    private String subject;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String body;

    private Double threatScore;

    private String threadId;

    @Column(nullable = false)
    private boolean isDeleted = false;

    @Column(nullable = false)
    private boolean burnAfterRead = false;

    @Column(nullable = false)
    private boolean trackerDetected = false;

    @Column(nullable = false)
    private LocalDateTime receivedAt;

    // JsonIgnore: nothing currently reads labels via the JSON APIs, and serializing a lazy
    // collection outside an open session (open-in-view=false) throws the same way owner did.
    @JsonIgnore
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "email_labels",
            joinColumns = @JoinColumn(name = "email_id"),
            inverseJoinColumns = @JoinColumn(name = "label_id")
    )
    private List<Label> labels = new ArrayList<>();

    public Email() {
        this.receivedAt = LocalDateTime.now();
    }

    // GETTERS AND SETTERS
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getVersion() { return version; }

    public User getOwner() { return owner; }
    public void setOwner(User owner) { this.owner = owner; }

    public EmailState getState() { return state; }
    public void setState(EmailState state) { this.state = state; }

    public String getSenderAddress() { return senderAddress; }
    public void setSenderAddress(String senderAddress) { this.senderAddress = senderAddress; }

    public String getRecipientAddress() { return recipientAddress; }
    public void setRecipientAddress(String recipientAddress) { this.recipientAddress = recipientAddress; }

    public String getCcAddresses() { return ccAddresses; }
    public void setCcAddresses(String ccAddresses) { this.ccAddresses = ccAddresses; }

    public String getBccAddresses() { return bccAddresses; }
    public void setBccAddresses(String bccAddresses) { this.bccAddresses = bccAddresses; }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }

    public Double getThreatScore() { return threatScore; }
    public void setThreatScore(Double threatScore) { this.threatScore = threatScore; }

    public String getThreadId() { return threadId; }
    public void setThreadId(String threadId) { this.threadId = threadId; }

    public LocalDateTime getReceivedAt() { return receivedAt; }
    public void setReceivedAt(LocalDateTime receivedAt) { this.receivedAt = receivedAt; }

    public boolean isDeleted() { return isDeleted; }
    public void setDeleted(boolean deleted) { this.isDeleted = deleted; }

    public boolean isBurnAfterRead() { return burnAfterRead; }
    public void setBurnAfterRead(boolean burnAfterRead) { this.burnAfterRead = burnAfterRead; }

    public boolean isTrackerDetected() { return trackerDetected; }
    public void setTrackerDetected(boolean trackerDetected) { this.trackerDetected = trackerDetected; }

    public List<Label> getLabels() { return labels; }
    public void setLabels(List<Label> labels) { this.labels = labels; }
}