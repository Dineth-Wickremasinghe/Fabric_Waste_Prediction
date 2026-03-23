package org.example.fabric_waste_prediction.Repository;

import org.example.fabric_waste_prediction.Entity.Report;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface ReportRepository extends JpaRepository<Report, UUID> {

    // Reports generated today
    @Query("SELECT COUNT(r) FROM Report r WHERE r.generatedAt >= :startOfDay")
    Long countTodayReports(ZonedDateTime startOfDay);

    // Most recent report
    @Query("SELECT r FROM Report r ORDER BY r.generatedAt DESC")
    List<Report> findAllOrderByGeneratedAtDesc();

    // Reports ordered newest first
    List<Report> findAllByOrderByGeneratedAtDesc();
}
