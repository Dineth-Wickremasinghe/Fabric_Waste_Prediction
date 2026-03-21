package org.example.fabric_waste_prediction.Service;

import org.example.fabric_waste_prediction.Entity.user;
import org.example.fabric_waste_prediction.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // ── Spring Security — loads user and assigns role ─────────────────────────
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        user u = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        // Map role field to Spring Security role
        String springRole = mapToSpringRole(u);

        return org.springframework.security.core.userdetails.User
                .withUsername(u.getUsername())
                .password(u.getPassword())
                .authorities(springRole)
                .build();
    }

    // ── Maps user role/admin flag to Spring Security role ─────────────────────
    private String mapToSpringRole(user u) {
        if (u.isAdmin()) return "ROLE_ADMIN";

        // Map role field to Spring Security role
        return switch (u.getRole().toUpperCase().trim()) {
            case "CUTTING DEPARTMENT MANAGER",
                 "CUTTING_MANAGER"           -> "ROLE_CUTTING_MANAGER";
            case "SUSTAINABILITY OFFICER",
                 "SUSTAINABILITY_OFFICER"    -> "ROLE_SUSTAINABILITY_OFFICER";
            case "TECHNICAL OFFICER",
                 "TECHNICAL_OFFICER"         -> "ROLE_TECHNICAL_OFFICER";
            case "BUSINESS ANALYST",
                 "BUSINESS_ANALYST"          -> "ROLE_BUSINESS_ANALYST";
            case "MANAGING DIRECTOR",
                 "MANAGING_DIRECTOR"         -> "ROLE_MANAGING_DIRECTOR";
            default                          -> "ROLE_USER"; // fallback
        };
    }

    // ── Get all non-admin users ───────────────────────────────────────────────
    public List<user> getAllUsers() {
        return userRepository.findAll().stream()
                .filter(u -> !u.isAdmin())
                .toList();
    }

    // ── Get user by ID ────────────────────────────────────────────────────────
    public Optional<user> getUserById(Long id) {
        return userRepository.findById(id);
    }

    // ── Create user — password BCrypt encoded ─────────────────────────────────
    public String createUser(user user) {
        if (userRepository.existsByUsername(user.getUsername())) {
            return "Username already exists!";
        }
        if (userRepository.existsByEmail(user.getEmail())) {
            return "Email already exists!";
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);
        return "success";
    }

    // ── Update user ───────────────────────────────────────────────────────────
    public String updateUser(Long id, user updatedUser) {
        Optional<user> existing = userRepository.findById(id);
        if (existing.isEmpty()) return "User not found!";

        user u = existing.get();

        Optional<user> byUsername = userRepository.findByUsername(updatedUser.getUsername());
        if (byUsername.isPresent() && !byUsername.get().getId().equals(id)) {
            return "Username already taken!";
        }

        u.setFullName(updatedUser.getFullName());
        u.setEmail(updatedUser.getEmail());
        u.setUsername(updatedUser.getUsername());
        u.setRole(updatedUser.getRole());
        u.setPhoneNumber(updatedUser.getPhoneNumber());

        if (updatedUser.getPassword() != null && !updatedUser.getPassword().isBlank()) {
            u.setPassword(passwordEncoder.encode(updatedUser.getPassword()));
        }

        userRepository.save(u);
        return "success";
    }

    // ── Delete user ───────────────────────────────────────────────────────────
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    // ── Legacy authenticate — kept for compatibility ───────────────────────────
    public Optional<user> authenticate(String username, String password, boolean adminLogin) {
        Optional<user> userOpt = userRepository.findByUsername(username);
        if (userOpt.isPresent()) {
            user u = userOpt.get();
            if (passwordEncoder.matches(password, u.getPassword())) {
                if (adminLogin && u.isAdmin()) return userOpt;
                if (!adminLogin && !u.isAdmin()) return userOpt;
            }
        }
        return Optional.empty();
    }
}