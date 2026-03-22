package org.example.fabric_waste_prediction.Repository;

import org.example.fabric_waste_prediction.Entity.EnhancedSustainabilityMetrics;
import org.example.fabric_waste_prediction.Entity.SustainabilityInput;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface EnhancedSustainabilityMetricsRepository extends JpaRepository<EnhancedSustainabilityMetrics, Long> {

    // Find metrics by input (returns List, but we'll take first)
    List<EnhancedSustainabilityMetrics> findByInput(SustainabilityInput input);

    // Alternative: find first by input
    Optional<EnhancedSustainabilityMetrics> findFirstByInput(SustainabilityInput input);

    @Query("SELECT e FROM EnhancedSustainabilityMetrics e ORDER BY e.createdAt DESC LIMIT 10")
    List<EnhancedSustainabilityMetrics> findTop10Recent();

    @Query("SELECT AVG(e.overallSustainabilityScore) FROM EnhancedSustainabilityMetrics e")
    Double getAverageOverallScore();

    @Query("SELECT e FROM EnhancedSustainabilityMetrics e WHERE e.sustainabilityRating = :rating")
    List<EnhancedSustainabilityMetrics> findByRating(@Param("rating") String rating);
}