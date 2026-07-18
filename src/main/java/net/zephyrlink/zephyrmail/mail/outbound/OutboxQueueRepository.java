package net.zephyrlink.zephyrmail.mail.outbound;

import net.zephyrlink.zephyrmail.mail.outbound.OutboxQueue;
import net.zephyrlink.zephyrmail.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OutboxQueueRepository extends JpaRepository<OutboxQueue, Long> {

    List<OutboxQueue> findByStatusAndScheduledAtBefore(
            OutboxQueue.QueueStatus status, LocalDateTime now);

    List<OutboxQueue> findByOwnerAndStatusOrderByCreatedAtDesc(
            User owner, OutboxQueue.QueueStatus status);

    // Used by purge job to clean up dispatched and cancelled entries
    List<OutboxQueue> findByStatusInAndCreatedAtBefore(
            List<OutboxQueue.QueueStatus> statuses, LocalDateTime cutoff);
}