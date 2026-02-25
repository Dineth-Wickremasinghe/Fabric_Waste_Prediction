package org.example.fabric_waste_prediction.Controller;

import org.example.fabric_waste_prediction.DTO.PredictionRequest;
import org.example.fabric_waste_prediction.DTO.PredictionResponse;
import org.example.fabric_waste_prediction.Entity.Prediction;
import org.example.fabric_waste_prediction.Service.PredictionService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Arrays;
import  org.springframework.web.servlet.mvc.support.RedirectAttributes;


@Controller
@RequestMapping("/dashboard")
public class PredictionController {

    private final PredictionService predictionService;

    public PredictionController(PredictionService predictionService) {
        this.predictionService = predictionService;
    }

    @GetMapping
    public String dashboard(Model model) {
        List<Prediction> history = predictionService.getAllPredictions();
        model.addAttribute("history", history);

        return "dashboard";
    }

    @PostMapping("/predict")
    public String predict(
            @RequestParam Double patternComplexity,
            @RequestParam Double operatorExperienceYears,
            @RequestParam Double fabricPatternEncoded,
            @RequestParam Double cuttingMethodManual,
            @RequestParam Double fabricTypeEncoded,
            @RequestParam Double markerLossPct,
            RedirectAttributes redirectAttributes) {

        PredictionRequest request = new PredictionRequest(Arrays.asList(
                patternComplexity,
                operatorExperienceYears,
                fabricPatternEncoded,
                cuttingMethodManual,
                fabricTypeEncoded,
                markerLossPct
        ));

        PredictionResponse response = predictionService.getPredictionAndSave(request);
        redirectAttributes.addFlashAttribute("prediction", response.getPrediction());

        // ✅ Redirect to GET /dashboard which will load history fresh
        return "redirect:/dashboard";
    }
}