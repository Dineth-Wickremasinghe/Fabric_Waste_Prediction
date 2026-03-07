package org.example.fabric_waste_prediction.Entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "cutting_jobs")
@Data
public class CuttingJob {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String jobId;

    @Column(nullable = false)
    private String fabricLotId;

    @Column(nullable = false)
    private String materialId;

    @Column(nullable = false)
    private String operatorId;

    @Column(nullable = false)
    private Double cuttingOverlapMm;

    @Column(nullable = false)
    private Integer noOfLayers;

    @Column(nullable = false)
    private String cuttingMethod;

    @Column(nullable = false)
    private String shift;

    @Column(nullable = false)
    private Double actualWastagePct;

    @Column(nullable = false)
    private Double predictedWastePct;

    @Column(nullable = false)
    private Double markerEfficiencyPct;

    @Column(nullable = false)
    private String status;

    @Column(nullable = false)
    private LocalDate jobDate;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
