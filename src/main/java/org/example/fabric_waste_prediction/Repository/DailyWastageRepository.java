package org.example.fabric_waste_prediction.Repository;

import org.example.fabric_waste_prediction.Entity.DailyWastage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface DailyWastageRepository extends JpaRepository<DailyWastage, Long> {

    List<DailyWastage> findByTrackingDateBetween(LocalDate startDate, LocalDate endDate);

    List<DailyWastage> findByFabricTypeOrderByTrackingDateDesc(String fabricType);

    @Query("SELECT d FROM DailyWastage d WHERE d.trackingDate >= :startDate ORDER BY d.trackingDate")
    List<DailyWastage> findLast30Days(@Param("startDate") LocalDate startDate);

    @Query("SELECT AVG(d.actualWastagePct) FROM DailyWastage d WHERE d.fabricType = :fabricType")
    Double getAverageWastageByFabric(@Param("fabricType") String fabricType);

    @Query("SELECT d.fabricType, AVG(d.actualWastagePct) as avgWastage, " +
            "SUM(d.wasteAmountKg) as totalWaste, COUNT(d) as jobCount " +
            "FROM DailyWastage d " +
            "WHERE d.trackingDate >= :startDate " +
            "GROUP BY d.fabricType")
    List<Object[]> getFabricWastageStats(@Param("startDate") LocalDate startDate);

    @Query("SELECT SUM(d.wasteAmountKg) FROM DailyWastage d WHERE d.trackingDate = :date")
    Double getTotalWasteForDate(@Param("date") LocalDate date);
}