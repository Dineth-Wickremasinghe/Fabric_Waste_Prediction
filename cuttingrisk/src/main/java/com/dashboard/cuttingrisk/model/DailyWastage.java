package com.dashboard.cuttingrisk.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "daily_wastage")
public class DailyWastage {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "tracking_date")
    private LocalDate trackingDate;

    @Column(name = "predicted_wastage_pct")
    private Double predictedWastagePct;

    @Column(name = "actual_wastage_pct")
    private Double actualWastagePct;

    // Getters
    public UUID getId() { return id; }
    public LocalDate getTrackingDate() { return trackingDate; }
    public Double getPredictedWastagePct() { return predictedWastagePct; }
    public Double getActualWastagePct() { return actualWastagePct; }

    // Setters
    public void setTrackingDate(LocalDate trackingDate) { this.trackingDate = trackingDate; }
    public void setPredictedWastagePct(Double predictedWastagePct) { this.predictedWastagePct = predictedWastagePct; }
    public void setActualWastagePct(Double actualWastagePct) { this.actualWastagePct = actualWastagePct; }
}