package org.example.fabric_waste_prediction.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table
public class Prediction {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    // Store the input features as a comma-separated string
    @Column(name = "input_features")
    private String inputFeatures;

    @Column(name ="pattern_complexity")
    private Double patternComplexity;

    @Column(name = "operator_experience")
    private Double operatorExperience;

    @Column(name="fabric_pattern")
    private String fabricPattern;

    @Column(name ="cutting_method")
    private String cuttingMethod;

    @Column(name="fabric_type")
    private  String fabricType;

    @Column (name="marker_loss_pct")
    private Double markerLossPct;


    @Column(name = "prediction_result")
    private Double predictionResult;

    @Column(name ="actual_result")
    private Double actualResult;

    @Column(name = "created_at")
    private LocalDateTime createdAt;



    // Called automatically before saving
    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

}
