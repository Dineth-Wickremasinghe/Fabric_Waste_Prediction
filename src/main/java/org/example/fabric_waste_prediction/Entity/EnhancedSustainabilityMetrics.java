package org.example.fabric_waste_prediction.Entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "enhanced_sustainability_metrics")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EnhancedSustainabilityMetrics {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "input_id")
    private SustainabilityInput input;

    // Waste Management Metrics
    @Column(name = "recycling_efficiency")
    private Double recyclingEfficiency;

    @Column(name = "landfill_diversion_rate")
    private Double landfillDiversionRate;

    @Column(name = "composting_rate")
    private Double compostingRate;

    @Column(name = "incineration_rate")
    private Double incinerationRate;

    // Circular Economy Metrics
    @Column(name = "circular_economy_index")
    private Double circularEconomyIndex;

    @Column(name = "material_recyclability_score")
    private Double materialRecyclabilityScore;

    @Column(name = "closed_loop_eligible")
    private Boolean closedLoopEligible;

    @Column(name = "recycled_content_used")
    private Double recycledContentUsed;

    // Water & Energy Metrics
    @Column(name = "water_recycling_savings")
    private Double waterRecyclingSavings;

    @Column(name = "renewable_energy_contribution")
    private Double renewableEnergyContribution;

    @Column(name = "energy_intensity_reduction")
    private Double energyIntensityReduction;

    @Column(name = "water_footprint")
    private Double waterFootprint;

    // Environmental Impact
    @Column(name = "biodiversity_impact_score")
    private Double biodiversityImpactScore;

    @Column(name = "ecosystem_contribution")
    private Double ecosystemContribution;

    @Column(name = "carbon_footprint_reduction")
    private Double carbonFootprintReduction;

    @Column(name = "methane_emissions_avoided")
    private Double methaneEmissionsAvoided;

    // Social Impact
    @Column(name = "social_impact_score")
    private Double socialImpactScore;

    @Column(name = "green_jobs_created")
    private Integer greenJobsCreated;

    @Column(name = "community_benefit")
    private Double communityBenefit;

    @Column(name = "community_program_impact", columnDefinition = "TEXT")
    private String communityProgramImpact;

    // Economic Impact
    @Column(name = "circular_economy_revenue")
    private Double circularEconomyRevenue;

    @Column(name = "cost_savings_from_recycling")
    private Double costSavingsFromRecycling;

    @Column(name = "carbon_credit_revenue")
    private Double carbonCreditRevenue;

    @Column(name = "tax_benefits")
    private Double taxBenefits;

    // Compliance & Certification
    @Column(name = "esg_score")
    private Double esgScore;

    @Column(name = "environmental_compliance", length = 50)
    private String environmentalCompliance;

    @Column(name = "meets_green_standards")
    private Boolean meetsGreenStandards;

    @Column(name = "eligible_certifications", columnDefinition = "TEXT[]")
    private String[] eligibleCertifications;

    @Column(name = "eligible_for_carbon_credits")
    private Boolean eligibleForCarbonCredits;

    @Column(name = "iso14001_compliant")
    private Boolean iso14001Compliant;

    // Final Scores
    @Column(name = "overall_sustainability_score")
    private Double overallSustainabilityScore;

    @Column(name = "sustainability_rating", length = 20)
    private String sustainabilityRating;

    @Column(name = "sustainability_risk_level", length = 20)
    private String sustainabilityRiskLevel;

    @Column(name = "climate_risk_score")
    private Double climateRiskScore;

    @Column(name = "regulatory_risk_exists")
    private Boolean regulatoryRiskExists;

    // Metadata
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}