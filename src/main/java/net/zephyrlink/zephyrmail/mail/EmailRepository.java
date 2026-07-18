package net.zephyrlink.zephyrmail.mail;

import net.zephyrlink.zephyrmail.mail.Email;
import net.zephyrlink.zephyrmail.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EmailRepository extends JpaRepository<Email, Long> {

    List<Email> findByOwnerAndStateOrderByReceivedAtDesc(User owner, Email.EmailState state);

    // Folder views exclude trashed mail — "trash" isn't a state, it's a flag set by
    // InboxApiController.trash(), matching what TrashGarbageCollector already purges by.
    List<Email> findByOwnerAndStateAndIsDeletedFalseOrderByReceivedAtDesc(User owner, Email.EmailState state);

    List<Email> findByOwnerAndIsDeletedTrueOrderByReceivedAtDesc(User owner);

    List<Email> findByState(Email.EmailState state);

    List<Email> findByThreadIdOrderByReceivedAtAsc(String threadId);

    List<Email> findByIsDeletedTrueAndReceivedAtBefore(LocalDateTime cutoff);
}