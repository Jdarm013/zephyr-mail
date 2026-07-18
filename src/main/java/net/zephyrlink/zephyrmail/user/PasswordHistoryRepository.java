package net.zephyrlink.zephyrmail.user;

import net.zephyrlink.zephyrmail.user.PasswordHistory;
import net.zephyrlink.zephyrmail.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PasswordHistoryRepository extends JpaRepository<PasswordHistory, Long> {

    // Grabs the history for a specific user, sorted newest first
    List<PasswordHistory> findByOwnerOrderByCreatedAtDesc(User owner);

}