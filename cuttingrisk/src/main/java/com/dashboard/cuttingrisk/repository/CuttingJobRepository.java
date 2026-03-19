package com.dashboard.cuttingrisk.repository;

import com.dashboard.cuttingrisk.model.CuttingJob;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface CuttingJobRepository extends JpaRepository<CuttingJob, UUID> {

    // Fabric-wise: avg waste by material name
    @Query("SELECT m.name, AVG(c.actualWastagePct) FROM CuttingJob c JOIN c.material m GROUP BY m.name")
    List<Object[]> getAvgWasteByMaterial();

    // Pattern-wise: avg waste by fabric pattern
    @Query("SELECT fl.fabricPattern, AVG(c.actualWastagePct) FROM CuttingJob c JOIN c.fabricLot fl GROUP BY fl.fabricPattern")
    List<Object[]> getAvgWasteByPattern();

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