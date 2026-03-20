package org.example.fabric_waste_prediction.Repository;

import org.example.fabric_waste_prediction.Entity.CuttingJob;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CuttingJobRepository extends JpaRepository<CuttingJob, Long> {
    Optional<CuttingJob> findByJobId(String jobId);
    boolean existsByJobId(String jobId);

    @Query("SELECT c.fabricLotId, AVG(c.actualWastagePct) FROM CuttingJob c GROUP BY c.fabricLotId")
    List<Object[]> getAvgWasteByPattern();

    @Query("SELECT c.materialId, AVG(c.actualWastagePct) FROM CuttingJob c GROUP BY c.materialId")
    List<Object[]> getAvgWasteByMaterial();


    // Shift-wise: Day vs Night
    @Query("SELECT c.shift, AVG(c.actualWastagePct) FROM CuttingJob c WHERE c.shift IS NOT NULL GROUP BY c.shift")
    List<Object[]> getAvgWasteByShift();

    // Cutting method: Manual vs Auto
    @Query("SELECT c.cuttingMethod, AVG(c.actualWastagePct) FROM CuttingJob c WHERE c.cuttingMethod IS NOT NULL GROUP BY c.cuttingMethod")
    List<Object[]> getAvgWasteByCuttingMethod();

    // Overall average waste
    @Query("SELECT AVG(c.actualWastagePct) FROM CuttingJob c WHERE c.actualWastagePct IS NOT NULL")
    Double getAvgWaste();

    // Max waste
    @Query("SELECT MAX(c.actualWastagePct) FROM CuttingJob c")
    Double getMaxWaste();

    // Status breakdown for insights
    @Query("SELECT c.status, COUNT(c) FROM CuttingJob c GROUP BY c.status")
    List<Object[]> getJobStatusBreakdown();
}