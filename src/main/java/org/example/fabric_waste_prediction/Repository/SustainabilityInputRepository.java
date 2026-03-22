package org.example.fabric_waste_prediction.Repository;

import org.example.fabric_waste_prediction.Entity.SustainabilityInput;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SustainabilityInputRepository extends JpaRepository<SustainabilityInput, Long> {

    // Updated to use wasteDisposal (not wasteDisposalMethod)
    List<SustainabilityInput> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    @Query("SELECT s FROM SustainabilityInput s WHERE s.wasteDisposal = :disposal ORDER BY s.createdAt DESC")
    List<SustainabilityInput> findByDisposalMethod(@Param("disposal") String disposal);

    @Query("SELECT AVG(s.recyclingRate) FROM SustainabilityInput s WHERE s.createdAt >= :since")
    Double getAverageRecyclingRate(@Param("since") LocalDateTime since);

    // New query to find by rating
    List<SustainabilityInput> findByRating(String rating);

    // New query to find by certification
    List<SustainabilityInput> findByCertification(String certification);

    // Get top submissions by score
    @Query("SELECT s FROM SustainabilityInput s ORDER BY s.sustainabilityScore DESC LIMIT 10")
    List<SustainabilityInput> findTop10ByScore();
}