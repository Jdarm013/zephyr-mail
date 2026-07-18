package net.zephyrlink.zephyrmail.mail;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "attachments")
public class Attachment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;

    // JsonIgnore: avoids re-serializing the whole parent Email (and the same lazy-loading
    // crash as Email.owner/labels once had) when a list of Attachments is returned as JSON.
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "email_id", nullable = false)
    private Email email;

    @Column(nullable = false)
    private String fileName;

    @Column(nullable = false)
    private String mimeType;

    @Column(nullable = false)
    private Long fileSize;

    @Column(nullable = false)
    private boolean flaggedUnchecked = false;

    @Column(nullable = false)
    private LocalDateTime uploadedAt;

    public Attachment() {
        this.uploadedAt = LocalDateTime.now();
    }

    // GETTERS AND SETTERS
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getVersion() { return version; }

    public Email getEmail() { return email; }
    public void setEmail(Email email) { this.email = email; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public String getMimeType() { return mimeType; }
    public void setMimeType(String mimeType) { this.mimeType = mimeType; }

    public Long getFileSize() { return fileSize; }
    public void setFileSize(Long fileSize) { this.fileSize = fileSize; }

    public boolean isFlaggedUnchecked() { return flaggedUnchecked; }
    public void setFlaggedUnchecked(boolean flaggedUnchecked) { this.flaggedUnchecked = flaggedUnchecked; }

    public LocalDateTime getUploadedAt() { return uploadedAt; }
    public void setUploadedAt(LocalDateTime uploadedAt) { this.uploadedAt = uploadedAt; }
}