package net.zephyrlink.zephyrmail.mail;

import net.zephyrlink.zephyrmail.mail.Email;
import net.zephyrlink.zephyrmail.security.audit.AuditLogRepository;
import net.zephyrlink.zephyrmail.mail.EmailRepository;
import net.zephyrlink.zephyrmail.security.auth.LoginAttemptRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TrashGarbageCollector {

    private static final int BATCH_SIZE = 100;

    private final EmailRepository emailRepository;
    private final AuditLogRepository auditLogRepository;
    private final LoginAttemptRepository loginAttemptRepository;

    public TrashGarbageCollector(EmailRepository emailRepository,
                                 AuditLogRepository auditLogRepository,
                                 LoginAttemptRepository loginAttemptRepository) {
        this.emailRepository = emailRepository;
        this.auditLogRepository = auditLogRepository;
        this.loginAttemptRepository = loginAttemptRepository;
    }

    // Purge deleted emails older than 30 days — runs every Sunday at 3:00 AM
    @Scheduled(cron = "0 0 3 * * SUN")
    @Transactional
    public void purgeExpiredTrash() {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(30);
        List<Email> expired = emailRepository.findByIsDeletedTrueAndReceivedAtBefore(cutoff);

        for (int i = 0; i < expired.size(); i += BATCH_SIZE) {
            List<Email> batch = expired.subList(i, Math.min(i + BATCH_SIZE, expired.size()));
            emailRepository.deleteAll(batch);
        }
    }

    // Purge audit log entries older than 90 days — runs every Sunday at 3:15 AM
    @Scheduled(cron = "0 15 3 * * SUN")
    @Transactional
    public void purgeAuditLogs() {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(90);
        auditLogRepository.deleteByTimestampBefore(cutoff);
    }

    // Purge login attempt entries older than 30 days — runs every Sunday at 3:20 AM
    @Scheduled(cron = "0 20 3 * * SUN")
    @Transactional
    public void purgeLoginAttempts() {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(30);
        loginAttemptRepository.deleteByTimestampBefore(cutoff);
    }
}