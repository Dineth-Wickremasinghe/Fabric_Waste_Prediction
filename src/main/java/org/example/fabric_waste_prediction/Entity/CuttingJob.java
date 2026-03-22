package org.example.fabric_waste_prediction.Entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "cutting_jobs")
@Data
public class CuttingJob {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ── FK references to source tables ───────────────────────────────────────
    @Column(name = "prediction_id")
    private Long predictionId;

    @Column(name = "cutting_risk_record_id")
    private UUID cuttingRiskRecordId;

    // ── From Prediction table ─────────────────────────────────────────────────
    @Column(name = "fabric_type")
    private String fabricType;

    @Column(name = "fabric_pattern")
    private String fabricPattern;

    @Column(name = "cutting_method")
    private String cuttingMethod;

    @Column(name = "marker_loss_pct")
    private Double markerLossPct;

    @Column(name = "pattern_complexity")
    private Double patternComplexity;

    @Column(name = "operator_experience")
    private Double operatorExperience;

    @Column(name = "predicted_waste_pct")
    private Double predictedWastePct;

    // ── From CuttingRiskRecord table ──────────────────────────────────────────
    @Column(name = "no_of_layers")
    private Integer noOfLayers;

    @Column(name = "fabric_gsm")
    private Integer fabricGsm;

    @Column(name = "shift")
    private String shift;

    @Column(name = "cutting_overlap_mm")
    private Integer cuttingOverlapMm;

    @Column(name = "marker_efficiency_pct")
    private Double markerEfficiencyPct;

    @Column(name = "actual_wastage_pct")
    private Double actualWastagePct;

    @Column(name = "job_date")
    private LocalDate jobDate;

    @Column(name = "notes")
    private String notes;

    // ── Auto generated ────────────────────────────────────────────────────────
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}