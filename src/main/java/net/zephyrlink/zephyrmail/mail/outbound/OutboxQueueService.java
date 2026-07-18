package net.zephyrlink.zephyrmail.mail.outbound;

import net.zephyrlink.zephyrmail.mail.Email;
import net.zephyrlink.zephyrmail.mail.outbound.OutboxQueue;
import net.zephyrlink.zephyrmail.user.User;
import net.zephyrlink.zephyrmail.mail.EmailRepository;
import net.zephyrlink.zephyrmail.mail.outbound.OutboxQueueRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class OutboxQueueService {

    private final OutboxQueueRepository outboxQueueRepository;
    private final OutboundMailService outboundMailService;
    private final EmailRepository emailRepository;

    public OutboxQueueService(OutboxQueueRepository outboxQueueRepository,
                              OutboundMailService outboundMailService,
                              EmailRepository emailRepository) {
        this.outboxQueueRepository = outboxQueueRepository;
        this.outboundMailService = outboundMailService;
        this.emailRepository = emailRepository;
    }

    public OutboxQueue queue(User owner, String recipient, String cc, String bcc,
                             String subject, String body, LocalDateTime scheduledAt) {
        OutboxQueue entry = new OutboxQueue();
        entry.setOwner(owner);
        entry.setRecipientAddress(recipient);
        entry.setCcAddresses(cc);
        entry.setBccAddresses(bcc);
        entry.setSubject(subject);
        entry.setBody(body);
        entry.setStatus(OutboxQueue.QueueStatus.PENDING);
        entry.setScheduledAt(scheduledAt);
        return outboxQueueRepository.save(entry);
    }

    public OutboxQueue queueWithUndoWindow(User owner, String recipient, String cc,
                                           String bcc, String subject, String body) {
        LocalDateTime sendAt = LocalDateTime.now().plusSeconds(10);
        return queue(owner, recipient, cc, bcc, subject, body, sendAt);
    }

    // Upserts a single DRAFT row per compose session instead of creating a new one on
    // every autosave tick. DRAFT status keeps it out of processQueue()'s PENDING scan,
    // so unlike PENDING/queueWithUndoWindow entries it never needs a real scheduledAt.
    public OutboxQueue saveDraft(User owner, Long draftId, String recipient, String cc,
                                 String bcc, String subject, String body) {
        OutboxQueue entry = null;
        if (draftId != null) {
            entry = outboxQueueRepository.findByIdAndOwner(draftId, owner)
                    .filter(e -> e.getStatus() == OutboxQueue.QueueStatus.DRAFT)
                    .orElse(null);
        }
        if (entry == null) {
            entry = new OutboxQueue();
            entry.setOwner(owner);
            entry.setStatus(OutboxQueue.QueueStatus.DRAFT);
            entry.setScheduledAt(LocalDateTime.now());
        }
        entry.setRecipientAddress(recipient);
        entry.setCcAddresses(cc);
        entry.setBccAddresses(bcc);
        entry.setSubject(subject);
        entry.setBody(body);
        return outboxQueueRepository.save(entry);
    }

    // Owner-scoped: a draft/pending id belonging to another user can never be cancelled.
    public boolean cancel(Long queueId, User owner) {
        Optional<OutboxQueue> entry = outboxQueueRepository.findByIdAndOwner(queueId, owner);
        if (entry.isPresent() && (entry.get().getStatus() == OutboxQueue.QueueStatus.PENDING
                || entry.get().getStatus() == OutboxQueue.QueueStatus.DRAFT)) {
            entry.get().setStatus(OutboxQueue.QueueStatus.CANCELLED);
            outboxQueueRepository.save(entry.get());
            return true;
        }
        return false;
    }

    private static final int MAX_DISPATCH_ATTEMPTS = 5;

    @Scheduled(fixedDelay = 5000)
    @Transactional
    public void processQueue() {
        List<OutboxQueue> ready = outboxQueueRepository.findByStatusAndScheduledAtBefore(
                OutboxQueue.QueueStatus.PENDING, LocalDateTime.now());

        for (OutboxQueue entry : ready) {
            boolean sent = outboundMailService.dispatch(entry);

            if (sent) {
                entry.setStatus(OutboxQueue.QueueStatus.DISPATCHED);
                outboxQueueRepository.save(entry);
                saveSentCopy(entry);
            } else {
                entry.setAttemptCount(entry.getAttemptCount() + 1);
                if (entry.getAttemptCount() >= MAX_DISPATCH_ATTEMPTS) {
                    entry.setStatus(OutboxQueue.QueueStatus.FAILED);
                    outboxQueueRepository.save(entry);
                    System.err.println("DISPATCH FAILED — giving up after " + entry.getAttemptCount()
                            + " attempts: " + entry.getId());
                } else {
                    outboxQueueRepository.save(entry);
                    System.err.println("DISPATCH FAILED — attempt " + entry.getAttemptCount()
                            + "/" + MAX_DISPATCH_ATTEMPTS + ", keeping in queue for retry: " + entry.getId());
                }
            }
        }
    }

    // Purge dispatched, cancelled, and permanently-failed entries older than 30 days — runs every Sunday at 3:30 AM
    @Scheduled(cron = "0 30 3 * * SUN")
    @Transactional
    public void purgeStaleEntries() {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(30);
        List<OutboxQueue> stale = outboxQueueRepository
                .findByStatusInAndCreatedAtBefore(
                        List.of(OutboxQueue.QueueStatus.DISPATCHED,
                                OutboxQueue.QueueStatus.CANCELLED,
                                OutboxQueue.QueueStatus.FAILED,
                                OutboxQueue.QueueStatus.DRAFT),
                        cutoff);
        outboxQueueRepository.deleteAll(stale);
    }

    private void saveSentCopy(OutboxQueue entry) {
        Email sentEmail = new Email();
        sentEmail.setOwner(entry.getOwner());
        sentEmail.setSenderAddress(entry.getOwner().getEmailAddress());
        sentEmail.setRecipientAddress(entry.getRecipientAddress());
        sentEmail.setCcAddresses(entry.getCcAddresses());
        sentEmail.setBccAddresses(entry.getBccAddresses());
        sentEmail.setSubject(entry.getSubject());
        sentEmail.setBody(entry.getBody());
        sentEmail.setState(Email.EmailState.SENT);
        emailRepository.save(sentEmail);
    }

    public List<OutboxQueue> getPendingByOwner(User owner) {
        return outboxQueueRepository.findByOwnerAndStatusOrderByCreatedAtDesc(
                owner, OutboxQueue.QueueStatus.PENDING);
    }
}