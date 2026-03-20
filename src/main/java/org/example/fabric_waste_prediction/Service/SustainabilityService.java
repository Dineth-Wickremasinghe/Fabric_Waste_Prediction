package org.example.fabric_waste_prediction.Service;

import org.example.fabric_waste_prediction.DTO.FabricBreakdownDTO;
import org.example.fabric_waste_prediction.DTO.ImpactMetricsDTO;
import org.example.fabric_waste_prediction.DTO.PredictionInputDTO;
import org.example.fabric_waste_prediction.Entity.FabricRisk;
import org.example.fabric_waste_prediction.Entity.SustainabilityMetrics;
import org.example.fabric_waste_prediction.Repository.DailyWastageRepository;
import org.example.fabric_waste_prediction.Repository.SustainabilityRepository;
import org.example.fabric_waste_prediction.Repository.WasteTrackingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class SustainabilityService {

    private final ImpactCalculatorService impactCalculator;
    private final SustainabilityRepository sustainabilityRepository;
    private final DailyWastageRepository dailyWastageRepository;
    private final WasteTrackingRepository wasteTrackingRepository;

    public SustainabilityService(ImpactCalculatorService impactCalculator,
                                 SustainabilityRepository sustainabilityRepository,
                                 DailyWastageRepository dailyWastageRepository,
                                 WasteTrackingRepository wasteTrackingRepository) {
        this.impactCalculator = impactCalculator;
        this.sustainabilityRepository = sustainabilityRepository;
        this.dailyWastageRepository = dailyWastageRepository;
        this.wasteTrackingRepository = wasteTrackingRepository;
    }

    @Transactional
    public ImpactMetricsDTO processPrediction(PredictionInputDTO input, Double predictedWastage) {
        // Calculate impacts
        ImpactMetricsDTO impact = impactCalculator.calculateImpact(input, predictedWastage);

        // Save metrics
        sustainabilityRepository.save(impactCalculator.saveDailyMetrics(impact));

        return impact;
    }

    public List<FabricBreakdownDTO> getFabricBreakdown() {
        List<FabricBreakdownDTO> breakdown = new ArrayList<>();
        LocalDate thirtyDaysAgo = LocalDate.now().minusDays(30);

        try {
            List<Object[]> stats = dailyWastageRepository.getFabricWastageStats(thirtyDaysAgo);

            if (stats != null && !stats.isEmpty()) {
                for (Object[] stat : stats) {
                    if (stat != null && stat.length >= 4) {
                        FabricBreakdownDTO dto = new FabricBreakdownDTO();
                        dto.setFabricType(stat[0] != null ? stat[0].toString() : "Unknown");

                        // Convert to Double safely
                        if (stat[1] != null) {
                            dto.setWastagePercentage(((Number) stat[1]).doubleValue());
                        } else {
                            dto.setWastagePercentage(0.0);
                        }

                        if (stat[2] != null) {
                            dto.setTotalWasteKg(((Number) stat[2]).doubleValue());
                        } else {
                            dto.setTotalWasteKg(0.0);
                        }

                        if (stat[3] != null) {
                            dto.setJobCount(((Number) stat[3]).intValue());
                        } else {
                            dto.setJobCount(0);
                        }

                        // Determine risk level
                        if (dto.getWastagePercentage() > 15) {
                            dto.setRiskLevel("HIGH");
                            dto.setColorCode("#dc3545");
                        } else if (dto.getWastagePercentage() > 10) {
                            dto.setRiskLevel("MEDIUM");
                            dto.setColorCode("#ffc107");
                        } else if (dto.getWastagePercentage() > 5) {
                            dto.setRiskLevel("LOW");
                            dto.setColorCode("#28a745");
                        } else {
                            dto.setRiskLevel("MINIMAL");
                            dto.setColorCode("#20c997");
                        }

                        // Calculate impacts
                        dto.setCo2Impact(dto.getTotalWasteKg() * 2.5);
                        dto.setWaterImpact(dto.getTotalWasteKg() * 150);
                        dto.setCostImpact(dto.getTotalWasteKg() * 12.5);

                        // Generate optimization tips
                        dto.setOptimizationTips(generateOptimizationTips(dto));

                        breakdown.add(dto);
                    }
                }
            }
        } catch (Exception e) {
            // Log error and return empty list
            e.printStackTrace();
        }

        // If no data, return sample data for testing
        if (breakdown.isEmpty()) {
            breakdown = getSampleFabricBreakdown();
        }

        return breakdown.stream()
                .sorted(Comparator.comparing(FabricBreakdownDTO::getWastagePercentage).reversed())
                .collect(Collectors.toList());
    }

    private List<FabricBreakdownDTO> getSampleFabricBreakdown() {
        List<FabricBreakdownDTO> samples = new ArrayList<>();

        String[] fabrics = {"Cotton", "Linen", "Polyester", "Denim", "Silk", "Viscose", "Rayon"};
        double[] wastages = {8.5, 12.3, 7.2, 15.8, 10.1, 9.4, 11.2};
        double[] wasteKg = {75.5, 62.3, 45.8, 98.2, 38.5, 52.7, 48.9};
        int[] jobCounts = {42, 28, 35, 55, 22, 31, 27};

        for (int i = 0; i < fabrics.length; i++) {
            FabricBreakdownDTO dto = new FabricBreakdownDTO();
            dto.setFabricType(fabrics[i]);
            dto.setWastagePercentage(wastages[i]);
            dto.setTotalWasteKg(wasteKg[i]);
            dto.setJobCount(jobCounts[i]);

            // Determine risk level
            if (wastages[i] > 15) {
                dto.setRiskLevel("HIGH");
                dto.setColorCode("#dc3545");
            } else if (wastages[i] > 10) {
                dto.setRiskLevel("MEDIUM");
                dto.setColorCode("#ffc107");
            } else if (wastages[i] > 5) {
                dto.setRiskLevel("LOW");
                dto.setColorCode("#28a745");
            } else {
                dto.setRiskLevel("MINIMAL");
                dto.setColorCode("#20c997");
            }

            dto.setCo2Impact(dto.getTotalWasteKg() * 2.5);
            dto.setWaterImpact(dto.getTotalWasteKg() * 150);
            dto.setCostImpact(dto.getTotalWasteKg() * 12.5);
            dto.setOptimizationTips(generateOptimizationTips(dto));

            samples.add(dto);
        }

        return samples;
    }

    private List<String> generateOptimizationTips(FabricBreakdownDTO fabric) {
        List<String> tips = new ArrayList<>();

        if (fabric.getWastagePercentage() > 15) {
            tips.add("Critical: Review cutting parameters immediately");
            tips.add("Consider using automated cutting method");
            tips.add("Check fabric quality before cutting");
        } else if (fabric.getWastagePercentage() > 10) {
            tips.add("Optimize marker efficiency");
            tips.add("Train operators on best practices");
        } else {
            tips.add("Current practices are effective");
            tips.add("Monitor for any deviations");
        }

        // Fabric-specific tips
        if (fabric.getFabricType() != null) {
            switch (fabric.getFabricType().toLowerCase()) {
                case "cotton":
                    tips.add("Cotton: Use high-speed cutting for better results");
                    break;
                case "denim":
                    tips.add("Denim: Ensure proper tension during cutting");
                    break;
                case "silk":
                    tips.add("Silk: Use sharp blades to prevent fraying");
                    break;
                case "linen":
                    tips.add("Linen: Pre-treat to minimize shrinkage");
                    break;
                case "polyester":
                    tips.add("Polyester: Use heat cutting to prevent fraying");
                    break;
            }
        }

        return tips;
    }

    public Map<String, Object> getDashboardSummary() {
        Map<String, Object> summary = new HashMap<>();
        LocalDate today = LocalDate.now();
        LocalDate weekAgo = today.minusDays(7);
        LocalDate monthAgo = today.minusDays(30);

        try {
            // Get totals with null safety and rounding
            Double totalWaste = sustainabilityRepository.getTotalWasteReduced(monthAgo, today);
            Double totalCarbon = sustainabilityRepository.getTotalCarbonAvoided(monthAgo, today);
            Double totalWater = sustainabilityRepository.getTotalWaterSaved(monthAgo, today);
            Double totalCost = sustainabilityRepository.getTotalCostSaved(monthAgo, today);

            summary.put("totalWasteReduced_tons", totalWaste != null ? totalWaste : 2.8);
            summary.put("totalCarbonAvoided_tons", totalCarbon != null ? totalCarbon : 12.4);

            summary.put("totalWasteReduced", totalWaste != null ? Math.round(totalWaste *1000 * 100.0) / 100.0 : 2800.0);
            summary.put("totalCarbonAvoided", totalCarbon != null ? Math.round(totalCarbon *1000* 100.0) / 100.0 : 12400.0);
            summary.put("totalWaterSaved", totalWater != null ? Math.round(totalWater * 100.0) / 100.0 : 45200.0);
            summary.put("totalCostSaved", totalCost != null ? Math.round(totalCost * 100.0) / 100.0 : 12500.0);

            // Get recent metrics (last 7 days) - KEEPING AS "LAST 7 DAYS"
            List<SustainabilityMetrics> recentMetrics = sustainabilityRepository.findLast7Days();
            summary.put("recentMetrics", recentMetrics != null ? recentMetrics : new ArrayList<>());

            // Get average scores
            Double avgScore30 = sustainabilityRepository.getAverageScoreBetween(monthAgo, today);
            Double avgScore7 = sustainabilityRepository.getAverageScoreBetween(weekAgo, today);

            summary.put("avgScore30Days", avgScore30 != null ? Math.round(avgScore30 * 100.0) / 100.0 : 75.5);
            summary.put("avgScore7Days", avgScore7 != null ? Math.round(avgScore7 * 100.0) / 100.0 : 78.2);

            // Get fabric breakdown
            summary.put("fabricBreakdown", getFabricBreakdown());

            // Get waste destinations
            List<Object[]> wasteDestinations = wasteTrackingRepository.getWasteDestinationSummary(monthAgo, today);
            summary.put("wasteDestinations", wasteDestinations != null ? wasteDestinations : new ArrayList<>());

        } catch (Exception e) {
            e.printStackTrace();
            // Fallback to sample data if database queries fail
            summary.put("totalWasteReduced", 2800.0);
            summary.put("totalCarbonAvoided", 12400.0);
            summary.put("totalWaterSaved", 45200.0);
            summary.put("totalCostSaved", 12500.0);
            summary.put("avgScore30Days", 75.5);
            summary.put("avgScore7Days", 78.2);
            summary.put("fabricBreakdown", getSampleFabricBreakdown());
            summary.put("recentMetrics", new ArrayList<>());
            summary.put("wasteDestinations", new ArrayList<>());
        }

        // Calculate overall sustainability score
        Double overallScore = calculateOverallSustainabilityScore(summary);
        summary.put("overallSustainabilityScore", overallScore != null ? Math.round(overallScore * 100.0) / 100.0 : 85.0);

        return summary;
    }

    private Double calculateOverallSustainabilityScore(Map<String, Object> summary) {
        try {
            Double wasteReduced = getDoubleValue(summary.get("totalWasteReduced_tons"));
            Double carbonAvoided = getDoubleValue(summary.get("totalCarbonAvoided_tons"));
            Double waterSaved = getDoubleValue(summary.get("totalWaterSaved"));
            Double avgScore = getDoubleValue(summary.get("avgScore30Days"));

            // Ensure we have default values
            wasteReduced = wasteReduced != null ? wasteReduced : 0.0;
            carbonAvoided = carbonAvoided != null ? carbonAvoided : 0.0;
            waterSaved = waterSaved != null ? waterSaved : 0.0;
            avgScore = avgScore != null ? avgScore : 75.0;

            // Weighted score calculation
            Double wasteScore = Math.min(100.0, wasteReduced * 10);
            Double carbonScore = Math.min(100.0, carbonAvoided * 5);
            Double waterScore = Math.min(100.0, waterSaved / 1000);

            return (wasteScore * 0.3) + (carbonScore * 0.3) + (waterScore * 0.2) + (avgScore * 0.2);
        } catch (Exception e) {
            return 85.0; // Default score
        }
    }

    private Double getDoubleValue(Object obj) {
        if (obj == null) return 0.0;
        if (obj instanceof Number) return ((Number) obj).doubleValue();
        if (obj instanceof String) {
            try {
                return Double.parseDouble((String) obj);
            } catch (NumberFormatException e) {
                return 0.0;
            }
        }
        return 0.0;
    }

    public List<FabricRisk> getHighRiskFabrics() {
        List<FabricBreakdownDTO> breakdown = getFabricBreakdown();
        List<FabricRisk> risks = new ArrayList<>();

        for (FabricBreakdownDTO fabric : breakdown) {
            if ("HIGH".equals(fabric.getRiskLevel()) || "MEDIUM".equals(fabric.getRiskLevel())) {
                FabricRisk risk = new FabricRisk();
                risk.setFabricType(fabric.getFabricType());
                risk.setAvgWastagePct(fabric.getWastagePercentage());
                risk.setRiskLevel(fabric.getRiskLevel());
                risk.setTotalJobs(fabric.getJobCount());
                risk.setTotalWasteKg(fabric.getTotalWasteKg());
                risk.setOptimizationNeeded(fabric.getWastagePercentage() > 12);
                risk.setRecordedDate(LocalDate.now());
                risks.add(risk);
            }
        }

        return risks;
    }
}