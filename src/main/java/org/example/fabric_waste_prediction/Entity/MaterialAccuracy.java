package org.example.fabric_waste_prediction.Entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "material_accuracy")
public class MaterialAccuracy {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "material_id")
    private Material material;

    @Column(name = "accuracy_pct")
    private Double accuracyPct;

    @Column(name = "needs_optimization")
    private Boolean needsOptimization;

    @Column(name = "recorded_date")
    private LocalDate recordedDate;

    // Getters
    public UUID getId()                  { return id; }
    public Material getMaterial()        { return material; }
    public Double getAccuracyPct()       { return accuracyPct; }
    public Boolean getNeedsOptimization(){ return needsOptimization; }
    public LocalDate getRecordedDate()   { return recordedDate; }
}