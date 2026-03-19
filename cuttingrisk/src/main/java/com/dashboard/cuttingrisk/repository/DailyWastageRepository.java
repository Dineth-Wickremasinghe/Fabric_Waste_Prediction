package com.dashboard.cuttingrisk.repository;

import com.dashboard.cuttingrisk.model.DailyWastage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface DailyWastageRepository extends JpaRepository<DailyWastage, UUID> {

    @Query("SELECT d FROM DailyWastage d ORDER BY d.trackingDate ASC")
    List<DailyWastage> findAllOrderedByDate();

    @Query("SELECT d FROM DailyWastage d WHERE d.actualWastagePct IS NOT NULL ORDER BY d.trackingDate ASC")
    List<DailyWastage> findAllWithActual();
}