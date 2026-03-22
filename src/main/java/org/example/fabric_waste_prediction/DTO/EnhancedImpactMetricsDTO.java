package org.example.fabric_waste_prediction.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.Map;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EnhancedImpactMetricsDTO extends org.example.fabric_waste_prediction.DTO.ImpactMetricsDTO {

    // ==================== ENHANCED SUSTAINABILITY METRICS ====================

    // Waste Management Metrics
    private Double recyclingEfficiency;
    private Double landfillDiversionRate;
    private Double compostingRate;
    private Double incinerationRate;

    // Circular Economy Metrics
    private Double circularEconomyIndex;
    private Double materialRecyclabilityScore;
    private Boolean closedLoopEligible;
    private Double recycledContentUsed;

    // Water & Energy Metrics
    private Double waterRecyclingSavings;
    private Double renewableEnergyContribution;
    private Double energyIntensityReduction;
    private Double waterFootprint;

    // Environmental Impact
    private Double biodiversityImpactScore;
    private Double ecosystemContribution;
    private Double carbonFootprintReduction;
    private Double methaneEmissionsAvoided;

    // Social Impact
    private Double socialImpactScore;
    private Integer greenJobsCreated;
    private Double communityBenefit;
    private String communityProgramImpact;

    // Economic Impact
    private Double circularEconomyRevenue;
    private Double costSavingsFromRecycling;
    private Double carbonCreditRevenue;
    private Double taxBenefits;

    // Compliance & Certification
    private Double esgScore;
    private String environmentalCompliance;
    private Boolean meetsGreenStandards;
    private String[] eligibleCertifications;
    private Boolean eligibleForCarbonCredits;
    private Boolean iso14001Compliant;

    // Detailed Breakdowns
    private Map<String, Double> sustainabilityBreakdown;
    private Map<String, Double> monthlyImprovement;
    private Map<String, String> recommendationsMap;  // ← Map version
    private List<String> improvementActions;

    // Risk Assessment
    private String sustainabilityRiskLevel;
    private Double climateRiskScore;
    private Boolean regulatoryRiskExists;

    // Rating - ADD THIS FIELD
    private String sustainabilityRating;  // ← ADD THIS

    // ==================== CALCULATION METHODS ====================

    public Double getOverallSustainabilityScore() {
        // Weighted average of all sustainability metrics
        double wasteScore = getSafeDouble(recyclingEfficiency) * 0.15;
        double circularScore = getSafeDouble(circularEconomyIndex) * 0.20;
        double energyScore = (getSafeDouble(renewableEnergyContribution) / 100) * 0.15;
        double socialScore = getSafeDouble(socialImpactScore) * 0.15;
        double esgScoreValue = getSafeDouble(esgScore) * 0.20;
        double baseScore = getSafeDouble(super.getSustainabilityScore()) * 0.15;

        return wasteScore + circularScore + energyScore + socialScore + esgScoreValue + baseScore;
    }

    // ADD THIS METHOD - calculates rating based on score
    public String calculateSustainabilityRating() {
        double score = getOverallSustainabilityScore();
        if (score >= 90) return "PLATINUM";
        if (score >= 80) return "GOLD";
        if (score >= 70) return "SILVER";
        if (score >= 60) return "BRONZE";
        if (score >= 50) return "BASIC";
        return "NEEDS_IMPROVEMENT";
    }

    // ADD THIS METHOD - getter for rating (will calculate if not set)
    public String getSustainabilityRating() {
        if (sustainabilityRating == null) {
            sustainabilityRating = calculateSustainabilityRating();
        }
        return sustainabilityRating;
    }

    // ADD THIS METHOD - setter for rating
    public void setSustainabilityRating(String sustainabilityRating) {
        this.sustainabilityRating = sustainabilityRating;
    }

    private Double getSafeDouble(Double value) {
        return value != null ? value : 0.0;
    }
}