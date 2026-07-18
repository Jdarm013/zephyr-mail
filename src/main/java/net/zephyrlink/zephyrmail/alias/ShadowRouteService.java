package net.zephyrlink.zephyrmail.alias;

import net.zephyrlink.zephyrmail.alias.ShadowRoute;
import net.zephyrlink.zephyrmail.user.User;
import net.zephyrlink.zephyrmail.alias.ShadowRouteRepository;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ShadowRouteService {

    private final ShadowRouteRepository shadowRouteRepository;
    private final CacheManager cacheManager;

    public ShadowRouteService(ShadowRouteRepository shadowRouteRepository,
                              CacheManager cacheManager) {
        this.shadowRouteRepository = shadowRouteRepository;
        this.cacheManager = cacheManager;
    }

    // Cached — hot path for SMTP accept and deliver (5-min TTL from CacheConfig)
    @Cacheable(value = "shadowRoutes", key = "#aliasAddress")
    public Optional<ShadowRoute> resolve(String aliasAddress) {
        return shadowRouteRepository.findByAliasAddressAndIsActiveTrue(aliasAddress);
    }

    public ShadowRoute create(User owner, String aliasAddress, String memo) {
        ShadowRoute route = new ShadowRoute();
        route.setOwner(owner);
        route.setAliasAddress(aliasAddress);
        route.setMemo(memo);
        route.setActive(true);
        return shadowRouteRepository.save(route);
    }

    // Evict by alias address before deactivating so next delivery attempt hits DB
    public void nuke(Long routeId) {
        shadowRouteRepository.findById(routeId).ifPresent(route -> {
            cacheManager.getCache("shadowRoutes").evict(route.getAliasAddress());
            route.setActive(false);
            shadowRouteRepository.save(route);
        });
    }

    public List<ShadowRoute> getByOwner(User owner) {
        return shadowRouteRepository.findByOwnerOrderByCreatedAtDesc(owner);
    }

    public void incrementForwardCount(ShadowRoute route) {
        route.setForwardCount(route.getForwardCount() + 1);
        shadowRouteRepository.save(route);
    }
}