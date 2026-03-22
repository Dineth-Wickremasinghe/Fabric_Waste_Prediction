package org.example.fabric_waste_prediction.Entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "sustainability_inputs")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SustainabilityInput {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Simple form fields
    @Column(name = "recycling_rate", nullable = false)
    private Double recyclingRate;

    @Column(name = "waste_disposal", nullable = false, length = 20)
    private String wasteDisposal;

    @Column(name = "energy_source", nullable = false, length = 20)
    private String energySource;

    @Column(name = "renewable_percentage", nullable = false)
    private Double renewablePercentage;

    @Column(name = "treat_wastewater", nullable = false)
    private Boolean treatWastewater;

    @Column(name = "certification", length = 20)
    private String certification;

    @Column(name = "follows_laws", nullable = false)
    private Boolean followsLaws;

    // Calculated fields
    @Column(name = "sustainability_score")
    private Double sustainabilityScore;

    @Column(name = "rating", length = 20)
    private String rating;

    @Column(name = "waste_score")
    private Double wasteScore;

    @Column(name = "energy_score")
    private Double energyScore;

    @Column(name = "water_score")
    private Double waterScore;

    @Column(name = "compliance_score")
    private Double complianceScore;

    @Column(name = "certification_bonus")
    private Double certificationBonus;

    // One-to-one relationship with cascade
    @OneToOne(mappedBy = "input", cascade = CascadeType.ALL, orphanRemoval = true)
    private EnhancedSustainabilityMetrics metrics;

    // Metadata
    @Column(name = "user_id")
    private String userId;

    @Column(name = "session_id")
    private String sessionId;

    @Column(name = "notes")
    private String notes;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}