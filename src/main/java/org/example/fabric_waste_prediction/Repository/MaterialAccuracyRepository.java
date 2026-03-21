package org.example.fabric_waste_prediction.Repository;

import org.example.fabric_waste_prediction.Entity.MaterialAccuracy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface MaterialAccuracyRepository extends JpaRepository<MaterialAccuracy, UUID> {

    @Query("SELECT m.name, AVG(a.accuracyPct) FROM MaterialAccuracy a JOIN a.material m GROUP BY m.name")
    List<Object[]> getAccuracyByMaterial();
}