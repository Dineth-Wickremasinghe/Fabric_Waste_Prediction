package org.example.fabric_waste_prediction.Repository;

import org.example.fabric_waste_prediction.Entity.CuttingRiskRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface CuttingRiskRecordRepository
        extends JpaRepository<CuttingRiskRecord, UUID> {

    @Query("SELECT r FROM CuttingRiskRecord r " +
            "JOIN FETCH r.prediction " +
            "ORDER BY r.jobDate DESC")
    List<CuttingRiskRecord> findAllWithPrediction();

    @Query("SELECT r.shift, AVG(r.actualWastagePct) " +
            "FROM CuttingRiskRecord r " +
            "WHERE r.shift IS NOT NULL " +
            "GROUP BY r.shift")
    List<Object[]> getAvgWasteByShift();

    @Query("SELECT r.cuttingMethod, AVG(r.actualWastagePct) " +
            "FROM CuttingRiskRecord r " +
            "WHERE r.cuttingMethod IS NOT NULL " +
            "GROUP BY r.cuttingMethod")
    List<Object[]> getAvgWasteByCuttingMethod();

    @Query("SELECT " +
            "CASE " +
            "WHEN r.fabricGsm < 150 THEN 'Light (<150)' " +
            "WHEN r.fabricGsm < 250 THEN 'Medium (150-250)' " +
            "ELSE 'Heavy (>250)' END, " +
            "AVG(r.actualWastagePct) " +
            "FROM CuttingRiskRecord r " +
            "WHERE r.fabricGsm IS NOT NULL " +
            "GROUP BY 1")
    List<Object[]> getAvgWasteByGsm();
}