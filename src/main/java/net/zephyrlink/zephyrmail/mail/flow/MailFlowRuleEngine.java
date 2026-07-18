package net.zephyrlink.zephyrmail.mail.flow;

import net.zephyrlink.zephyrmail.mail.outbound.OutboundMailService;

import net.zephyrlink.zephyrmail.mail.Email;
import net.zephyrlink.zephyrmail.mail.Label;
import net.zephyrlink.zephyrmail.mail.flow.MailFlowRule;
import net.zephyrlink.zephyrmail.mail.outbound.OutboxQueue;
import net.zephyrlink.zephyrmail.mail.EmailRepository;
import net.zephyrlink.zephyrmail.mail.LabelRepository;
import net.zephyrlink.zephyrmail.mail.flow.MailFlowRuleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class MailFlowRuleEngine {

    private final MailFlowRuleRepository ruleRepository;
    private final EmailRepository emailRepository;
    private final LabelRepository labelRepository;
    private final OutboundMailService outboundMailService;

    public MailFlowRuleEngine(MailFlowRuleRepository ruleRepository,
                              EmailRepository emailRepository,
                              LabelRepository labelRepository,
                              OutboundMailService outboundMailService) {
        this.ruleRepository = ruleRepository;
        this.emailRepository = emailRepository;
        this.labelRepository = labelRepository;
        this.outboundMailService = outboundMailService;
    }

    @Transactional
    public void evaluate(Email email) {
        List<MailFlowRule> rules = ruleRepository.findByOwnerAndIsActiveTrue(email.getOwner());

        for (MailFlowRule rule : rules) {
            if (matches(rule.getRegexPattern(), email)) {
                applyAction(rule, email);
            }
        }
    }

    private boolean matches(String regexPattern, Email email) {
        try {
            Pattern pattern = Pattern.compile(regexPattern, Pattern.CASE_INSENSITIVE);
            String target = email.getSenderAddress() + " " +
                    email.getSubject() + " " +
                    email.getBody();
            Matcher matcher = pattern.matcher(target);
            return matcher.find();
        } catch (Exception e) {
            return false;
        }
    }

    private void applyAction(MailFlowRule rule, Email email) {
        switch (rule.getAction()) {
            case DELETE:
                email.setDeleted(true);
                emailRepository.save(email);
                break;
            case MARK_SPAM:
                email.setState(Email.EmailState.SPAM);
                emailRepository.save(email);
                break;
            case TAG:
                Label label = labelRepository
                        .findByOwnerAndName(email.getOwner(), rule.getActionTarget())
                        .orElseGet(() -> {
                            Label created = new Label();
                            created.setOwner(email.getOwner());
                            created.setName(rule.getActionTarget());
                            return labelRepository.save(created);
                        });
                if (!email.getLabels().contains(label)) {
                    email.getLabels().add(label);
                    emailRepository.save(email);
                }
                break;
            case FORWARD:
                OutboxQueue fwd = new OutboxQueue();
                fwd.setOwner(email.getOwner());
                fwd.setRecipientAddress(rule.getActionTarget());
                fwd.setSubject("Fwd: " + email.getSubject());
                fwd.setBody(email.getBody());
                fwd.setScheduledAt(LocalDateTime.now());
                fwd.setStatus(OutboxQueue.QueueStatus.PENDING);
                outboundMailService.dispatch(fwd);
                break;
        }
    }
}