package org.example.fabric_waste_prediction.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Setter
@Getter
@Entity
@Table(name="prediction_records")
public class PredictionRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    // Store the input features as a comma-separated string
    @Column(name = "input_features")
    private String inputFeatures;

    @Column(name = "prediction_result")
    private Double predictionResult;


    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // Called automatically before saving
    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}

