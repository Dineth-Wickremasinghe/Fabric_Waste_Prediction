package org.example.fabric_waste_prediction.Config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.fabric_waste_prediction.Service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import java.io.IOException;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // ── Authentication Provider ───────────────────────────────────────────────
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    // ── Authentication Manager ────────────────────────────────────────────────
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    // ── Custom Success Handler ────────────────────────────────────────────────
    @Bean
    public AuthenticationSuccessHandler customSuccessHandler() {
        return new AuthenticationSuccessHandler() {
            @Override
            public void onAuthenticationSuccess(HttpServletRequest request,
                                                HttpServletResponse response,
                                                Authentication authentication) throws IOException {
                if (authentication.getAuthorities()
                        .contains(new SimpleGrantedAuthority("ROLE_ADMIN"))) {
                    response.sendRedirect("/admin/dashboard");
                } else {
                    response.sendRedirect("/user/home");
                }
            }
        };
    }

    // ── Security Filter Chain ─────────────────────────────────────────────────
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authenticationProvider(authenticationProvider())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/admin/login",
                                "/user/login",
                                "/access-denied",
                                "/css/**",
                                "/js/**",
                                "/images/**",
                                "/webjars/**",
                                "/error"
                        ).permitAll()
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .requestMatchers("/user/home").hasAnyRole("CUTTING_MANAGER", "SUSTAINABILITY_OFFICER",
                                "TECHNICAL_OFFICER", "BUSINESS_ANALYST", "MANAGING_DIRECTOR")
                        .requestMatchers("/user/historical/**").hasAnyRole("BUSINESS_ANALYST", "MANAGING_DIRECTOR")
                        .requestMatchers("/sustainability/**").hasAnyRole("SUSTAINABILITY_OFFICER", "MANAGING_DIRECTOR")
                        .requestMatchers("/report/**").hasAnyRole("BUSINESS_ANALYST", "MANAGING_DIRECTOR")
                        .requestMatchers("/monitoring/**").hasAnyRole("TECHNICAL_OFFICER", "MANAGING_DIRECTOR")
                        .requestMatchers("/dashboard/**").hasAnyRole("CUTTING_MANAGER", "MANAGING_DIRECTOR")
                        .requestMatchers("/cutting/risk/**").hasAnyRole("CUTTING_MANAGER", "MANAGING_DIRECTOR")
                        .anyRequest().authenticated()
                )
                // ── Single formLogin handles BOTH admin and user ──────────────────
                // customSuccessHandler redirects to correct page based on role
                .formLogin(form -> form
                        .loginPage("/user/login")
                        .loginProcessingUrl("/user/login")
                        .successHandler(customSuccessHandler())
                        .failureUrl("/user/login?error=true")
                        .usernameParameter("username")
                        .passwordParameter("password")
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/user/login?logout=true")
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                        .permitAll()
                )
                .exceptionHandling(ex -> ex
                        .accessDeniedPage("/access-denied")
                )
                .csrf(csrf -> csrf.disable());

        return http.build();
    }
}