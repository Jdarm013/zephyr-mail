package net.zephyrlink.zephyrmail.mail.inbound;

import net.zephyrlink.zephyrmail.mail.inbound.BounceBlock;
import net.zephyrlink.zephyrmail.user.User;
import net.zephyrlink.zephyrmail.mail.inbound.BounceBlockRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BounceBlockService {

    private final BounceBlockRepository bounceBlockRepository;

    public BounceBlockService(BounceBlockRepository bounceBlockRepository) {
        this.bounceBlockRepository = bounceBlockRepository;
    }

    // Cached — hot path for SMTP accept check (5-min TTL from CacheConfig)
    @Cacheable(value = "bounceBlocks", key = "#senderAddress")
    public boolean isBlocked(String senderAddress) {
        return bounceBlockRepository.findByBlockedAddress(senderAddress).isPresent();
    }

    // Evict by address so an unblocked sender is accepted on next delivery attempt
    public BounceBlock block(User owner, String senderAddress) {
        BounceBlock block = new BounceBlock();
        block.setOwner(owner);
        block.setBlockedAddress(senderAddress);
        return bounceBlockRepository.save(block);
    }

    @CacheEvict(value = "bounceBlocks", key = "#senderAddress")
    public void unblock(Long blockId, String senderAddress) {
        bounceBlockRepository.deleteById(blockId);
    }

    public List<BounceBlock> getByOwner(User owner) {
        return bounceBlockRepository.findByOwnerOrderByCreatedAtDesc(owner);
    }
}