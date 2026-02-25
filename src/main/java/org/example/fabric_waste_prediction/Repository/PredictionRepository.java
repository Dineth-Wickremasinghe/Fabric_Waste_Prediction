package org.example.fabric_waste_prediction.Repository;

import org.example.fabric_waste_prediction.Entity.Prediction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import java.util.List;

 
@Repository
//Test to see if Prediction class can be added instead of PredictionRecord, if not then we will have to change the name of the class to PredictionRecord
public interface PredictionRepository extends JpaRepository<Prediction, Long> {
    List<Prediction> findAllByOrderByCreatedAtDesc();

    List<Prediction> findByActualResultNotNull();
}
