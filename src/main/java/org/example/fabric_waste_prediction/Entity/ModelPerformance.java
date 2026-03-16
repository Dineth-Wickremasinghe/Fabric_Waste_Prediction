package org.example.fabric_waste_prediction.Entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "model_performance")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ModelPerformance {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "recorded_at")
    private LocalDateTime recordedAt;

    @Column(name = "mse_error")
    private Double mseError;

    @Column(name = "mae_error")
    private Double maeError;

    @Column(name = "r2_score")
    private Double r2Score;

    @Column(name = "health_status")
    private String healthStatus;

    @Column(name = "predictions_made")
    private Integer predictionsMade;

    @PrePersist
    protected void onCreate() {
        recordedAt = LocalDateTime.now();
    }
}