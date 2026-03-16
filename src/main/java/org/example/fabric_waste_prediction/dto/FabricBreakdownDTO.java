package org.example.fabric_waste_prediction.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FabricBreakdownDTO {

    private String fabricType;
    private Double totalUsageKg;
    private Double totalWasteKg;
    private Double wastagePercentage;
    private String riskLevel;
    private Integer jobCount;
    private Double sustainabilityContribution;

    // Color code for UI
    private String colorCode;

    // Optimization suggestions
    private List<String> optimizationTips;

    // Historical trend
    private Map<String, Double> monthlyTrend;

    // Impact metrics for this fabric
    private Double co2Impact;
    private Double waterImpact;
    private Double costImpact;
}