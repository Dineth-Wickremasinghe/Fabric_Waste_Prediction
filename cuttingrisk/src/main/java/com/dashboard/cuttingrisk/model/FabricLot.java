package com.dashboard.cuttingrisk.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "fabric_lots")
public class FabricLot {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "lot_number")
    private String lotNumber;

    @ManyToOne
    @JoinColumn(name = "material_id")
    private Material material;

    @Column(name = "fabric_width_mm")
    private Integer fabricWidthMm;

    @Column(name = "fabric_gsm")
    private Integer fabricGsm;

    @Column(name = "fabric_pattern")
    @Enumerated(EnumType.STRING)
    private FabricPattern fabricPattern;

    @Column(name = "received_date")
    private LocalDate receivedDate;

    // Getters
    public UUID getId()                  { return id; }
    public String getLotNumber()         { return lotNumber; }
    public Material getMaterial()        { return material; }
    public Integer getFabricWidthMm()    { return fabricWidthMm; }
    public Integer getFabricGsm()        { return fabricGsm; }
    public FabricPattern getFabricPattern() { return fabricPattern; }
    public LocalDate getReceivedDate()   { return receivedDate; }
}