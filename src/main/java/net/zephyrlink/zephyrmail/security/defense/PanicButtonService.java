package net.zephyrlink.zephyrmail.security.defense;

import net.zephyrlink.zephyrmail.security.audit.AuditLog;
import net.zephyrlink.zephyrmail.security.audit.AuditLogRepository;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.stereotype.Component;

@Component
public class PanicButtonService {

    private final SessionRegistry sessionRegistry;
    private final AuditLogRepository auditLogRepository;

    public PanicButtonService(SessionRegistry sessionRegistry,
                              AuditLogRepository auditLogRepository) {
        this.sessionRegistry = sessionRegistry;
        this.auditLogRepository = auditLogRepository;
    }

    public void trigger(String triggeredByEmail) {

        // KILL ALL ACTIVE SESSIONS
        for (Object principal : sessionRegistry.getAllPrincipals()) {
            for (SessionInformation session : sessionRegistry.getAllSessions(principal, false)) {
                session.expireNow();
            }
        }

        AuditLog log = new AuditLog();
        log.setAction("PANIC_BUTTON_TRIGGERED");
        log.setActorEmail(triggeredByEmail);
        log.setSeverity(AuditLog.Severity.BREACH_ATTEMPT);
        log.setDetails("All sessions invalidated. Manual restart required to restore access.");
        auditLogRepository.save(log);

        System.err.println("PANIC BUTTON TRIGGERED BY: " + triggeredByEmail);
    }
}