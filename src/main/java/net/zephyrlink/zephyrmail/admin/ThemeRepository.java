package net.zephyrlink.zephyrmail.admin;

import net.zephyrlink.zephyrmail.admin.SystemTheme;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ThemeRepository extends JpaRepository<SystemTheme, Long> {

    // Instantly fetches the ONE theme designated for the live production server
    Optional<SystemTheme> findByIsActiveTrue();
}