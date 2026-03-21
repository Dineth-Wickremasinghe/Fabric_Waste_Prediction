package org.example.fabric_waste_prediction.Entity;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "materials")
public class Material {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "name")
    @Enumerated(EnumType.STRING)
    private FabricType name;

    @Column(name = "base_wastage_factor")
    private Double baseWastageFactor;

    // Getters
    public UUID getId()                   { return id; }
    public FabricType getName()           { return name; }
    public Double getBaseWastageFactor()  { return baseWastageFactor; }
}