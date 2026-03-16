package org.example.fabric_waste_prediction.Service;

import org.example.fabric_waste_prediction.DTO.ImpactMetricsDTO;
import org.example.fabric_waste_prediction.DTO.PredictionInputDTO;
import org.example.fabric_waste_prediction.Entity.SustainabilityMetrics;
import org.example.fabric_waste_prediction.Repository.DailyWastageRepository;
import org.example.fabric_waste_prediction.Repository.SustainabilityRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.time.LocalDate;

@Service
public class ImpactCalculatorService {

    @Value("${sustainability.factors.co2-per-kg-waste:2.5}")
    private Double co2Factor;

    @Value("${sustainability.factors.water-per-kg-waste:150}")
    private Double waterFactor;

    @Value("${sustainability.factors.energy-per-kg-waste:45}")
    private Double energyFactor;

    @Value("${sustainability.factors.cost-per-kg-fabric:12.5}")
    private Double costFactorUSD;

    @Value("${sustainability.factors.landfill-per-kg-waste:0.8}")
    private Double landfillFactor;

    @Value("${sustainability.currency.usd-to-lkr:300}")
    private Double usdToLkrRate;

    private final SustainabilityRepository sustainabilityRepository;
    private final DailyWastageRepository dailyWastageRepository;

    public ImpactCalculatorService(SustainabilityRepository sustainabilityRepository,
                                   DailyWastageRepository dailyWastageRepository) {
        this.sustainabilityRepository = sustainabilityRepository;
        this.dailyWastageRepository = dailyWastageRepository;
    }

    public ImpactMetricsDTO calculateImpact(PredictionInputDTO input, Double predictedWastage) {
        ImpactMetricsDTO impact = new ImpactMetricsDTO();

        // Calculate fabric usage and waste
        Double fabricNeeded = input.getOrderQuantity() / (1 - (predictedWastage / 100));
        Double fabricWaste = fabricNeeded - input.getOrderQuantity();
        Double wasteKg = fabricWaste * getFabricDensityFactor(input.getFabricType());

        // Calculate impacts
        impact.setCo2SavedKg(roundToTwoDecimals(wasteKg * co2Factor));
        impact.setWaterSavedL(roundToTwoDecimals(wasteKg * waterFactor));
        impact.setFabricSavedKg(roundToTwoDecimals(wasteKg));
        impact.setEnergySavedKwh(roundToTwoDecimals(wasteKg * energyFactor));

        // Calculate cost in LKR
        Double costInUSD = wasteKg * costFactorUSD;
        Double costInLKR = costInUSD * usdToLkrRate;
        impact.setCostSavedLkr(roundToTwoDecimals(costInLKR));

        impact.setLandfillAvoidedKg(roundToTwoDecimals(wasteKg * landfillFactor));

        impact.setPredictedWastagePct(roundToTwoDecimals(predictedWastage));
        impact.setFabricType(input.getFabricType());
        impact.setOrderQuantity(input.getOrderQuantity());
        impact.setCalculationDate(LocalDate.now());

        // Calculate sustainability score
        impact.setSustainabilityScore(roundToTwoDecimals(calculateSustainabilityScore(impact)));

        // Determine risk level
        impact.setRiskLevel(determineRiskLevel(predictedWastage, input.getFabricType()));

        // Generate recommendations
        impact.setRecommendations(generateRecommendations(input, predictedWastage));

        return impact;
    }

    private Double roundToTwoDecimals(Double value) {
        if (value == null) return 0.0;
        return Math.round(value * 100.0) / 100.0;
    }

    public Double calculateSustainabilityScore(ImpactMetricsDTO impact) {
        Double predictedWastage = getSafeDouble(impact.getPredictedWastagePct());
        Double wasteScore = 100 - (predictedWastage * 2);
        wasteScore = Math.max(0, Math.min(100, wasteScore));

        Double co2Saved = getSafeDouble(impact.getCo2SavedKg());
        Double waterSaved = getSafeDouble(impact.getWaterSavedL());
        Double energySaved = getSafeDouble(impact.getEnergySavedKwh());
        Double costSaved = getSafeDouble(impact.getCostSavedLkr()) / usdToLkrRate;

        Double impactScore = (co2Saved + waterSaved/100 + energySaved + costSaved) / 100;
        impactScore = Math.min(100, impactScore * 10);

        return (wasteScore * 0.6) + (impactScore * 0.4);
    }

    private Double getSafeDouble(Double value) {
        return value != null ? value : 0.0;
    }

    private String determineRiskLevel(Double wastage, String fabricType) {
        Double safeWastage = getSafeDouble(wastage);
        if (safeWastage > 15) return "HIGH";
        if (safeWastage > 10) return "MEDIUM";
        if (safeWastage > 5) return "LOW";
        return "MINIMAL";
    }

    private String generateRecommendations(PredictionInputDTO input, Double predictedWastage) {
        StringBuilder recommendations = new StringBuilder();
        Double safeWastage = getSafeDouble(predictedWastage);

        if (safeWastage > 15) {
            recommendations.append("High wastage detected. Consider: ");
            recommendations.append("1. Increasing marker efficiency; ");
            recommendations.append("2. Optimizing cutting pattern; ");
            recommendations.append("3. Using automated cutting if manual; ");
        } else if (safeWastage > 10) {
            recommendations.append("Moderate wastage. Suggestions: ");
            recommendations.append("1. Review cutting parameters; ");
            recommendations.append("2. Check fabric quality; ");
        } else {
            recommendations.append("Good efficiency. Continue current practices.");
        }

        if (input.getFabricType() != null) {
            if ("Cotton".equalsIgnoreCase(input.getFabricType())) {
                recommendations.append(" Cotton shows best efficiency with automated cutting.");
            } else if ("Silk".equalsIgnoreCase(input.getFabricType())) {
                recommendations.append(" Silk requires careful handling to minimize waste.");
            }
        }

        return recommendations.toString();
    }

    private Double getFabricDensityFactor(String fabricType) {
        if (fabricType == null) return 0.25;
        return switch (fabricType.toLowerCase()) {
            case "cotton" -> 0.25;
            case "silk" -> 0.15;
            case "linen" -> 0.22;
            case "polyester" -> 0.28;
            case "denim" -> 0.35;
            default -> 0.25;
        };
    }

    public SustainabilityMetrics saveDailyMetrics(ImpactMetricsDTO impact) {
        // Always create a new record - don't check for existing
        SustainabilityMetrics metrics = new SustainabilityMetrics();

        metrics.setMetricDate(LocalDate.now());
        metrics.setCarbonAvoidedTons(getSafeDouble(impact.getCo2SavedKg()) / 1000);
        metrics.setWaterSavedL(getSafeDouble(impact.getWaterSavedL()));
        metrics.setWasteReducedTons(getSafeDouble(impact.getFabricSavedKg()) / 1000);
        metrics.setEnergySavedKwh(getSafeDouble(impact.getEnergySavedKwh()));
        metrics.setCostSavedLkr(getSafeDouble(impact.getCostSavedLkr()));
        metrics.setLandfillAvoidedKg(getSafeDouble(impact.getLandfillAvoidedKg()));
        metrics.setFabricSavedKg(getSafeDouble(impact.getFabricSavedKg()));
        metrics.setSustainabilityScore(getSafeDouble(impact.getSustainabilityScore()));

        // Add the new fields
        metrics.setFabricType(impact.getFabricType());
        metrics.setOrderQuantity(impact.getOrderQuantity());
        metrics.setPredictedWastage(impact.getPredictedWastagePct());

        metrics.setCreatedAt(LocalDate.now());

        return sustainabilityRepository.save(metrics);
    }
}