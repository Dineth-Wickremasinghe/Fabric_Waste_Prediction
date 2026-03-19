package com.dashboard.cuttingrisk.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "cutting_jobs")
public class CuttingJob {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "job_id")
    private String jobId;

    @ManyToOne
    @JoinColumn(name = "material_id")
    private Material material;

    @ManyToOne
    @JoinColumn(name = "fabric_lot_id")
    private FabricLot fabricLot;

    @ManyToOne
    @JoinColumn(name = "operator_id")
    private User operator;

    @Column(name = "cutting_overlap_mm")
    private Integer cuttingOverlapMm;

    @Column(name = "no_of_layers")
    private Integer noOfLayers;

    @Column(name = "cutting_method")
    @Enumerated(EnumType.STRING)
    private CuttingMethod cuttingMethod;

    @Column(name = "shift")
    @Enumerated(EnumType.STRING)
    private ShiftType shift;

    @Column(name = "actual_wastage_pct")
    private Double actualWastagePct;

    @Column(name = "marker_efficiency_pct")
    private Double markerEfficiencyPct;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private JobStatus status;

    @Column(name = "job_date")
    private LocalDate jobDate;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // Getters
    public UUID getId()                    { return id; }
    public String getJobId()               { return jobId; }
    public Material getMaterial()          { return material; }
    public FabricLot getFabricLot()        { return fabricLot; }
    public User getOperator()              { return operator; }
    public Integer getCuttingOverlapMm()   { return cuttingOverlapMm; }
    public Integer getNoOfLayers()         { return noOfLayers; }
    public CuttingMethod getCuttingMethod(){ return cuttingMethod; }
    public ShiftType getShift()            { return shift; }
    public Double getActualWastagePct()    { return actualWastagePct; }
    public Double getMarkerEfficiencyPct() { return markerEfficiencyPct; }
    public JobStatus getStatus()           { return status; }
    public LocalDate getJobDate()          { return jobDate; }
}