package org.example.fabric_waste_prediction.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDate;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImpactMetricsDTO {

    // Core metrics
    private Double co2SavedKg = 0.0;
    private Double waterSavedL = 0.0;
    private Double fabricSavedKg = 0.0;
    private Double energySavedKwh = 0.0;
    private Double costSavedLkr = 0.0;  // Changed from costSavedUsd to costSavedLkr
    private Double landfillAvoidedKg = 0.0;

    // Derived metrics
    private Double predictedWastagePct = 0.0;
    private Double actualWastagePct = 0.0;
    private Double efficiencyGain = 0.0;

    // Context
    private String fabricType;
    private Double orderQuantity = 0.0;
    private LocalDate calculationDate;

    // Sustainability score (0-100)
    private Double sustainabilityScore = 0.0;

    // Breakdown by category
    private Map<String, Double> impactBreakdown;

    // Risk assessment
    private String riskLevel = "MINIMAL";
    private String recommendations = "";

    // Trend data
    private Map<String, Double> weeklyTrend;
    private Map<String, Double> monthlyTrend;
}