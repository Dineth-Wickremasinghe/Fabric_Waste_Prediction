package org.example.fabric_waste_prediction.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDate;

@Entity
@Table(name = "fabric_risks")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FabricRisk {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "fabric_type", nullable = false)
    private String fabricType;

    @Column(name = "avg_wastage_pct")
    private Double avgWastagePct;

    @Column(name = "risk_level")
    private String riskLevel;

    @Column(name = "total_jobs")
    private Integer totalJobs;

    @Column(name = "recorded_date")
    private LocalDate recordedDate;

    @Column(name = "total_waste_kg")
    private Double totalWasteKg;

    @Column(name = "optimization_needed")
    private Boolean optimizationNeeded;
}