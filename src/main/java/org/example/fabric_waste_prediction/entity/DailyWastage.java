package org.example.fabric_waste_prediction.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDate;

@Entity
@Table(name = "daily_wastage")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DailyWastage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tracking_date", nullable = false, unique = true)
    private LocalDate trackingDate;

    @Column(name = "predicted_wastage_pct")
    private Double predictedWastagePct;

    @Column(name = "actual_wastage_pct")
    private Double actualWastagePct;

    @Column(name = "fabric_type")
    private String fabricType;

    @Column(name = "total_fabric_used_kg")
    private Double totalFabricUsedKg;

    @Column(name = "waste_amount_kg")
    private Double wasteAmountKg;

    @Column(name = "created_at")
    private LocalDate createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDate.now();
    }
}