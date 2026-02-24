package org.example.fabric_waste_prediction.repository;

import org.example.fabric_waste_prediction.entity.SustainabilityMetrics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface SustainabilityRepository extends JpaRepository<SustainabilityMetrics, Long> {

    // Find metrics by date range
    List<SustainabilityMetrics> findByMetricDateBetween(LocalDate startDate, LocalDate endDate);

    // Find all metrics for a specific date (returns list since multiple records can exist per day)
    List<SustainabilityMetrics> findByMetricDate(LocalDate date);

    // Get last 7 days of metrics (grouped by date, showing the latest record for each day)
    @Query("SELECT s FROM SustainabilityMetrics s WHERE s.metricDate >= :startDate ORDER BY s.metricDate DESC, s.id DESC")
    List<SustainabilityMetrics> findLast7Days(@Param("startDate") LocalDate startDate);

    // Convenience method to get last 7 days from current date
    default List<SustainabilityMetrics> findLast7Days() {
        return findLast7Days(LocalDate.now().minusDays(7));
    }

    // Get average sustainability score between two dates
    @Query("SELECT AVG(s.sustainabilityScore) FROM SustainabilityMetrics s WHERE s.metricDate BETWEEN :startDate AND :endDate")
    Double getAverageScoreBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    // Get total waste reduced between two dates
    @Query("SELECT SUM(s.wasteReducedTons) FROM SustainabilityMetrics s WHERE s.metricDate BETWEEN :startDate AND :endDate")
    Double getTotalWasteReduced(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    // Get total carbon avoided between two dates
    @Query("SELECT SUM(s.carbonAvoidedTons) FROM SustainabilityMetrics s WHERE s.metricDate BETWEEN :startDate AND :endDate")
    Double getTotalCarbonAvoided(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    // Get total water saved between two dates
    @Query("SELECT SUM(s.waterSavedL) FROM SustainabilityMetrics s WHERE s.metricDate BETWEEN :startDate AND :endDate")
    Double getTotalWaterSaved(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    // Get total cost saved (in LKR) between two dates
    @Query("SELECT SUM(s.costSavedLkr) FROM SustainabilityMetrics s WHERE s.metricDate BETWEEN :startDate AND :endDate")
    Double getTotalCostSaved(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    // Get total energy saved between two dates
    @Query("SELECT SUM(s.energySavedKwh) FROM SustainabilityMetrics s WHERE s.metricDate BETWEEN :startDate AND :endDate")
    Double getTotalEnergySaved(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    // Get total landfill avoided between two dates
    @Query("SELECT SUM(s.landfillAvoidedKg) FROM SustainabilityMetrics s WHERE s.metricDate BETWEEN :startDate AND :endDate")
    Double getTotalLandfillAvoided(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    // Get total fabric saved between two dates
    @Query("SELECT SUM(s.fabricSavedKg) FROM SustainabilityMetrics s WHERE s.metricDate BETWEEN :startDate AND :endDate")
    Double getTotalFabricSaved(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    // Get statistics grouped by fabric type for a date range
    @Query("SELECT s.fabricType, " +
            "COUNT(s), " +
            "AVG(s.predictedWastage), " +
            "AVG(s.sustainabilityScore), " +
            "SUM(s.fabricSavedKg), " +
            "SUM(s.costSavedLkr) " +
            "FROM SustainabilityMetrics s " +
            "WHERE s.metricDate BETWEEN :startDate AND :endDate " +
            "AND s.fabricType IS NOT NULL " +
            "GROUP BY s.fabricType")
    List<Object[]> getFabricStatistics(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    // Get daily totals for charting
    @Query("SELECT s.metricDate, " +
            "SUM(s.carbonAvoidedTons), " +
            "SUM(s.waterSavedL), " +
            "SUM(s.energySavedKwh), " +
            "SUM(s.costSavedLkr) " +
            "FROM SustainabilityMetrics s " +
            "WHERE s.metricDate BETWEEN :startDate AND :endDate " +
            "GROUP BY s.metricDate " +
            "ORDER BY s.metricDate")
    List<Object[]> getDailyTotals(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    // Get the latest record
    @Query("SELECT s FROM SustainabilityMetrics s ORDER BY s.metricDate DESC, s.id DESC LIMIT 1")
    SustainabilityMetrics findLatest();

    // Get top 10 most recent records
    @Query("SELECT s FROM SustainabilityMetrics s ORDER BY s.metricDate DESC, s.id DESC LIMIT 10")
    List<SustainabilityMetrics> findTop10Recent();

    // Count total predictions in a date range
    @Query("SELECT COUNT(s) FROM SustainabilityMetrics s WHERE s.metricDate BETWEEN :startDate AND :endDate")
    Long countPredictionsBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    // Get average wastage percentage by fabric type
    @Query("SELECT s.fabricType, AVG(s.predictedWastage) FROM SustainabilityMetrics s " +
            "WHERE s.metricDate BETWEEN :startDate AND :endDate " +
            "AND s.fabricType IS NOT NULL " +
            "GROUP BY s.fabricType")
    List<Object[]> getAverageWastageByFabric(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    // Get highest cost saving records
    @Query("SELECT s FROM SustainabilityMetrics s WHERE s.metricDate BETWEEN :startDate AND :endDate " +
            "ORDER BY s.costSavedLkr DESC LIMIT 5")
    List<SustainabilityMetrics> findTopCostSavings(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    // Get highest waste reduction records
    @Query("SELECT s FROM SustainabilityMetrics s WHERE s.metricDate BETWEEN :startDate AND :endDate " +
            "ORDER BY s.wasteReducedTons DESC LIMIT 5")
    List<SustainabilityMetrics> findTopWasteReduction(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
}