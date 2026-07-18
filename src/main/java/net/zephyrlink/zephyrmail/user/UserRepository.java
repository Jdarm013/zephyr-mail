package net.zephyrlink.zephyrmail.user;

import java.util.List;
import java.util.Optional;
import net.zephyrlink.zephyrmail.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // 1. GLOBAL SEARCH: Finds any email ever used (Used for duplicate checks)
    Optional<User> findByEmailAddress(String emailAddress);

    // 2. ACTIVE SEARCH: Only finds users who are NOT deleted (Used for Login)
    Optional<User> findByEmailAddressAndDeletedFalse(String emailAddress);

    // 3. UI LIST: Only returns active users for the management table
    List<User> findAllByDeletedFalse();
}