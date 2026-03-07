package org.example.fabric_waste_prediction.Repository;

import org.example.fabric_waste_prediction.Entity.CuttingJob;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CuttingJobRepository extends JpaRepository<CuttingJob, Long> {
    Optional<CuttingJob> findByJobId(String jobId);
    boolean existsByJobId(String jobId);
}