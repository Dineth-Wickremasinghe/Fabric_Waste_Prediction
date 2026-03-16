package org.example.fabric_waste_prediction.Entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDate;

@Entity
@Table(name = "sustainability")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SustainabilityMetrics {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "metric_date", nullable = false)  // Removed unique = true
    private LocalDate metricDate;

    @Column(name = "carbon_avoided_tons")
    private Double carbonAvoidedTons = 0.0;

    @Column(name = "water_saved_l")
    private Double waterSavedL = 0.0;

    @Column(name = "waste_reduced_tons")
    private Double wasteReducedTons = 0.0;

    @Column(name = "energy_saved_kwh")
    private Double energySavedKwh = 0.0;

    @Column(name = "cost_saved_lkr")
    private Double costSavedLkr = 0.0;

    @Column(name = "landfill_avoided_kg")
    private Double landfillAvoidedKg = 0.0;

    @Column(name = "fabric_saved_kg")
    private Double fabricSavedKg = 0.0;

    @Column(name = "sustainability_score")
    private Double sustainabilityScore = 0.0;

    @Column(name = "fabric_type")  // Add this to track which fabric was used
    private String fabricType;

    @Column(name = "order_quantity")  // Add this to track order quantity
    private Double orderQuantity;

    @Column(name = "predicted_wastage")  // Add this to track predicted wastage
    private Double predictedWastage;

    @Column(name = "created_at")
    private LocalDate createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDate.now();
    }
}