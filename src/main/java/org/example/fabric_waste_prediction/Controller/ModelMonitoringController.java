package org.example.fabric_waste_prediction.Controller;

import org.example.fabric_waste_prediction.Entity.Prediction;
import org.example.fabric_waste_prediction.Repository.PredictionRepository;
import org.example.fabric_waste_prediction.Service.MetricsService;
import org.example.fabric_waste_prediction.Service.PredictionService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping(path = "/monitoring")
public class ModelMonitoringController {

    private final PredictionService predictionService;
    private final MetricsService metricsService;


    private final PredictionRepository predictionRepository;


    public ModelMonitoringController(PredictionService predictionService, PredictionRepository predictionRepository, MetricsService metricsService) {
        this.predictionRepository = predictionRepository;
        this.predictionService = predictionService;
        this.metricsService = metricsService;
    }

    @GetMapping(path = "/show")
    public String showDashboard(Model model) {
        List<Prediction> history = predictionService.getAllPredictions();
        model.addAttribute("history", history != null ? history : Collections.emptyList());

        Double r2 = metricsService.computeR2();
        Double mae = metricsService.computeMAE();

        model.addAttribute("maeScore", mae);
        model.addAttribute("r2Score", r2);

        model.addAttribute("enoughDataR2", r2 != null);
        model.addAttribute("enoughDataMAE", mae != null);
        return "model_feedback";
    }

    @PostMapping(path="/{id}/actual")
    public ResponseEntity<?> updateActual(
            @PathVariable Long id,
            @RequestBody Map<String, Double> payload) {

        Double actual = payload.get("actualResult");
        Prediction p = predictionRepository.findById(id).orElseThrow();
        p.setActualResult(actual);
        predictionRepository.save(p);

        return ResponseEntity.ok().build();
    }

    @PutMapping("/predictions/{id}/actual")
    public ResponseEntity<?> addActual(@PathVariable Long id, @RequestBody Double actual) {

        Prediction p = predictionRepository.findById(id).orElseThrow();
        p.setActualResult(actual);
        predictionRepository.save(p);

        Double r2 = metricsService.computeR2();

        return ResponseEntity.ok(Map.of(
                "message", "Actual value added",
                "r2_score", r2
        ));
    }


}
