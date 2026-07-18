package net.zephyrlink.zephyrmail.security.auth;

import net.zephyrlink.zephyrmail.security.auth.LoginAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface LoginAttemptRepository extends JpaRepository<LoginAttempt, Long> {

    // IP-BASED LOOKUPS
    int countByIpAddressAndIsSuccessfulFalseAndTimestampAfter(String ipAddress, LocalDateTime timestamp);
    Optional<LoginAttempt> findTopByIpAddressAndIsSuccessfulTrueOrderByTimestampDesc(String ipAddress);

    // ACCOUNT-BASED LOOKUPS
    int countByEmailAddressAndIsSuccessfulFalseAndTimestampAfter(String emailAddress, LocalDateTime timestamp);
    Optional<LoginAttempt> findTopByEmailAddressAndIsSuccessfulTrueOrderByTimestampDesc(String emailAddress);

    // Dashboard Telemetry
    List<LoginAttempt> findAllByOrderByTimestampDesc();

    // Used by purge job to delete entries older than retention window
    void deleteByTimestampBefore(LocalDateTime cutoff);
}