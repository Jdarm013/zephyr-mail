package net.zephyrlink.zephyrmail.security.audit;

import net.zephyrlink.zephyrmail.security.audit.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    // Fetches the master log, newest events at the top
    List<AuditLog> findAllByOrderByTimestampDesc();

    // Allows the SOC Dashboard to filter by threat level (e.g., show me only BREACH_ATTEMPTs)
    List<AuditLog> findBySeverityOrderByTimestampDesc(AuditLog.Severity severity);

    // Tracks a specific user's footprint
    List<AuditLog> findByActorEmailOrderByTimestampDesc(String actorEmail);

    // Used by purge job to delete entries older than retention window
    void deleteByTimestampBefore(LocalDateTime cutoff);
}