package net.zephyrlink.zephyrmail.mail.flow;

import net.zephyrlink.zephyrmail.user.User;

import jakarta.persistence.*;

@Entity
@Table(name = "mail_flow_rules")
public class MailFlowRule {

    public enum RuleAction {
        DELETE, TAG, FORWARD, MARK_SPAM
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String regexPattern;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RuleAction action;

    private String actionTarget;

    @Column(nullable = false)
    private boolean isActive = true;

    public MailFlowRule() {}

    // GETTERS AND SETTERS
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getVersion() { return version; }

    public User getOwner() { return owner; }
    public void setOwner(User owner) { this.owner = owner; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getRegexPattern() { return regexPattern; }
    public void setRegexPattern(String regexPattern) { this.regexPattern = regexPattern; }

    public RuleAction getAction() { return action; }
    public void setAction(RuleAction action) { this.action = action; }

    public String getActionTarget() { return actionTarget; }
    public void setActionTarget(String actionTarget) { this.actionTarget = actionTarget; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }
}