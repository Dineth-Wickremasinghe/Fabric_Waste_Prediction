package org.example.fabric_waste_prediction.Repository;

import org.example.fabric_waste_prediction.Entity.CuttingJob;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CuttingJobRepository extends JpaRepository<CuttingJob, Long> {
}