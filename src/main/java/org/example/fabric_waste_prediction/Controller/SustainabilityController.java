package org.example.fabric_waste_prediction.Controller;

import jakarta.validation.Valid;
import org.example.fabric_waste_prediction.dto.FabricBreakdownDTO;
import org.example.fabric_waste_prediction.dto.ImpactMetricsDTO;
import org.example.fabric_waste_prediction.dto.PredictionInputDTO;
import org.example.fabric_waste_prediction.Entity.FabricRisk;
import org.example.fabric_waste_prediction.Service.ReportGeneratorService;
import org.example.fabric_waste_prediction.Service.SustainabilityService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/sustainability")
public class SustainabilityController {

    private final SustainabilityService sustainabilityService;
    private final ReportGeneratorService reportGeneratorService;

    public SustainabilityController(SustainabilityService sustainabilityService,
                                    ReportGeneratorService reportGeneratorService) {
        this.sustainabilityService = sustainabilityService;
        this.reportGeneratorService = reportGeneratorService;
    }

    @GetMapping("/dashboard")
    public String showDashboard(Model model) {
        Map<String, Object> summary = sustainabilityService.getDashboardSummary();
        List<FabricBreakdownDTO> fabricBreakdown = sustainabilityService.getFabricBreakdown();
        List<FabricRisk> highRiskFabrics = sustainabilityService.getHighRiskFabrics();

        model.addAttribute("summary", summary);
        model.addAttribute("fabricBreakdown", fabricBreakdown);
        model.addAttribute("highRiskFabrics", highRiskFabrics);
        model.addAttribute("currentDate", LocalDate.now());
        model.addAttribute("predictionInput", new PredictionInputDTO());

        // Add dropdown attributes
        model.addAttribute("fabricTypes", getFabricTypes());
        model.addAttribute("fabricPatterns", getFabricPatterns());

        return "sustainability/dashboard";
    }

    private String[] getFabricTypes() {
        return new String[]{"Cotton", "Linen", "Silk", "Polyester", "Viscose", "Rayon",
                "Denim", "Twill", "Poplin", "Blended"};
    }

    private String[] getFabricPatterns() {
        return new String[]{"Solid", "Striped", "Checked", "Floral", "Printed"};
    }

    @PostMapping("/predict")
    public String predictImpact(@Valid @ModelAttribute PredictionInputDTO input,
                                RedirectAttributes redirectAttributes) {
        try {
            Double predictedWastage = calculateSamplePrediction(input);
            ImpactMetricsDTO impact = sustainabilityService.processPrediction(input, predictedWastage);
            redirectAttributes.addFlashAttribute("impact", impact);
            redirectAttributes.addFlashAttribute("success", "Prediction calculated successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error calculating prediction: " + e.getMessage());
        }
        return "redirect:/sustainability/dashboard";
    }

    @GetMapping("/api/impact")
    @ResponseBody
    public ResponseEntity<ImpactMetricsDTO> getImpactMetrics(@RequestParam String fabricType,
                                                             @RequestParam Double orderQuantity) {
        PredictionInputDTO input = new PredictionInputDTO();
        input.setFabricType(fabricType);
        input.setOrderQuantity(orderQuantity);
        Double predictedWastage = 8.5;
        ImpactMetricsDTO impact = sustainabilityService.processPrediction(input, predictedWastage);
        return ResponseEntity.ok(impact);
    }

    @GetMapping("/api/fabric-breakdown")
    @ResponseBody
    public ResponseEntity<List<FabricBreakdownDTO>> getFabricBreakdown() {
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
            headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));;
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

    private Double calculateSamplePrediction(PredictionInputDTO input) {
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
}