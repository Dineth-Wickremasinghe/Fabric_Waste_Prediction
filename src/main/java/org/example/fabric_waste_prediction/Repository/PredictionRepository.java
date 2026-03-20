package org.example.fabric_waste_prediction.Repository;

import org.example.fabric_waste_prediction.Entity.Prediction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import java.util.List;

 
@Repository
public interface PredictionRepository extends JpaRepository<Prediction, Long> {
    List<Prediction> findAllByOrderByCreatedAtDesc();

    List<Prediction> findByActualResultNotNull();

    // Fabric chart
    @Query("SELECT p.fabricType, AVG(p.predictionResult) FROM Prediction p " +
            "WHERE p.fabricType IS NOT NULL GROUP BY p.fabricType")
    List<Object[]> getAvgWasteByFabric();

    // Pattern chart
    @Query("SELECT p.fabricPattern, AVG(p.predictionResult) FROM Prediction p " +
            "WHERE p.fabricPattern IS NOT NULL GROUP BY p.fabricPattern")
    List<Object[]> getAvgWasteByPattern();

    // Cutting method chart (0.0 = Manual, 1.0 = Auto)
    @Query("SELECT p.cuttingMethod, AVG(p.predictionResult) FROM Prediction p " +
            "WHERE p.cuttingMethod IS NOT NULL GROUP BY p.cuttingMethod")
    List<Object[]> getAvgWasteByCuttingMethod();

    // Overall average waste
    @Query("SELECT AVG(p.predictionResult) FROM Prediction p " +
            "WHERE p.predictionResult IS NOT NULL")
    Double getAvgWaste();

    // Accuracy by fabric (using markerLossPct as proxy)
    @Query("SELECT p.fabricType, AVG(p.markerLossPct) FROM Prediction p " +
            "WHERE p.fabricType IS NOT NULL AND p.markerLossPct IS NOT NULL " +
            "GROUP BY p.fabricType")
    List<Object[]> getAccuracyByFabric();
}
