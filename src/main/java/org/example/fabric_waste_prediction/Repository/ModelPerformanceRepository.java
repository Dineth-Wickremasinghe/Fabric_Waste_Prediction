package org.example.fabric_waste_prediction.Repository;

import org.example.fabric_waste_prediction.Entity.ModelPerformance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ModelPerformanceRepository extends JpaRepository<ModelPerformance, Long> {
}
