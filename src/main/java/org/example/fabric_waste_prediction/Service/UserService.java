package org.example.fabric_waste_prediction.Service;

import org.example.fabric_waste_prediction.Entity.user;
import org.example.fabric_waste_prediction.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public List<user> getAllUsers() {
        return userRepository.findAll().stream()
                .filter(u -> !u.isAdmin())
                .toList();
    }

    public Optional<user> getUserById(Long id) {
        return userRepository.findById(id);
    }

    public String createUser(user user) {
        if (userRepository.existsByUsername(user.getUsername())) {
            return "Username already exists!";
        }
        if (userRepository.existsByEmail(user.getEmail())) {
            return "Email already exists!";
        }
        userRepository.save(user);
        return "success";
    }

    public String updateUser(Long id, user updatedUser) {
        Optional<user> existing = userRepository.findById(id);
        if (existing.isEmpty()) return "User not found!";

        user user = existing.get();

        // Check username conflict with another user
        Optional<user> byUsername = userRepository.findByUsername(updatedUser.getUsername());
        if (byUsername.isPresent() && !byUsername.get().getId().equals(id)) {
            return "Username already taken!";
        }

        user.setFullName(updatedUser.getFullName());
        user.setEmail(updatedUser.getEmail());
        user.setUsername(updatedUser.getUsername());
        user.setRole(updatedUser.getRole());
        user.setPhoneNumber(updatedUser.getPhoneNumber());

        // Only update password if a new one is provided
        if (updatedUser.getPassword() != null && !updatedUser.getPassword().isBlank()) {
            user.setPassword(updatedUser.getPassword());
        }

        userRepository.save(user);
        return "success";
    }

    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    public Optional<user> authenticate(String username, String password, boolean adminLogin) {
        Optional<user> userOpt = userRepository.findByUsername(username);
        if (userOpt.isPresent()) {
            user user = userOpt.get();
            if (user.getPassword().equals(password)) {
                if (adminLogin && user.isAdmin()) return userOpt;
                if (!adminLogin && !user.isAdmin()) return userOpt;
            }
        }
        return Optional.empty();
    }
}
