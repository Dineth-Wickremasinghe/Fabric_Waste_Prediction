package org.example.fabric_waste_prediction.Entity;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "cutting_risk_records")
public class CuttingRiskRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "prediction_id", nullable = false)
    private DailyWastage prediction;

    @Column(name = "no_of_layers")
    private Integer noOfLayers;

    @Column(name = "fabric_gsm")
    private Integer fabricGsm;

    @Column(name = "cutting_method")
    @Enumerated(EnumType.STRING)
    private CuttingMethod cuttingMethod;

    @Column(name = "shift")
    @Enumerated(EnumType.STRING)
    private ShiftType shift;

    @Column(name = "cutting_overlap_mm")
    private Integer cuttingOverlapMm;

    @Column(name = "marker_efficiency_pct")
    private Double markerEfficiencyPct;

    @Column(name = "actual_wastage_pct")
    private Double actualWastagePct;

    @ManyToOne
    @JoinColumn(name = "operator_id")
    private user operator;

    @Column(name = "job_date")
    private LocalDate jobDate;

    @Column(name = "notes")
    private String notes;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // ── Getters ──
    public UUID getId()                     { return id; }
    public DailyWastage getPrediction()     { return prediction; }
    public Integer getNoOfLayers()          { return noOfLayers; }
    public Integer getFabricGsm()           { return fabricGsm; }
    public CuttingMethod getCuttingMethod() { return cuttingMethod; }
    public ShiftType getShift()             { return shift; }
    public Integer getCuttingOverlapMm()    { return cuttingOverlapMm; }
    public Double getMarkerEfficiencyPct()  { return markerEfficiencyPct; }
    public Double getActualWastagePct()     { return actualWastagePct; }
    public user getOperator()               { return operator; }
    public LocalDate getJobDate()           { return jobDate; }
    public String getNotes()                { return notes; }

    // ── Setters ──
    public void setPrediction(DailyWastage p)     { this.prediction = p; }
    public void setNoOfLayers(Integer n)           { this.noOfLayers = n; }
    public void setFabricGsm(Integer g)            { this.fabricGsm = g; }
    public void setCuttingMethod(CuttingMethod c)  { this.cuttingMethod = c; }
    public void setShift(ShiftType s)              { this.shift = s; }
    public void setCuttingOverlapMm(Integer o)     { this.cuttingOverlapMm = o; }
    public void setMarkerEfficiencyPct(Double m)   { this.markerEfficiencyPct = m; }
    public void setActualWastagePct(Double a)      { this.actualWastagePct = a; }
    public void setOperator(user u)                { this.operator = u; }
    public void setJobDate(LocalDate d)            { this.jobDate = d; }
    public void setNotes(String n)                 { this.notes = n; }
}