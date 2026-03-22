package org.example.fabric_waste_prediction.Initializer;

import org.example.fabric_waste_prediction.Entity.user;
import org.example.fabric_waste_prediction.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class AdminInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (userRepository.findByUsername("admin").isEmpty()) {
            user admin = new user();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin123")); // BCrypt encoded
            admin.setFullName("System Administrator");
            admin.setEmail("admin@fabricsystem.com");
            admin.setPhoneNumber("0000000000");
            admin.setRole("ADMIN");
            admin.setAdmin(true);
            userRepository.save(admin);
            System.out.println("✅ Default admin created: username=admin, password=admin123");
        }
    }
}