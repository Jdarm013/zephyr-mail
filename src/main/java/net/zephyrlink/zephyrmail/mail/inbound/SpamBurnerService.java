package net.zephyrlink.zephyrmail.mail.inbound;

import jakarta.annotation.PostConstruct;
import net.zephyrlink.zephyrmail.mail.inbound.SpamBurner;
import net.zephyrlink.zephyrmail.mail.inbound.SpamBurnerRepository;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Service
public class SpamBurnerService {

    private final Set<String> burnedAddresses = Collections.synchronizedSet(new HashSet<>());
    private final SpamBurnerRepository spamBurnerRepository;

    public SpamBurnerService(SpamBurnerRepository spamBurnerRepository) {
        this.spamBurnerRepository = spamBurnerRepository;
    }

    // LOAD ON STARTUP
    @PostConstruct
    public void loadFromDatabase() {
        spamBurnerRepository.findAll().forEach(entry ->
                burnedAddresses.add(entry.getBurnedAddress())
        );
        System.out.println("====== SPAM BURNER: Loaded " + burnedAddresses.size() + " burned addresses ======");
    }

    public boolean isBurned(String senderAddress) {
        return burnedAddresses.contains(senderAddress.toLowerCase());
    }

    public void burn(String senderAddress) {
        String normalized = senderAddress.toLowerCase();
        burnedAddresses.add(normalized);
        if (!spamBurnerRepository.existsByBurnedAddress(normalized)) {
            SpamBurner entry = new SpamBurner();
            entry.setBurnedAddress(normalized);
            spamBurnerRepository.save(entry);
        }
    }

    public void unburn(String senderAddress) {
        String normalized = senderAddress.toLowerCase();
        burnedAddresses.remove(normalized);
        spamBurnerRepository.findByBurnedAddress(normalized)
                .ifPresent(spamBurnerRepository::delete);
    }
}