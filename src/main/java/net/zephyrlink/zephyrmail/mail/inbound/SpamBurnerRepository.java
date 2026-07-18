package net.zephyrlink.zephyrmail.mail.inbound;

import net.zephyrlink.zephyrmail.mail.inbound.SpamBurner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SpamBurnerRepository extends JpaRepository<SpamBurner, Long> {

    Optional<SpamBurner> findByBurnedAddress(String burnedAddress);

    boolean existsByBurnedAddress(String burnedAddress);
}