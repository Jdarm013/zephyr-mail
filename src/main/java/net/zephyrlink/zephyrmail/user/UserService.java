package net.zephyrlink.zephyrmail.user;

import net.zephyrlink.zephyrmail.user.User;
import net.zephyrlink.zephyrmail.user.UserRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // Cached — hot path for SMTP accept and deliver (5-min TTL from CacheConfig)
    @Cacheable(value = "userLookup", key = "#emailAddress")
    public Optional<User> findActiveByEmail(String emailAddress) {
        return userRepository.findByEmailAddressAndDeletedFalse(emailAddress);
    }

    // Evict on delete so a removed user is not accepted on next delivery attempt
    @CacheEvict(value = "userLookup", key = "#emailAddress")
    public void evictUser(String emailAddress) {
        // eviction only — no DB operation
    }

    public Optional<User> findByEmail(String emailAddress) {
        return userRepository.findByEmailAddress(emailAddress);
    }

    public List<User> findAllActive() {
        return userRepository.findAllByDeletedFalse();
    }
}