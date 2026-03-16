package org.example.fabric_waste_prediction.Repository;

import org.example.fabric_waste_prediction.Entity.WasteTracking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface WasteTrackingRepository extends JpaRepository<WasteTracking, Long> {

    List<WasteTracking> findByTrackingDateBetween(LocalDate startDate, LocalDate endDate);

    List<WasteTracking> findByDestination(String destination);

    @Query("SELECT w.destination, SUM(w.amountKg) FROM WasteTracking w " +
            "WHERE w.trackingDate BETWEEN :startDate AND :endDate " +
            "GROUP BY w.destination")
    List<Object[]> getWasteDestinationSummary(@Param("startDate") LocalDate startDate,
                                              @Param("endDate") LocalDate endDate);

    @Query("SELECT SUM(w.amountKg) FROM WasteTracking w WHERE w.recycledPercentage > 50")
    Double getTotalRecycledWaste();

    @Query("SELECT w.fabricType, SUM(w.amountKg) FROM WasteTracking w " +
            "WHERE w.trackingDate >= :startDate GROUP BY w.fabricType")
    List<Object[]> getWasteByFabricType(@Param("startDate") LocalDate startDate);
}