package net.zephyrlink.zephyrmail.alias;

import net.zephyrlink.zephyrmail.alias.ShadowRoute;
import net.zephyrlink.zephyrmail.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ShadowRouteRepository extends JpaRepository<ShadowRoute, Long> {

    // Used by the Admin/User dashboard to list all their active and dead aliases
    List<ShadowRoute> findByOwnerOrderByCreatedAtDesc(User owner);

    // CRITICAL for SMTP Routing: Finds the exact route for an incoming burner email
    Optional<ShadowRoute> findByAliasAddress(String aliasAddress);

    // Checks if an alias exists AND if the kill switch is still active
    Optional<ShadowRoute> findByAliasAddressAndIsActiveTrue(String aliasAddress);
}