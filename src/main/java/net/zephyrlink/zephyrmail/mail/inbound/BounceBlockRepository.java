package net.zephyrlink.zephyrmail.mail.inbound;

import net.zephyrlink.zephyrmail.mail.inbound.BounceBlock;
import net.zephyrlink.zephyrmail.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BounceBlockRepository extends JpaRepository<BounceBlock, Long> {

    Optional<BounceBlock> findByBlockedAddress(String blockedAddress);

    List<BounceBlock> findByOwnerOrderByCreatedAtDesc(User owner);
}