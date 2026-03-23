package org.example.fabric_waste_prediction.Repository;

import org.example.fabric_waste_prediction.Entity.user;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<user, Long> {
    Optional<user> findByUsername(String username);
    Optional<user> findByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
}