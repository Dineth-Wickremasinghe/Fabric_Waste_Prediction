package org.example.fabric_waste_prediction.Service;

import org.example.fabric_waste_prediction.DTO.*;
import org.example.fabric_waste_prediction.Entity.*;
import org.example.fabric_waste_prediction.Repository.*;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class EnhancedSustainabilityService {

    private final SustainabilityRepository sustainabilityRepository;
    private final ImpactCalculatorService impactCalculator;
    private final SustainabilityInputRepository sustainabilityInputRepository;
    private final EnhancedSustainabilityMetricsRepository metricsRepository;

    @Value("${sustainability.currency.usd-to-lkr:300}")
    private Double usdToLkrRate;

    public EnhancedSustainabilityService(SustainabilityRepository sustainabilityRepository,
                                         ImpactCalculatorService impactCalculator,
                                         SustainabilityInputRepository sustainabilityInputRepository,
                                         EnhancedSustainabilityMetricsRepository metricsRepository) {
        this.sustainabilityRepository = sustainabilityRepository;
        this.impactCalculator = impactCalculator;
        this.sustainabilityInputRepository = sustainabilityInputRepository;
        this.metricsRepository = metricsRepository;
    }

    // ==================== MAIN PUBLIC METHOD ====================

    @Transactional
    public EnhancedImpactMetricsDTO calculateEnhancedMetrics(
            SustainabilityInputDTO inputDTO,
            org.example.fabric_waste_prediction.DTO.ImpactMetricsDTO baseImpact) {

        // 1. Convert simple DTO to format expected by calculations
        ConvertedInput converted = convertToServiceFormat(inputDTO);

        // 2. Save the input data to database
        SustainabilityInput savedInput = saveSustainabilityInput(inputDTO);

        // 3. Check if metrics already exist for this input
        Optional<EnhancedSustainabilityMetrics> existingMetrics =
                metricsRepository.findByInput(savedInput).stream().findFirst();

        EnhancedImpactMetricsDTO enhanced;

        if (existingMetrics.isPresent()) {
            // Metrics already exist - update them instead of creating new
            enhanced = performFullCalculations(converted, baseImpact);
            updateEnhancedMetrics(existingMetrics.get(), enhanced);
        } else {
            // Calculate enhanced metrics using converted values
            enhanced = performFullCalculations(converted, baseImpact);

            // Save the calculated metrics to database
            saveEnhancedMetrics(savedInput, enhanced);
        }

        return enhanced;
    }

    // ==================== CONVERSION METHOD (UPDATED) ====================

    private ConvertedInput convertToServiceFormat(SustainabilityInputDTO simple) {
        ConvertedInput converted = new ConvertedInput();

        // Waste Management - map simple fields
        converted.wasteDisposalMethod = simple.getWasteDisposal();
        converted.recyclingRate = simple.getRecyclingRate();
        converted.finalDisposalMethod = simple.getWasteDisposal();

        // Energy - map simple fields
        converted.energySource = simple.getEnergySource();
        converted.renewableEnergyPercentage = simple.getRenewablePercentage();
        converted.usesRenewableEnergy = simple.getRenewablePercentage() >= 30;

        // Water treatment removed - default to 85% for textile industry (typical good practice)
        converted.waterTreatmentRate = 85.0;  // Default good practice value

        // Certification
        converted.ecoCertification = simple.getCertification();

        // Compliance
        converted.meetsLocalRegulations = simple.getFollowsLaws();

        // Default values for other fields
        converted.treatmentFacilityType = "LOCAL";
        converted.transportDistanceKm = 100.0;
        converted.carbonCreditPrice = 50.0;
        converted.recycledContentPercentage = simple.getRecyclingRate() * 0.8;
        converted.participatesInTakeBack = "RECYCLE".equals(simple.getWasteDisposal());
        converted.carbonTaxRate = 25.0;
        converted.environmentalStandard = Boolean.TRUE.equals(simple.getFollowsLaws()) ? "ISO_14001" : "NONE";

        // Social Impact (default values)
        converted.jobsCreated = 0;
        converted.communityProgram = "";
        converted.fairTradePremium = 0.0;

        return converted;
    }

    // ==================== DATABASE PERSISTENCE METHODS ====================

    private SustainabilityInput saveSustainabilityInput(SustainabilityInputDTO dto) {
        SustainabilityInput entity = new SustainabilityInput();

        // Simple form fields - matching your new entity structure
        entity.setRecyclingRate(dto.getRecyclingRate());
        entity.setWasteDisposal(dto.getWasteDisposal());
        entity.setEnergySource(dto.getEnergySource());
        entity.setRenewablePercentage(dto.getRenewablePercentage());
        entity.setTreatWastewater(false); // Water treatment not in simple form, default to false
        entity.setCertification(dto.getCertification());
        entity.setFollowsLaws(dto.getFollowsLaws());

        // Calculated fields
        entity.setSustainabilityScore(dto.calculateSimpleScore());
        entity.setRating(dto.getRating());

        // Calculate individual component scores (updated for 6 questions)
        double wasteScore = (dto.getRecyclingRate() / 100) * 35;
        double energyScore = (dto.getRenewablePercentage() / 100) * 35;
        double waterScore = 0; // Water treatment not in simple form
        double complianceScore = Boolean.TRUE.equals(dto.getFollowsLaws()) ? 20 : 0;
        double certificationBonus = "BLUESIGN".equals(dto.getCertification()) ? 10 :
                "GOTS".equals(dto.getCertification()) ? 8 :
                        "OEKO_TEX".equals(dto.getCertification()) ? 5 : 0;

        entity.setWasteScore(wasteScore);
        entity.setEnergyScore(energyScore);
        entity.setWaterScore(waterScore);
        entity.setComplianceScore(complianceScore);
        entity.setCertificationBonus(certificationBonus);

        // Metadata
        entity.setUserId("system");
        entity.setSessionId(null);
        entity.setNotes(null);

        return sustainabilityInputRepository.save(entity);
    }

    // New method to update existing metrics
    private void updateEnhancedMetrics(EnhancedSustainabilityMetrics metrics, EnhancedImpactMetricsDTO dto) {
        // Waste Management Metrics
        metrics.setRecyclingEfficiency(dto.getRecyclingEfficiency());
        metrics.setLandfillDiversionRate(dto.getLandfillDiversionRate());
        metrics.setCompostingRate(dto.getCompostingRate());
        metrics.setIncinerationRate(dto.getIncinerationRate());

        // Circular Economy Metrics
        metrics.setCircularEconomyIndex(dto.getCircularEconomyIndex());
        metrics.setMaterialRecyclabilityScore(dto.getMaterialRecyclabilityScore());
        metrics.setClosedLoopEligible(dto.getClosedLoopEligible());
        metrics.setRecycledContentUsed(dto.getRecycledContentUsed());

        // Water & Energy Metrics
        metrics.setWaterRecyclingSavings(dto.getWaterRecyclingSavings());
        metrics.setRenewableEnergyContribution(dto.getRenewableEnergyContribution());
        metrics.setEnergyIntensityReduction(dto.getEnergyIntensityReduction());
        metrics.setWaterFootprint(dto.getWaterFootprint());

        // Environmental Impact
        metrics.setBiodiversityImpactScore(dto.getBiodiversityImpactScore());
        metrics.setEcosystemContribution(dto.getEcosystemContribution());
        metrics.setCarbonFootprintReduction(dto.getCarbonFootprintReduction());
        metrics.setMethaneEmissionsAvoided(dto.getMethaneEmissionsAvoided());

        // Social Impact
        metrics.setSocialImpactScore(dto.getSocialImpactScore());
        metrics.setGreenJobsCreated(dto.getGreenJobsCreated());
        metrics.setCommunityBenefit(dto.getCommunityBenefit());
        metrics.setCommunityProgramImpact(dto.getCommunityProgramImpact());

        // Economic Impact
        metrics.setCircularEconomyRevenue(dto.getCircularEconomyRevenue());
        metrics.setCostSavingsFromRecycling(dto.getCostSavingsFromRecycling());
        metrics.setCarbonCreditRevenue(dto.getCarbonCreditRevenue());
        metrics.setTaxBenefits(dto.getTaxBenefits());

        // Compliance & Certification
        metrics.setEsgScore(dto.getEsgScore());
        metrics.setEnvironmentalCompliance(dto.getEnvironmentalCompliance());
        metrics.setMeetsGreenStandards(dto.getMeetsGreenStandards());
        metrics.setEligibleCertifications(dto.getEligibleCertifications());
        metrics.setEligibleForCarbonCredits(dto.getEligibleForCarbonCredits());
        metrics.setIso14001Compliant(dto.getIso14001Compliant());

        // Final Scores
        metrics.setOverallSustainabilityScore(dto.getOverallSustainabilityScore());
        metrics.setSustainabilityRating(dto.getSustainabilityRating());
        metrics.setSustainabilityRiskLevel(dto.getSustainabilityRiskLevel());
        metrics.setClimateRiskScore(dto.getClimateRiskScore());
        metrics.setRegulatoryRiskExists(dto.getRegulatoryRiskExists());

        // Save the updated metrics
        metricsRepository.save(metrics);
    }

    private void saveEnhancedMetrics(SustainabilityInput input, EnhancedImpactMetricsDTO dto) {
        EnhancedSustainabilityMetrics metrics = new EnhancedSustainabilityMetrics();
        metrics.setInput(input);

        // Waste Management Metrics
        metrics.setRecyclingEfficiency(dto.getRecyclingEfficiency());
        metrics.setLandfillDiversionRate(dto.getLandfillDiversionRate());
        metrics.setCompostingRate(dto.getCompostingRate());
        metrics.setIncinerationRate(dto.getIncinerationRate());

        // Circular Economy Metrics
        metrics.setCircularEconomyIndex(dto.getCircularEconomyIndex());
        metrics.setMaterialRecyclabilityScore(dto.getMaterialRecyclabilityScore());
        metrics.setClosedLoopEligible(dto.getClosedLoopEligible());
        metrics.setRecycledContentUsed(dto.getRecycledContentUsed());

        // Water & Energy Metrics
        metrics.setWaterRecyclingSavings(dto.getWaterRecyclingSavings());
        metrics.setRenewableEnergyContribution(dto.getRenewableEnergyContribution());
        metrics.setEnergyIntensityReduction(dto.getEnergyIntensityReduction());
        metrics.setWaterFootprint(dto.getWaterFootprint());

        // Environmental Impact
        metrics.setBiodiversityImpactScore(dto.getBiodiversityImpactScore());
        metrics.setEcosystemContribution(dto.getEcosystemContribution());
        metrics.setCarbonFootprintReduction(dto.getCarbonFootprintReduction());
        metrics.setMethaneEmissionsAvoided(dto.getMethaneEmissionsAvoided());

        // Social Impact
        metrics.setSocialImpactScore(dto.getSocialImpactScore());
        metrics.setGreenJobsCreated(dto.getGreenJobsCreated());
        metrics.setCommunityBenefit(dto.getCommunityBenefit());
        metrics.setCommunityProgramImpact(dto.getCommunityProgramImpact());

        // Economic Impact
        metrics.setCircularEconomyRevenue(dto.getCircularEconomyRevenue());
        metrics.setCostSavingsFromRecycling(dto.getCostSavingsFromRecycling());
        metrics.setCarbonCreditRevenue(dto.getCarbonCreditRevenue());
        metrics.setTaxBenefits(dto.getTaxBenefits());

        // Compliance & Certification
        metrics.setEsgScore(dto.getEsgScore());
        metrics.setEnvironmentalCompliance(dto.getEnvironmentalCompliance());
        metrics.setMeetsGreenStandards(dto.getMeetsGreenStandards());
        metrics.setEligibleCertifications(dto.getEligibleCertifications());
        metrics.setEligibleForCarbonCredits(dto.getEligibleForCarbonCredits());
        metrics.setIso14001Compliant(dto.getIso14001Compliant());

        // Final Scores
        metrics.setOverallSustainabilityScore(dto.getOverallSustainabilityScore());
        metrics.setSustainabilityRating(dto.getSustainabilityRating());
        metrics.setSustainabilityRiskLevel(dto.getSustainabilityRiskLevel());
        metrics.setClimateRiskScore(dto.getClimateRiskScore());
        metrics.setRegulatoryRiskExists(dto.getRegulatoryRiskExists());

        metricsRepository.save(metrics);
    }

    // ==================== CALCULATION METHODS ====================

    private EnhancedImpactMetricsDTO performFullCalculations(ConvertedInput input, org.example.fabric_waste_prediction.DTO.ImpactMetricsDTO baseImpact) {
        EnhancedImpactMetricsDTO enhanced = new EnhancedImpactMetricsDTO();
        copyBaseImpactMetrics(enhanced, baseImpact);

        enhanced.setRecyclingEfficiency(calculateRecyclingEfficiency(input));
        enhanced.setLandfillDiversionRate(calculateLandfillDiversion(input));
        enhanced.setCompostingRate(calculateCompostingRate(input));
        enhanced.setIncinerationRate(calculateIncinerationRate(input));
        enhanced.setCircularEconomyIndex(calculateCircularEconomyIndex(input));
        enhanced.setMaterialRecyclabilityScore(calculateRecyclabilityScore(input));
        enhanced.setClosedLoopEligible(determineClosedLoopEligibility(input));
        enhanced.setRecycledContentUsed(input.recycledContentPercentage);
        enhanced.setWaterRecyclingSavings(calculateWaterSavings(input, baseImpact));
        enhanced.setRenewableEnergyContribution(input.renewableEnergyPercentage);
        enhanced.setEnergyIntensityReduction(calculateEnergyReduction(input));
        enhanced.setWaterFootprint(calculateWaterFootprint(input, baseImpact));
        enhanced.setBiodiversityImpactScore(calculateBiodiversityScore(input));
        enhanced.setEcosystemContribution(calculateEcosystemContribution(input));
        enhanced.setCarbonFootprintReduction(calculateCarbonReduction(input, baseImpact));
        enhanced.setMethaneEmissionsAvoided(calculateMethaneAvoided(input));
        enhanced.setSocialImpactScore(calculateSocialImpactScore(input));
        enhanced.setGreenJobsCreated(input.jobsCreated);
        enhanced.setCommunityBenefit(calculateCommunityBenefit(input));
        enhanced.setCommunityProgramImpact(input.communityProgram);
        enhanced.setCircularEconomyRevenue(calculateCircularRevenue(input, baseImpact));
        enhanced.setCostSavingsFromRecycling(calculateRecyclingSavings(input, baseImpact));
        enhanced.setCarbonCreditRevenue(calculateCarbonCreditRevenue(input, baseImpact));
        enhanced.setTaxBenefits(calculateTaxBenefits(input, baseImpact));
        enhanced.setEsgScore(calculateESGScore(input, enhanced));
        enhanced.setEnvironmentalCompliance(determineComplianceLevel(input));
        enhanced.setMeetsGreenStandards(checkGreenStandards(input));
        enhanced.setEligibleCertifications(determineEligibleCertifications(input));
        enhanced.setEligibleForCarbonCredits(checkCarbonCreditEligibility(input));
        enhanced.setIso14001Compliant(checkISO14001Compliance(input));
        enhanced.setSustainabilityBreakdown(generateSustainabilityBreakdown(input, enhanced));
        enhanced.setMonthlyImprovement(calculateMonthlyImprovement(input));
        enhanced.setRecommendationsMap(generateRecommendations(input, enhanced));
        enhanced.setImprovementActions(generateImprovementActions(input, enhanced));
        enhanced.setSustainabilityRiskLevel(determineRiskLevel(enhanced));
        enhanced.setClimateRiskScore(calculateClimateRiskScore(input));
        enhanced.setRegulatoryRiskExists(checkRegulatoryRisk(input));
        enhanced.setSustainabilityScore(enhanced.getOverallSustainabilityScore());
        enhanced.setSustainabilityRating(enhanced.getSustainabilityRating());

        return enhanced;
    }

    private void copyBaseImpactMetrics(EnhancedImpactMetricsDTO enhanced, org.example.fabric_waste_prediction.DTO.ImpactMetricsDTO base) {
        enhanced.setCo2SavedKg(base.getCo2SavedKg());
        enhanced.setWaterSavedL(base.getWaterSavedL());
        enhanced.setFabricSavedKg(base.getFabricSavedKg());
        enhanced.setEnergySavedKwh(base.getEnergySavedKwh());
        enhanced.setCostSavedLkr(base.getCostSavedLkr());
        enhanced.setLandfillAvoidedKg(base.getLandfillAvoidedKg());
        enhanced.setPredictedWastagePct(base.getPredictedWastagePct());
        enhanced.setSustainabilityScore(base.getSustainabilityScore());
        enhanced.setFabricType(base.getFabricType());
        enhanced.setOrderQuantity(base.getOrderQuantity());
        enhanced.setRiskLevel(base.getRiskLevel());
        enhanced.setRecommendations(base.getRecommendations());
    }

    // ==================== ALL EXISTING CALCULATION METHODS ====================

    private Double calculateRecyclingEfficiency(ConvertedInput input) {
        double efficiency = input.recyclingRate;
        if ("INTERNATIONAL".equals(input.treatmentFacilityType)) {
            efficiency *= 1.2;
        }
        if ("GOTS".equals(input.ecoCertification)) {
            efficiency *= 1.15;
        }
        return Math.min(100, efficiency);
    }

    private Double calculateLandfillDiversion(ConvertedInput input) {
        double diversion = 0.0;
        if ("RECYCLE".equals(input.wasteDisposalMethod)) {
            diversion = input.recyclingRate;
        } else if ("COMPOST".equals(input.wasteDisposalMethod)) {
            diversion = 80.0;
        } else if ("INCINERATE".equals(input.wasteDisposalMethod)) {
            diversion = 30.0;
        }
        return Math.min(100, diversion);
    }

    private Double calculateCompostingRate(ConvertedInput input) {
        return "COMPOST".equals(input.wasteDisposalMethod) ? input.recyclingRate : 0.0;
    }

    private Double calculateIncinerationRate(ConvertedInput input) {
        return "INCINERATE".equals(input.wasteDisposalMethod) ? input.recyclingRate : 0.0;
    }

    private Double calculateCircularEconomyIndex(ConvertedInput input) {
        double index = 0.0;
        index += (input.recycledContentPercentage / 100) * 30;
        if (input.participatesInTakeBack) {
            index += 20;
        }
        if (input.ecoCertification != null && !"NONE".equals(input.ecoCertification)) {
            index += 25;
        }
        index += (input.recyclingRate / 100) * 25;
        return index;
    }

    private Double calculateRecyclabilityScore(ConvertedInput input) {
        double score = input.recyclingRate * 0.5;
        if ("GOTS".equals(input.ecoCertification)) {
            score += 30;
        } else if ("OEKO_TEX".equals(input.ecoCertification)) {
            score += 20;
        } else if ("BLUESIGN".equals(input.ecoCertification)) {
            score += 25;
        }
        if (input.participatesInTakeBack) {
            score += 15;
        }
        return Math.min(100, score);
    }

    private Boolean determineClosedLoopEligibility(ConvertedInput input) {
        return input.recycledContentPercentage >= 50 &&
                input.recyclingRate >= 60 &&
                input.participatesInTakeBack &&
                ("GOTS".equals(input.ecoCertification) || "C2C".equals(input.ecoCertification));
    }

    private Double calculateWaterSavings(ConvertedInput input, org.example.fabric_waste_prediction.DTO.ImpactMetricsDTO base) {
        double waterSaved = base.getWaterSavedL() != null ? base.getWaterSavedL() : 0;
        double treatmentBonus = (input.waterTreatmentRate / 100) * waterSaved * 0.3;
        if (input.usesRenewableEnergy) {
            treatmentBonus *= 1.1;
        }
        return waterSaved + treatmentBonus;
    }

    private Double calculateEnergyReduction(ConvertedInput input) {
        double reduction = input.renewableEnergyPercentage;
        if ("SOLAR".equals(input.energySource)) {
            reduction *= 1.2;
        } else if ("WIND".equals(input.energySource)) {
            reduction *= 1.15;
        } else if ("HYDRO".equals(input.energySource)) {
            reduction *= 1.1;
        }
        return Math.min(100, reduction);
    }

    private Double calculateWaterFootprint(ConvertedInput input, org.example.fabric_waste_prediction.DTO.ImpactMetricsDTO base) {
        double baseWaterSaved = base.getWaterSavedL() != null ? base.getWaterSavedL() : 0;
        double treatmentImpact = input.waterTreatmentRate / 100 * baseWaterSaved;
        return baseWaterSaved - treatmentImpact;
    }

    private Double calculateBiodiversityScore(ConvertedInput input) {
        double score = 50.0;
        if (input.recyclingRate > 50) score += 15;
        if (input.waterTreatmentRate > 80) score += 15;
        if (input.usesRenewableEnergy) score += 10;
        if ("GOTS".equals(input.ecoCertification)) score += 10;
        if (input.transportDistanceKm > 1000) score -= 10;
        if ("LANDFILL".equals(input.wasteDisposalMethod)) score -= 20;
        return Math.max(0, Math.min(100, score));
    }

    private Double calculateEcosystemContribution(ConvertedInput input) {
        double contribution = 0.0;
        if ("COMPOST".equals(input.wasteDisposalMethod)) {
            contribution += 30;
        }
        if (input.recyclingRate > 50) {
            contribution += 20;
        }
        if (input.waterTreatmentRate > 90) {
            contribution += 25;
        }
        if (input.renewableEnergyPercentage > 50) {
            contribution += 25;
        }
        return contribution;
    }

    private Double calculateCarbonReduction(ConvertedInput input, org.example.fabric_waste_prediction.DTO.ImpactMetricsDTO base) {
        double baseCarbon = base.getCo2SavedKg() != null ? base.getCo2SavedKg() : 0;
        double reduction = baseCarbon;
        if (input.recyclingRate > 50) {
            reduction += baseCarbon * 0.2;
        }
        if (input.usesRenewableEnergy) {
            reduction += baseCarbon * 0.15;
        }
        if (input.carbonCreditPrice > 100) {
            reduction += baseCarbon * 0.1;
        }
        return reduction;
    }

    private Double calculateMethaneAvoided(ConvertedInput input) {
        double methaneAvoided = 0.0;
        if ("COMPOST".equals(input.wasteDisposalMethod)) {
            methaneAvoided = input.recyclingRate * 0.5;
        } else if ("RECYCLE".equals(input.wasteDisposalMethod)) {
            methaneAvoided = input.recyclingRate * 0.3;
        }
        return methaneAvoided;
    }

    private Double calculateSocialImpactScore(ConvertedInput input) {
        double score = 50.0;
        if (input.jobsCreated != null) {
            score += Math.min(20, input.jobsCreated / 10);
        }
        if (input.communityProgram != null && !input.communityProgram.isEmpty()) {
            score += 15;
        }
        if (input.fairTradePremium != null && input.fairTradePremium > 0) {
            score += Math.min(15, input.fairTradePremium / 100);
        }
        return Math.min(100, score);
    }

    private Double calculateCommunityBenefit(ConvertedInput input) {
        double benefit = 0.0;
        if (input.communityProgram != null) {
            benefit += 5000;
            if (input.jobsCreated != null) {
                benefit += input.jobsCreated * 1000;
            }
            if (input.fairTradePremium != null) {
                benefit += input.fairTradePremium * 10;
            }
        }
        return benefit;
    }

    private Double calculateCircularRevenue(ConvertedInput input, org.example.fabric_waste_prediction.DTO.ImpactMetricsDTO base) {
        double revenue = 0.0;
        if (input.recycledContentPercentage > 0) {
            revenue += base.getFabricSavedKg() * 2.5 * (input.recycledContentPercentage / 100);
        }
        if (input.participatesInTakeBack) {
            revenue += base.getFabricSavedKg() * 1.5;
        }
        return revenue * usdToLkrRate;
    }

    private Double calculateRecyclingSavings(ConvertedInput input, org.example.fabric_waste_prediction.DTO.ImpactMetricsDTO base) {
        double savings = 0.0;
        if (input.recyclingRate > 0) {
            double wasteReduced = base.getLandfillAvoidedKg() != null ? base.getLandfillAvoidedKg() : 0;
            savings += wasteReduced * 0.5 * (input.recyclingRate / 100);
        }
        return savings * usdToLkrRate;
    }

    private Double calculateCarbonCreditRevenue(ConvertedInput input, org.example.fabric_waste_prediction.DTO.ImpactMetricsDTO base) {
        if (input.carbonCreditPrice == null || input.carbonCreditPrice <= 0) {
            return 0.0;
        }
        double carbonReduced = base.getCo2SavedKg() != null ? base.getCo2SavedKg() / 1000 : 0;
        double revenue = carbonReduced * input.carbonCreditPrice;
        return revenue * usdToLkrRate;
    }

    private Double calculateTaxBenefits(ConvertedInput input, org.example.fabric_waste_prediction.DTO.ImpactMetricsDTO base) {
        if (input.carbonTaxRate == null || input.carbonTaxRate <= 0) {
            return 0.0;
        }
        double carbonReduced = base.getCo2SavedKg() != null ? base.getCo2SavedKg() / 1000 : 0;
        double taxSaved = carbonReduced * input.carbonTaxRate;
        return taxSaved * usdToLkrRate;
    }

    private Double calculateESGScore(ConvertedInput input, EnhancedImpactMetricsDTO enhanced) {
        double environmental = (enhanced.getRecyclingEfficiency() * 0.3 +
                enhanced.getRenewableEnergyContribution() * 0.3 +
                enhanced.getWaterRecyclingSavings() / 1000 * 0.4);
        double social = enhanced.getSocialImpactScore();
        double governance = 50.0;
        if (input.environmentalStandard != null && !"NONE".equals(input.environmentalStandard)) {
            governance += 25;
        }
        if (input.meetsLocalRegulations) {
            governance += 15;
        }
        if (input.ecoCertification != null && !"NONE".equals(input.ecoCertification)) {
            governance += 10;
        }
        return (environmental * 0.4) + (social * 0.3) + (governance * 0.3);
    }

    private String determineComplianceLevel(ConvertedInput input) {
        if (input.meetsLocalRegulations &&
                input.environmentalStandard != null &&
                !"NONE".equals(input.environmentalStandard)) {
            return "FULL_COMPLIANCE";
        } else if (input.meetsLocalRegulations) {
            return "BASIC_COMPLIANCE";
        } else {
            return "NON_COMPLIANT";
        }
    }

    private Boolean checkGreenStandards(ConvertedInput input) {
        return input.recyclingRate >= 50 &&
                input.renewableEnergyPercentage >= 30 &&
                input.waterTreatmentRate >= 80 &&
                input.meetsLocalRegulations;
    }

    private String[] determineEligibleCertifications(ConvertedInput input) {
        List<String> certifications = new ArrayList<>();
        if (input.recyclingRate >= 70) {
            certifications.add("RECYCLED_CONTENT_CERTIFIED");
        }
        if (input.renewableEnergyPercentage >= 50) {
            certifications.add("RENEWABLE_ENERGY_CERTIFIED");
        }
        if (input.waterTreatmentRate >= 90) {
            certifications.add("WATER_STEWARDSHIP_CERTIFIED");
        }
        if (input.participatesInTakeBack) {
            certifications.add("CIRCULAR_ECONOMY_CERTIFIED");
        }
        return certifications.toArray(new String[0]);
    }

    private Boolean checkCarbonCreditEligibility(ConvertedInput input) {
        return input.recyclingRate >= 50 &&
                input.renewableEnergyPercentage >= 30 &&
                input.transportDistanceKm <= 500;
    }

    private Boolean checkISO14001Compliance(ConvertedInput input) {
        return "ISO_14001".equals(input.environmentalStandard) &&
                input.meetsLocalRegulations &&
                input.waterTreatmentRate >= 80;
    }

    private Map<String, Double> generateSustainabilityBreakdown(ConvertedInput input, EnhancedImpactMetricsDTO enhanced) {
        Map<String, Double> breakdown = new LinkedHashMap<>();
        breakdown.put("Waste Management", enhanced.getRecyclingEfficiency());
        breakdown.put("Circular Economy", enhanced.getCircularEconomyIndex());
        breakdown.put("Energy & Water", (enhanced.getRenewableEnergyContribution() +
                enhanced.getWaterRecyclingSavings() / 100) / 2);
        breakdown.put("Social Impact", enhanced.getSocialImpactScore());
        breakdown.put("Compliance", enhanced.getEsgScore());
        return breakdown;
    }

    private Map<String, Double> calculateMonthlyImprovement(ConvertedInput input) {
        Map<String, Double> improvement = new LinkedHashMap<>();
        improvement.put("Recycling Rate", input.recyclingRate * 0.05);
        improvement.put("Energy Efficiency", input.renewableEnergyPercentage * 0.03);
        improvement.put("Water Savings", input.waterTreatmentRate * 0.04);
        improvement.put("Carbon Reduction", input.carbonCreditPrice != null ? input.carbonCreditPrice * 0.1 : 5.0);
        return improvement;
    }

    private Map<String, String> generateRecommendations(ConvertedInput input, EnhancedImpactMetricsDTO enhanced) {
        Map<String, String> recommendations = new LinkedHashMap<>();
        if (enhanced.getRecyclingEfficiency() < 50) {
            recommendations.put("recycling", "Increase recycling rate to at least 50%");
        }
        if (enhanced.getRenewableEnergyContribution() < 30) {
            recommendations.put("energy", "Increase renewable energy usage to 30%");
        }
        if (enhanced.getWaterRecyclingSavings() < 1000) {
            recommendations.put("water", "Implement water recycling systems");
        }
        if (enhanced.getSocialImpactScore() < 60) {
            recommendations.put("social", "Create community programs and green jobs");
        }
        if (!input.meetsLocalRegulations) {
            recommendations.put("compliance", "Ensure compliance with environmental regulations");
        }
        return recommendations;
    }

    private List<String> generateImprovementActions(ConvertedInput input, EnhancedImpactMetricsDTO enhanced) {
        List<String> actions = new ArrayList<>();
        if (input.recyclingRate < 50) {
            actions.add("Invest in recycling infrastructure");
            actions.add("Partner with local recycling facilities");
        }
        if (input.renewableEnergyPercentage < 30) {
            actions.add("Install solar panels or wind turbines");
            actions.add("Purchase renewable energy certificates");
        }
        if (input.waterTreatmentRate < 80) {
            actions.add("Upgrade water treatment facilities");
            actions.add("Implement water recycling systems");
        }
        if (input.ecoCertification == null || "NONE".equals(input.ecoCertification)) {
            actions.add("Apply for GOTS, OEKO-TEX, or BLUESIGN certification");
        }
        if (!input.participatesInTakeBack) {
            actions.add("Implement customer take-back program");
        }
        return actions;
    }

    private String determineRiskLevel(EnhancedImpactMetricsDTO enhanced) {
        double score = enhanced.getOverallSustainabilityScore();
        if (score >= 80) return "LOW";
        if (score >= 60) return "MEDIUM";
        if (score >= 40) return "HIGH";
        return "CRITICAL";
    }

    private Double calculateClimateRiskScore(ConvertedInput input) {
        double risk = 100.0;
        if (input.recyclingRate > 50) risk -= 20;
        if (input.renewableEnergyPercentage > 30) risk -= 20;
        if (input.waterTreatmentRate > 80) risk -= 15;
        if (input.usesRenewableEnergy) risk -= 15;
        if (input.ecoCertification != null && !"NONE".equals(input.ecoCertification)) risk -= 10;
        return Math.max(0, risk);
    }

    private Boolean checkRegulatoryRisk(ConvertedInput input) {
        return !input.meetsLocalRegulations ||
                input.environmentalStandard == null ||
                "NONE".equals(input.environmentalStandard);
    }

    // ==================== INNER CLASS ====================

    private static class ConvertedInput {
        String wasteDisposalMethod;
        Double recyclingRate;
        String finalDisposalMethod;
        String treatmentFacilityType = "LOCAL";
        String energySource;
        Double renewableEnergyPercentage;
        Boolean usesRenewableEnergy = false;
        Double waterTreatmentRate = 85.0;  // Default good practice value
        Double transportDistanceKm = 100.0;
        Double carbonCreditPrice = 50.0;
        Double recycledContentPercentage = 50.0;
        Boolean participatesInTakeBack = false;
        String ecoCertification = "NONE";
        Boolean meetsLocalRegulations = false;
        String environmentalStandard = "NONE";
        Double carbonTaxRate = 25.0;
        Integer jobsCreated = 0;
        String communityProgram = "";
        Double fairTradePremium = 0.0;
    }
}