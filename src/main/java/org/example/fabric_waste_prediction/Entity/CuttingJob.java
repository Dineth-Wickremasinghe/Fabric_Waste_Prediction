package org.example.fabric_waste_prediction.Entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
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
    @NotBlank(message = "Fabric type is required")
    @Column(name = "fabric_type")
    private String fabricType;

    @Column(name = "fabric_pattern")
    private String fabricPattern;

    @Column(name = "cutting_method")
    private String cuttingMethod;

    @DecimalMin(value = "0.0", message = "Marker loss must be between 0 and 100")
    @DecimalMax(value = "100.0", message = "Marker loss must be between 0 and 100")
    @Column(name = "marker_loss_pct")
    private Double markerLossPct;

    @Positive(message = "Pattern complexity must be a positive number")
    @Column(name = "pattern_complexity")
    private Double patternComplexity;

    @Positive(message = "Operator experience must be a positive number")
    @Column(name = "operator_experience")
    private Double operatorExperience;

    @DecimalMin(value = "0.0", message = "Predicted waste must be between 0 and 100")
    @DecimalMax(value = "100.0", message = "Predicted waste must be between 0 and 100")
    @Column(name = "predicted_waste_pct")
    private Double predictedWastePct;

    // ── From CuttingRiskRecord table ──────────────────────────────────────────
    @NotNull(message = "Number of layers is required")
    @Min(value = 1, message = "Layers must be at least 1")
    @Max(value = 500, message = "Layers cannot exceed 500")
    @Column(name = "no_of_layers")
    private Integer noOfLayers;

    @Positive(message = "Fabric GSM must be a positive number")
    @Column(name = "fabric_gsm")
    private Integer fabricGsm;

    @Column(name = "shift")
    private String shift;

    @Positive(message = "Cutting overlap must be a positive number")
    @Column(name = "cutting_overlap_mm")
    private Integer cuttingOverlapMm;

    @DecimalMin(value = "0.0", message = "Marker efficiency must be between 0 and 100")
    @DecimalMax(value = "100.0", message = "Marker efficiency must be between 0 and 100")
    @Column(name = "marker_efficiency_pct")
    private Double markerEfficiencyPct;

    @DecimalMin(value = "0.0", message = "Actual wastage must be between 0 and 100")
    @DecimalMax(value = "100.0", message = "Actual wastage must be between 0 and 100")
    @Column(name = "actual_wastage_pct")
    private Double actualWastagePct;

    @NotNull(message = "Job date is required")
    @PastOrPresent(message = "Job date cannot be a future date")
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