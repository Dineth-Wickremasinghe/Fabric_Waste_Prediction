package org.example.fabric_waste_prediction.repository;

import org.example.fabric_waste_prediction.entity.user;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<user, Long> {
}
