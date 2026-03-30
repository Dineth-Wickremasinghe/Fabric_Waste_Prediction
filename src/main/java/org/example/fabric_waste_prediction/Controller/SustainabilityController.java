package org.example.fabric_waste_prediction.Controller;

import jakarta.validation.Valid;
import org.example.fabric_waste_prediction.Validation.SustainabilityInputValidator;
import org.example.fabric_waste_prediction.DTO.*;
import org.example.fabric_waste_prediction.Entity.FabricRisk;
import org.example.fabric_waste_prediction.Service.ReportGeneratorService;
import org.example.fabric_waste_prediction.Service.SustainabilityService;
import org.example.fabric_waste_prediction.Service.EnhancedSustainabilityService;
import org.example.fabric_waste_prediction.Validation.ValidSustainabilityInput;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Controller
@RequestMapping("/sustainability")
public class SustainabilityController {

    private final SustainabilityService sustainabilityService;
    private final EnhancedSustainabilityService enhancedSustainabilityService;
    private final ReportGeneratorService reportGeneratorService;

    public SustainabilityController(SustainabilityService sustainabilityService,
                                    EnhancedSustainabilityService enhancedSustainabilityService,
                                    ReportGeneratorService reportGeneratorService) {
        this.sustainabilityService = sustainabilityService;
        this.enhancedSustainabilityService = enhancedSustainabilityService;
        this.reportGeneratorService = reportGeneratorService;
    }

    // ==================== EXISTING ENDPOINTS ====================

    @GetMapping("/dashboard")
    public String showDashboard(Model model) {
        Map<String, Object> summary = sustainabilityService.getDashboardSummary();
        List<org.example.fabric_waste_prediction.DTO.FabricBreakdownDTO> fabricBreakdown = sustainabilityService.getFabricBreakdown();
        List<FabricRisk> highRiskFabrics = sustainabilityService.getHighRiskFabrics();

        model.addAttribute("summary", summary);
        model.addAttribute("fabricBreakdown", fabricBreakdown);
        model.addAttribute("highRiskFabrics", highRiskFabrics);
        model.addAttribute("currentDate", LocalDate.now());
        model.addAttribute("predictionInput", new org.example.fabric_waste_prediction.DTO.PredictionInputDTO());

        // Add dropdown attributes
        model.addAttribute("fabricTypes", getFabricTypes());
        model.addAttribute("fabricPatterns", getFabricPatterns());

        // Add sustainability input if not present (for the form)
        if (!model.containsAttribute("sustainabilityInput")) {
            model.addAttribute("sustainabilityInput", new org.example.fabric_waste_prediction.DTO.SustainabilityInputDTO());
        }

        return "sustainability/dashboard";
    }

    @PostMapping("/predict")
    public String predictImpact(@Valid @ModelAttribute org.example.fabric_waste_prediction.DTO.PredictionInputDTO input,
                                RedirectAttributes redirectAttributes) {
        try {
            Double predictedWastage = calculateSamplePrediction(input);
            org.example.fabric_waste_prediction.DTO.ImpactMetricsDTO impact = sustainabilityService.processPrediction(input, predictedWastage);
            redirectAttributes.addFlashAttribute("impact", impact);
            redirectAttributes.addFlashAttribute("success", "Prediction calculated successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error calculating prediction: " + e.getMessage());
        }
        return "redirect:/sustainability/dashboard";
    }

    // ==================== SIMPLE SUSTAINABILITY FORM ENDPOINTS (NEW) ====================

    /**
     * Show the simple sustainability input form (6 questions - Textile Industry)
     */
    //@GetMapping("/input-form")
    //public String showSimpleInputForm(Model model) {
    //    if (!model.containsAttribute("sustainabilityInput")) {
    //model.addAttribute("sustainabilityInput", new SustainabilityInputDTO());
    //    }

        // Add dropdown options for the simple form (updated for 6 questions - Textile Industry)
     //   model.addAttribute("disposalMethods", new String[]{"RECYCLE", "INCINERATE", "LANDFILL"});
      //  model.addAttribute("energySources", new String[]{"SOLAR", "WIND", "GRID", "MIXED"});
      //  model.addAttribute("certifications", new String[]{"NONE", "GOTS", "OEKO_TEX", "BLUESIGN"});
//
   //      return "sustainability/input-form";
    //}

    @GetMapping("/input-form")
    public String showSimpleInputForm() {
        return "redirect:/sustainability/dashboard";
    }
    /**
     * Process the simple sustainability form submission
     */
    @PostMapping("/calculate-simple")
    public String calculateSimpleMetrics(
            @Valid @ModelAttribute("sustainabilityInput") org.example.fabric_waste_prediction.DTO.SustainabilityInputDTO input,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("error", "Please fill all required fields correctly");
            redirectAttributes.addFlashAttribute("sustainabilityInput", input);
            return "redirect:/sustainability/input-form";
        }

        try {
            // Create base impact (or get from prediction)
            org.example.fabric_waste_prediction.DTO.ImpactMetricsDTO baseImpact = new org.example.fabric_waste_prediction.DTO.ImpactMetricsDTO();
            baseImpact.setCo2SavedKg(125.0);
            baseImpact.setWaterSavedL(7530.0);
            baseImpact.setFabricSavedKg(50.2);
            baseImpact.setEnergySavedKwh(2259.0);
            baseImpact.setCostSavedLkr(37500.0);
            baseImpact.setLandfillAvoidedKg(15.8);
            baseImpact.setSustainabilityScore(85.0);

            // Calculate enhanced metrics using your DTO
            org.example.fabric_waste_prediction.DTO.EnhancedImpactMetricsDTO enhanced = enhancedSustainabilityService.calculateEnhancedMetrics(input, baseImpact);

            // Add simple score as well
            double simpleScore = input.calculateSimpleScore();
            String rating = input.getRating();

            redirectAttributes.addFlashAttribute("enhancedMetrics", enhanced);
            redirectAttributes.addFlashAttribute("sustainabilityInput", input);
            redirectAttributes.addFlashAttribute("simpleScore", simpleScore);
            redirectAttributes.addFlashAttribute("simpleRating", rating);
            redirectAttributes.addFlashAttribute("success",
                    "Your Sustainability Score: " + Math.round(simpleScore) + "% - " + rating);

        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Error calculating metrics: " + e.getMessage());
            redirectAttributes.addFlashAttribute("sustainabilityInput", input);
        }

        return "redirect:/sustainability/input-form";
    }

    // ==================== ORIGINAL SUSTAINABILITY INPUT ENDPOINTS ====================

    /**
     * Original: Show sustainability input form (complex version)
     */
    @GetMapping("/input")
    public String showOriginalSustainabilityInputForm(Model model) {
        if (!model.containsAttribute("sustainabilityInput")) {
            model.addAttribute("sustainabilityInput", new org.example.fabric_waste_prediction.DTO.SustainabilityInputDTO());
        }

        // Add dropdown options for complex form
        model.addAttribute("disposalMethods", new String[]{"LANDFILL", "RECYCLE", "INCINERATE", "COMPOST", "ANAEROBIC_DIGESTION"});
        model.addAttribute("facilityTypes", new String[]{"LOCAL", "REGIONAL", "INTERNATIONAL"});
        model.addAttribute("certifications", new String[]{"NONE", "GOTS", "OEKO_TEX", "BLUESIGN", "C2C", "FAIR_TRADE"});
        model.addAttribute("energySources", new String[]{"SOLAR", "WIND", "HYDRO", "GRID", "MIXED", "BIOMASS"});
        model.addAttribute("environmentalStandards", new String[]{"NONE", "ISO_14001", "ISO_50001", "EMAS", "GRI", "SASB"});

        return "sustainability/input-form";
    }

    /**
     * Original: Process sustainability input and calculate enhanced metrics
     */
    @PostMapping("/calculate-sustainability")
    public String calculateSustainabilityMetrics(
            @Valid @ModelAttribute("sustainabilityInput") org.example.fabric_waste_prediction.DTO.SustainabilityInputDTO input,
            BindingResult bindingResult,
            @RequestParam(required = false) String scenario,
            RedirectAttributes redirectAttributes,
            Model model) {

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.sustainabilityInput", bindingResult);
            redirectAttributes.addFlashAttribute("sustainabilityInput", input);
            redirectAttributes.addFlashAttribute("error", "Please correct the validation errors");
            return "redirect:/sustainability/input";
        }

        try {
            // Get base impact from last prediction (or create default)
            org.example.fabric_waste_prediction.DTO.ImpactMetricsDTO baseImpact = new org.example.fabric_waste_prediction.DTO.ImpactMetricsDTO();
            baseImpact.setCo2SavedKg(125.0);
            baseImpact.setWaterSavedL(7530.0);
            baseImpact.setFabricSavedKg(50.2);
            baseImpact.setEnergySavedKwh(2259.0);
            baseImpact.setCostSavedLkr(37500.0);
            baseImpact.setLandfillAvoidedKg(15.8);

            // Calculate enhanced metrics
            EnhancedImpactMetricsDTO enhancedMetrics =
                    enhancedSustainabilityService.calculateEnhancedMetrics(input, baseImpact);

            redirectAttributes.addFlashAttribute("enhancedMetrics", enhancedMetrics);
            redirectAttributes.addFlashAttribute("sustainabilityInput", input);
            redirectAttributes.addFlashAttribute("success",
                    "Sustainability metrics calculated successfully! Overall Score: " +
                            String.format("%.1f", enhancedMetrics.getOverallSustainabilityScore()) + "%");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error calculating metrics: " + e.getMessage());
            redirectAttributes.addFlashAttribute("sustainabilityInput", input);
        }

        return "redirect:/sustainability/dashboard?showEnhanced=true";
    }

    // ==================== API ENDPOINTS ====================

    /**
     * API endpoint for real-time validation
     */
    @PostMapping("/api/validate-input")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> validateInput(@RequestBody org.example.fabric_waste_prediction.DTO.SustainabilityInputDTO input) {
        Map<String, Object> response = new HashMap<>();
        List<org.example.fabric_waste_prediction.DTO.ValidationErrorDTO> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();

        // Perform validation
        if (input.getRecyclingRate() + input.getRenewablePercentage() > 150) {
            errors.add(new org.example.fabric_waste_prediction.DTO.ValidationErrorDTO("combined",
                    "Recycling rate and renewable energy percentage combined too high"));
        }

        if ("RECYCLE".equals(input.getWasteDisposal()) &&
                input.getRecyclingRate() < 30) {
            warnings.add("Recycling method selected but recycling rate is low");
        }

        response.put("isValid", errors.isEmpty());
        response.put("errors", errors);
        response.put("warnings", warnings);
        response.put("suggestions", generateSuggestions(input));

        return errors.isEmpty() ?
                ResponseEntity.ok(response) :
                ResponseEntity.badRequest().body(response);
    }

    /**
     * API endpoint for calculation preview
     */
    @GetMapping("/api/preview-calculation")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> previewCalculation(
            @RequestParam Double recyclingRate,
            @RequestParam Double renewableEnergy,
            @RequestParam String disposalMethod) {

        Map<String, Object> preview = new HashMap<>();

        // Simple preview calculations
        double estimatedScore = (recyclingRate * 0.4) + (renewableEnergy * 0.3);
        if ("RECYCLE".equals(disposalMethod)) {
            estimatedScore += 10;
        }

        preview.put("estimatedSustainabilityScore", Math.min(100, estimatedScore));
        preview.put("estimatedRating", estimatedScore >= 80 ? "GOLD" :
                estimatedScore >= 60 ? "SILVER" :
                        estimatedScore >= 40 ? "BRONZE" : "BASIC");
        preview.put("estimatedCarbonSavings", recyclingRate * 2.5);
        preview.put("estimatedWaterSavings", renewableEnergy * 150);

        return ResponseEntity.ok(preview);
    }

    @GetMapping("/api/impact")
    @ResponseBody
    public ResponseEntity<org.example.fabric_waste_prediction.DTO.ImpactMetricsDTO> getImpactMetrics(@RequestParam String fabricType,
                                                                                                     @RequestParam Double orderQuantity) {
        org.example.fabric_waste_prediction.DTO.PredictionInputDTO input = new org.example.fabric_waste_prediction.DTO.PredictionInputDTO();
        input.setFabricType(fabricType);
        input.setOrderQuantity(orderQuantity);
        Double predictedWastage = 8.5;
        org.example.fabric_waste_prediction.DTO.ImpactMetricsDTO impact = sustainabilityService.processPrediction(input, predictedWastage);
        return ResponseEntity.ok(impact);
    }

    @GetMapping("/api/fabric-breakdown")
    @ResponseBody
    public ResponseEntity<List<org.example.fabric_waste_prediction.DTO.FabricBreakdownDTO>> getFabricBreakdown() {
        return ResponseEntity.ok(sustainabilityService.getFabricBreakdown());
    }

    @GetMapping("/api/high-risk")
    @ResponseBody
    public ResponseEntity<List<FabricRisk>> getHighRiskFabrics() {
        return ResponseEntity.ok(sustainabilityService.getHighRiskFabrics());
    }

    @GetMapping("/report/{format}")
    public ResponseEntity<byte[]> generateReport(
            @PathVariable String format,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        Map<String, Object> summaryData = sustainabilityService.getDashboardSummary();
        byte[] reportContent = reportGeneratorService.generateReport(format, startDate, endDate, summaryData);

        HttpHeaders headers = new HttpHeaders();
        String filename = "sustainability_report_" + LocalDate.now() + "." + format;

        if ("pdf".equalsIgnoreCase(format)) {
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", filename);
        } else if ("excel".equalsIgnoreCase(format)) {
            filename = "sustainability_report_" + LocalDate.now() + ".xlsx";
            headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        }

        return new ResponseEntity<>(reportContent, headers, HttpStatus.OK);
    }

    @GetMapping("/chart/trend")
    @ResponseBody
    public ResponseEntity<byte[]> getTrendChart(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        try {
            byte[] chartImage = reportGeneratorService.generateChartImage(startDate, endDate);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.IMAGE_PNG);
            return new ResponseEntity<>(chartImage, headers, HttpStatus.OK);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ==================== HELPER METHODS ====================

    private String[] getFabricTypes() {
        return new String[]{"Cotton", "Linen", "Silk", "Polyester", "Viscose", "Rayon",
                "Denim", "Twill", "Poplin", "Blended"};
    }

    private String[] getFabricPatterns() {
        return new String[]{"Solid", "Striped", "Checked", "Floral", "Printed"};
    }

    private Double calculateSamplePrediction(org.example.fabric_waste_prediction.DTO.PredictionInputDTO input) {
        double baseWastage = switch (input.getFabricType().toLowerCase()) {
            case "cotton" -> 8.5;
            case "silk" -> 12.0;
            case "linen" -> 9.2;
            case "polyester" -> 7.8;
            case "denim" -> 10.5;
            default -> 9.0;
        };

        double layerFactor = 1.0 - (input.getNumberOfLayers() * 0.02);
        if (layerFactor < 0.6) layerFactor = 0.6;

        double widthFactor = input.getFabricWidth() > 60 ? 0.95 : 1.05;

        return baseWastage * layerFactor * widthFactor;
    }

    private Map<String, String> generateSuggestions(org.example.fabric_waste_prediction.DTO.SustainabilityInputDTO input) {
        Map<String, String> suggestions = new HashMap<>();

        if (input.getRecyclingRate() < 50) {
            suggestions.put("recycling", "Consider increasing recycling rate to 50% for better score");
        }

        if (input.getRenewablePercentage() < 30) {
            suggestions.put("energy", "Increase renewable energy to 30% for tax benefits");
        }

        if ("LANDFILL".equals(input.getWasteDisposal())) {
            suggestions.put("disposal", "Consider switching to RECYCLE or INCINERATE for better impact");
        }

        if ("NONE".equals(input.getCertification())) {
            suggestions.put("certification", "Consider getting eco-certification (GOTS, OEKO-TEX, or BLUESIGN)");
        }

        return suggestions;
    }
}