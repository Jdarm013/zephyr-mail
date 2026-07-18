package net.zephyrlink.zephyrmail.security;

import net.zephyrlink.zephyrmail.security.auth.CustomAuthenticationFailureHandler;

import net.zephyrlink.zephyrmail.security.defense.PayloadSanitizer;
import net.zephyrlink.zephyrmail.security.defense.ProofOfWorkFilter;
import net.zephyrlink.zephyrmail.security.defense.TurnstileFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.session.HttpSessionEventPublisher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final CustomAuthenticationFailureHandler failureHandler;

    public SecurityConfig(CustomAuthenticationFailureHandler failureHandler) {
        this.failureHandler = failureHandler;
    }

    @Bean
    public SessionRegistry sessionRegistry() {
        return new SessionRegistryImpl();
    }

    @Bean
    public HttpSessionEventPublisher httpSessionEventPublisher() {
        return new HttpSessionEventPublisher();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http.cors(cors -> cors.configurationSource(corsConfigurationSource()));

        // SPA CSRF — correct configuration for fetch-based JavaScript login
        // Handles CookieCsrfTokenRepository, BREACH protection, and cookie refresh after login/logout
        http.csrf(csrf -> csrf
                .spa()
                .ignoringRequestMatchers("/api/webhooks/resend")
        );

        http.headers(headers -> headers
                .frameOptions(frame -> frame.deny())
                .contentSecurityPolicy(csp -> csp
                        .policyDirectives("default-src 'self'; " +
                                "script-src 'self' 'unsafe-inline' https://challenges.cloudflare.com; " +
                                "frame-src https://challenges.cloudflare.com; " +
                                "style-src 'self' 'unsafe-inline' https://fonts.googleapis.com; " +
                                "font-src 'self' https://fonts.gstatic.com; " +
                                "img-src 'self' data:;")
                )
                .httpStrictTransportSecurity(hsts -> hsts
                        .includeSubDomains(true)
                        .maxAgeInSeconds(31536000)
                )
        );

        http.sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                .maximumSessions(-1)
                .sessionRegistry(sessionRegistry())
        );

        http.authorizeHttpRequests(auth -> auth
                .requestMatchers(
                        "/login",
                        "/register",
                        "/error",
                        "/css/**",
                        "/images/**",
                        "/icons/**",
                        "/manifest.json",
                        "/api/webhooks/resend",
                        "/emergency-access"
                ).permitAll()
                .anyRequest().authenticated()
        );

        http.formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/", true)
                .failureHandler(failureHandler)
                .permitAll()
        );

        http.logout(logout -> logout
                .logoutSuccessUrl("/login?logout")
                .permitAll()
        );

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("https://zephyrlink.net", "https://mail.zephyrlink.net", "http://localhost:8080"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("Content-Type", "X-Turnstile-Token", "X-PoW-Hash", "X-PoW-Nonce", "X-XSRF-TOKEN"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8();
    }

    // Must run before Spring Security's own filter chain, or a matched POST /login is
    // authenticated and redirected by Spring Security before ever reaching these filters.
    @Bean
    public FilterRegistrationBean<PayloadSanitizer> payloadSanitizerRegistration(PayloadSanitizer filter) {
        FilterRegistrationBean<PayloadSanitizer> registration = new FilterRegistrationBean<>(filter);
        registration.setEnabled(true);
        registration.setOrder(-300);
        return registration;
    }

    @Bean
    public FilterRegistrationBean<TurnstileFilter> turnstileFilterRegistration(TurnstileFilter filter) {
        FilterRegistrationBean<TurnstileFilter> registration = new FilterRegistrationBean<>(filter);
        registration.setEnabled(true);
        registration.setOrder(-200);
        return registration;
    }

    @Bean
    public FilterRegistrationBean<ProofOfWorkFilter> powFilterRegistration(ProofOfWorkFilter filter) {
        FilterRegistrationBean<ProofOfWorkFilter> registration = new FilterRegistrationBean<>(filter);
        registration.setEnabled(true);
        registration.setOrder(-100);
        return registration;
    }
}