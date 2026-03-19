package com.dashboard.cuttingrisk.controller;

import com.dashboard.cuttingrisk.model.CuttingRiskRequest;
import com.dashboard.cuttingrisk.model.WasteRequest;
import com.dashboard.cuttingrisk.model.WasteResponse;
import com.dashboard.cuttingrisk.service.WasteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/waste")
@CrossOrigin(origins = "*")
public class WasteController {

    @Autowired
    private WasteService wasteService;

    // ══════════════════════════════════════════
    // PREDICTION
    // ══════════════════════════════════════════
    @PostMapping("/predict")
    public WasteResponse predict(@RequestBody WasteRequest request) {
        WasteResponse response = wasteService.calculateWaste(request);
        wasteService.savePrediction(request, response);
        return response;
    }

    // ══════════════════════════════════════════
    // TREND CHART
    // ══════════════════════════════════════════
    @GetMapping("/trend")
    public Map<String, Object> getTrend() {
        Map<String, Object> result = new HashMap<>();
        result.put("labels",    wasteService.getTrendLabels());
        result.put("data",      wasteService.getTrendData());
        result.put("predicted", wasteService.getTrendPredicted());
        return result;
    }

    // ══════════════════════════════════════════
    // FABRIC CHART
    // ══════════════════════════════════════════
    @GetMapping("/fabric")
    public Map<String, Object> getFabricWaste() {
        Map<String, Object> result = new HashMap<>();
        result.put("labels", wasteService.getFabricLabels());
        result.put("data",   wasteService.getFabricWasteData());
        return result;
    }

    // ══════════════════════════════════════════
    // STYLE/PATTERN CHART
    // ══════════════════════════════════════════
    @GetMapping("/style")
    public Map<String, Object> getStyleWaste() {
        Map<String, Object> result = new HashMap<>();
        result.put("labels", wasteService.getStyleLabels());
        result.put("data",   wasteService.getStyleWasteData());
        return result;
    }

    // ══════════════════════════════════════════
    // LAST 5 PREDICTIONS
    // ══════════════════════════════════════════
    @GetMapping("/last5")
    public Object getLast5() {
        return wasteService.getLast5Predictions();
    }

    // ══════════════════════════════════════════
    // MODEL ACCURACY
    // ══════════════════════════════════════════
    @GetMapping("/accuracy")
    public Object getAccuracy() {
        return wasteService.getMaterialAccuracy();
    }

    // ══════════════════════════════════════════
    // PREDICTION HISTORY
    // ══════════════════════════════════════════
    @GetMapping("/history")
    public Object getHistory() {
        return wasteService.getPredictionHistory();
    }

    // ══════════════════════════════════════════
    // SHIFT CHART
    // ══════════════════════════════════════════
    @GetMapping("/shift")
    public Map<String, Object> getShiftWaste() {
        Map<String, Object> result = new HashMap<>();
        result.put("labels", wasteService.getShiftLabels());
        result.put("data",   wasteService.getShiftWasteData());
        return result;
    }

    // ══════════════════════════════════════════
    // CUTTING METHOD CHART
    // ══════════════════════════════════════════
    @GetMapping("/cuttingmethod")
    public Map<String, Object> getCuttingMethodWaste() {
        Map<String, Object> result = new HashMap<>();
        result.put("labels", wasteService.getCuttingMethodLabels());
        result.put("data",   wasteService.getCuttingMethodData());
        return result;
    }

    // ══════════════════════════════════════════
    // RISK DISTRIBUTION
    // ══════════════════════════════════════════
    @GetMapping("/riskdistribution")
    public Map<String, Object> getRiskDistribution() {
        return wasteService.getRiskDistribution();
    }

    // ══════════════════════════════════════════
    // ACCURACY GAP
    // ══════════════════════════════════════════
    @GetMapping("/accuracygap")
    public Map<String, Object> getAccuracyGap() {
        Map<String, Object> result = new HashMap<>();
        result.put("labels", wasteService.getAccuracyGapLabels());
        result.put("data",   wasteService.getAccuracyGapData());
        return result;
    }

    // ══════════════════════════════════════════
    // SUMMARY KPIs
    // ══════════════════════════════════════════
    @GetMapping("/summary")
    public Map<String, Object> getSummaryKPIs() {
        return wasteService.getSummaryKPIs();
    }

    // ══════════════════════════════════════════
    // CUTTING RISK RECORDS
    // ══════════════════════════════════════════
    @GetMapping("/predictions/list")
    public Object getPredictionsList() {
        return wasteService.getPredictionsForDropdown();
    }

    @PostMapping("/risk/save")
    public Map<String, Object> saveCuttingRisk(
            @RequestBody CuttingRiskRequest request) {
        return wasteService.saveCuttingRiskRecord(request);
    }

    @GetMapping("/risk/shift")
    public Map<String, Object> getRiskShift() {
        Map<String, Object> result = new HashMap<>();
        result.put("labels", wasteService.getRiskShiftLabels());
        result.put("data",   wasteService.getRiskShiftData());
        return result;
    }

    @GetMapping("/risk/gsm")
    public Map<String, Object> getGsmWaste() {
        Map<String, Object> result = new HashMap<>();
        result.put("labels", wasteService.getGsmLabels());
        result.put("data",   wasteService.getGsmData());
        return result;
    }
}