package org.example.fabric_waste_prediction.Service;


import org.example.fabric_waste_prediction.Entity.*;
import org.example.fabric_waste_prediction.Repository.CuttingJobRepository;
import org.example.fabric_waste_prediction.Repository.CuttingRiskRecordRepository;
import org.example.fabric_waste_prediction.Repository.DailyWastageRepository;
import org.example.fabric_waste_prediction.Repository.MaterialAccuracyRepository;
import org.example.fabric_waste_prediction.Repository.PredictionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class WasteService {

    @Autowired
    private DailyWastageRepository dailyWastageRepository;

    @Autowired
    private CuttingJobRepository cuttingJobRepository;

    @Autowired
    private MaterialAccuracyRepository materialAccuracyRepository;

    @Autowired
    private PredictionRepository predictionRepository;

    @Autowired
    private CuttingRiskRecordRepository cuttingRiskRecordRepository;

    // ─── Trend Chart ──────────────────────────────────────────────────────────
    public double[] getTrendData() {
        List<Prediction> records = predictionRepository
                .findAllByOrderByCreatedAtDesc();
        Collections.reverse(records);
        return records.stream()
                .mapToDouble(r -> r.getActualResult() != null
                        ? r.getActualResult() : 0.0)
                .toArray();
    }

    public double[] getTrendPredicted() {
        List<Prediction> records = predictionRepository
                .findAllByOrderByCreatedAtDesc();
        Collections.reverse(records);
        return records.stream()
                .mapToDouble(r -> r.getPredictionResult() != null
                        ? r.getPredictionResult() : 0.0)
                .toArray();
    }

    public String[] getTrendLabels() {
        List<Prediction> records = predictionRepository
                .findAllByOrderByCreatedAtDesc();
        Collections.reverse(records);
        return records.stream()
                .map(r -> r.getCreatedAt().toLocalDate().toString())
                .toArray(String[]::new);
    }

    // ─── Fabric Chart ─────────────────────────────────────────────────────────
    public String[] getFabricLabels() {
        List<Object[]> results = predictionRepository.getAvgWasteByFabric();
        return results.stream()
                .map(row -> row[0] != null ? row[0].toString() : "Unknown")
                .toArray(String[]::new);
    }

    public double[] getFabricWasteData() {
        List<Object[]> results = predictionRepository.getAvgWasteByFabric();
        return results.stream()
                .mapToDouble(row -> row[1] != null
                        ? ((Number) row[1]).doubleValue() : 0.0)
                .toArray();
    }

    // ─── Pattern Chart ────────────────────────────────────────────────────────
    public String[] getStyleLabels() {
        List<Object[]> results = predictionRepository.getAvgWasteByPattern();
        return results.stream()
                .map(row -> row[0] != null ? row[0].toString() : "Unknown")
                .toArray(String[]::new);
    }

    public double[] getStyleWasteData() {
        List<Object[]> results = predictionRepository.getAvgWasteByPattern();
        return results.stream()
                .mapToDouble(row -> row[1] != null
                        ? ((Number) row[1]).doubleValue() : 0.0)
                .toArray();
    }

    // ─── Last 5 Predictions ───────────────────────────────────────────────────
    public List<Map<String, Object>> getLast5Predictions() {
        List<Prediction> records = predictionRepository
                .findAllByOrderByCreatedAtDesc();
        List<Map<String, Object>> result = new ArrayList<>();
        int count = 0;
        for (Prediction r : records) {
            if (count >= 5) break;
            if (r.getPredictionResult() != null) {
                Map<String, Object> item = new HashMap<>();
                item.put("date",      r.getCreatedAt().toLocalDate().toString());
                item.put("predicted", r.getPredictionResult());
                double p = r.getPredictionResult();
                item.put("risk", p <= 5 ? "Low" : p <= 10 ? "Medium" : "High");
                result.add(item);
                count++;
            }
        }
        return result;
    }

    // ─── Material Accuracy ────────────────────────────────────────────────────
    public List<Map<String, Object>> getMaterialAccuracy() {
        List<Object[]> results = predictionRepository.getAccuracyByFabric();
        List<Map<String, Object>> list = new ArrayList<>();
        for (Object[] row : results) {
            Map<String, Object> item = new HashMap<>();
            item.put("material", row[0] != null ? row[0].toString() : "Unknown");
            item.put("accuracy", row[1] != null
                    ? ((Number) row[1]).doubleValue() : 0.0);
            list.add(item);
        }
        return list;
    }

    // ─── Prediction History ───────────────────────────────────────────────────
    public List<Map<String, Object>> getPredictionHistory() {
        List<Prediction> records = predictionRepository
                .findAllByOrderByCreatedAtDesc();
        List<Map<String, Object>> result = new ArrayList<>();
        for (Prediction r : records) {
            Map<String, Object> item = new HashMap<>();
            item.put("date",      r.getCreatedAt().toLocalDate().toString());
            item.put("predicted", r.getPredictionResult() != null
                    ? r.getPredictionResult() : "--");
            item.put("actual",    r.getActualResult() != null
                    ? r.getActualResult() : "--");
            double p = r.getPredictionResult() != null
                    ? r.getPredictionResult() : 0;
            item.put("risk", p <= 5 ? "Low" : p <= 10 ? "Medium" : "High");
            result.add(item);
        }
        return result;
    }

    // ─── Save Prediction ──────────────────────────────────────────────────────
    public void savePrediction(WasteRequest req, WasteResponse response) {
        try {
            java.time.LocalDate today = java.time.LocalDate.now();
            boolean exists = dailyWastageRepository.findAllOrderedByDate()
                    .stream()
                    .anyMatch(r -> r.getTrackingDate().equals(today));

            if (exists) {
                DailyWastage existing = dailyWastageRepository.findAllOrderedByDate()
                        .stream()
                        .filter(r -> r.getTrackingDate().equals(today))
                        .findFirst().get();
                existing.setPredictedWastagePct(response.getPredictedWaste());
                dailyWastageRepository.save(existing);
            } else {
                DailyWastage record = new DailyWastage();
                record.setTrackingDate(today);
                record.setPredictedWastagePct(response.getPredictedWaste());
                dailyWastageRepository.save(record);
            }
        } catch (Exception e) {
            System.out.println("Note: Could not save prediction — " + e.getMessage());
        }
    }

    // ─── Prediction Logic ─────────────────────────────────────────────────────
    private double getFabricMultiplier(String fabricType) {
        if (fabricType == null) return 1.0;
        if (fabricType.equalsIgnoreCase("silk"))           return 1.4;
        if (fabricType.equalsIgnoreCase("denim"))          return 1.2;
        if (fabricType.equalsIgnoreCase("wool flannel"))   return 1.3;
        if (fabricType.equalsIgnoreCase("polyester mesh")) return 1.15;
        if (fabricType.equalsIgnoreCase("linen"))          return 1.15;
        if (fabricType.equalsIgnoreCase("rayon"))          return 1.05;
        if (fabricType.equalsIgnoreCase("viscose"))        return 1.10;
        if (fabricType.equalsIgnoreCase("blended"))        return 1.10;
        if (fabricType.equalsIgnoreCase("polyester"))      return 1.10;
        if (fabricType.equalsIgnoreCase("cotton"))         return 1.0;
        if (fabricType.equalsIgnoreCase("pima cotton"))    return 1.0;
        if (fabricType.equalsIgnoreCase("organic cotton")) return 1.0;
        return 1.1;
    }

    private double getStyleAddition(String style) {
        if (style == null) return 0.0;
        if (style.equalsIgnoreCase("suit"))   return 3.0;
        if (style.equalsIgnoreCase("jacket")) return 2.8;
        if (style.equalsIgnoreCase("dress"))  return 2.5;
        if (style.equalsIgnoreCase("blouse")) return 2.0;
        if (style.equalsIgnoreCase("shirt"))  return 1.5;
        if (style.equalsIgnoreCase("skirt"))  return 1.3;
        if (style.equalsIgnoreCase("pant"))   return 1.0;
        if (style.equalsIgnoreCase("shorts")) return 0.8;
        return 1.5;
    }

    public WasteResponse calculateWaste(WasteRequest req) {
        // ── Input Validation ──
        List<String> errors = new ArrayList<>();

        if (req.getFabricWidth() <= 0)
            errors.add("Fabric width must be greater than 0");
        else if (req.getFabricWidth() > 500)
            errors.add("Fabric width must be between 1 and 500 cm");

        if (req.getLayers() <= 0)
            errors.add("Number of layers must be at least 1");
        else if (req.getLayers() > 200)
            errors.add("Number of layers must be between 1 and 200");

        if (req.getOrderQty() <= 0)
            errors.add("Order quantity must be greater than 0");
        else if (req.getOrderQty() > 100000)
            errors.add("Order quantity must be between 1 and 100,000");

        if (req.getFabricType() == null || req.getFabricType().trim().isEmpty())
            errors.add("Fabric type is required");

        if (req.getStyle() == null || req.getStyle().trim().isEmpty())
            errors.add("Style is required");

        if (!errors.isEmpty())
            throw new IllegalArgumentException(String.join(", ", errors));

        // ── Formula ──
        double baseWaste = (req.getLayers() * 0.5)
                + (req.getOrderQty() / 1000.0)
                + (10.0 / req.getFabricWidth());

        double predicted = Math.round(
                ((baseWaste * getFabricMultiplier(req.getFabricType()))
                        + getStyleAddition(req.getStyle())) * 100.0) / 100.0;

        String riskLevel, message;
        if (predicted <= 5) {
            riskLevel = "Low";
            message = "Waste is within acceptable limits.";
        } else if (predicted <= 10) {
            riskLevel = "Medium";
            message = "Moderate waste. Review cutting plan and layer count.";
        } else {
            riskLevel = "High";
            message = "High waste risk! Consider reducing layers or adjusting fabric width.";
        }

        return new WasteResponse(predicted, riskLevel, message);
    }

    // ─── Shift Chart ──────────────────────────────────────────
    public String[] getShiftLabels() {
        List<Object[]> results = cuttingRiskRecordRepository.getAvgWasteByShift();
        return results.stream()
                .map(row -> row[0] != null ? row[0].toString() : "Unknown")
                .toArray(String[]::new);
    }

    public double[] getShiftWasteData() {
        List<Object[]> results = cuttingRiskRecordRepository.getAvgWasteByShift();
        return results.stream()
                .mapToDouble(row -> row[1] != null
                        ? ((Number) row[1]).doubleValue() : 0.0)
                .toArray();
    }

    // ─── Cutting Method Chart ─────────────────────────────────
    public String[] getCuttingMethodLabels() {
        List<Object[]> results = predictionRepository.getAvgWasteByCuttingMethod();
        return results.stream()
                .map(row -> {
                    if (row[0] == null) return "Unknown";
                    String val = row[0].toString().trim();
                    return val.equals("0.0") ? "Manual" : "Auto";
                })
                .toArray(String[]::new);
    }

    public double[] getCuttingMethodData() {
        List<Object[]> results = predictionRepository.getAvgWasteByCuttingMethod();
        return results.stream()
                .mapToDouble(row -> row[1] != null
                        ? ((Number) row[1]).doubleValue() : 0.0)
                .toArray();
    }

    // ─── Risk Distribution ────────────────────────────────────
    public Map<String, Object> getRiskDistribution() {
        List<Prediction> records = predictionRepository
                .findAllByOrderByCreatedAtDesc();
        long low = 0, medium = 0, high = 0;
        for (Prediction r : records) {
            if (r.getPredictionResult() == null) continue;
            double p = r.getPredictionResult();
            if (p <= 5) low++;
            else if (p <= 10) medium++;
            else high++;
        }
        Map<String, Object> result = new HashMap<>();
        result.put("labels", new String[]{"Low", "Medium", "High"});
        result.put("data",   new long[]{low, medium, high});
        return result;
    }

    // ─── Accuracy Gap ─────────────────────────────────────────
    public String[] getAccuracyGapLabels() {
        List<Prediction> records = predictionRepository
                .findAllByOrderByCreatedAtDesc();
        Collections.reverse(records);
        return records.stream()
                .filter(r -> r.getActualResult() != null
                        && r.getPredictionResult() != null)
                .map(r -> r.getCreatedAt().toLocalDate().toString())
                .toArray(String[]::new);
    }

    public double[] getAccuracyGapData() {
        List<Prediction> records = predictionRepository
                .findAllByOrderByCreatedAtDesc();
        Collections.reverse(records);
        return records.stream()
                .filter(r -> r.getActualResult() != null
                        && r.getPredictionResult() != null)
                .mapToDouble(r -> Math.abs(
                        r.getPredictionResult() - r.getActualResult()))
                .toArray();
    }

    // ─── Summary KPIs ─────────────────────────────────────────
    public Map<String, Object> getSummaryKPIs() {
        Map<String, Object> summary = new HashMap<>();

        try {
            Double avg = predictionRepository.getAvgWaste();
            summary.put("avgWaste", avg != null
                    ? Math.round(avg * 100.0) / 100.0 : 0);
        } catch (Exception e) {
            summary.put("avgWaste", 0);
        }

        try {
            List<Prediction> records = predictionRepository
                    .findAllByOrderByCreatedAtDesc();
            long highRiskDays = records.stream()
                    .filter(r -> r.getPredictionResult() != null
                            && r.getPredictionResult() > 10)
                    .count();
            summary.put("highRiskDays", highRiskDays);
        } catch (Exception e) {
            summary.put("highRiskDays", 0);
        }

        try {
            List<Object[]> fabricData = predictionRepository.getAvgWasteByFabric();
            String bestFabric = fabricData.stream()
                    .filter(row -> row[0] != null && row[1] != null)
                    .min(Comparator.comparingDouble(
                            row -> ((Number) row[1]).doubleValue()))
                    .map(row -> row[0].toString())
                    .orElse("N/A");
            summary.put("bestFabric", bestFabric);
        } catch (Exception e) {
            summary.put("bestFabric", "N/A");
        }

        try {
            List<Object[]> shiftData = cuttingRiskRecordRepository.getAvgWasteByShift();
            String worstShift = shiftData.stream()
                    .filter(row -> row[0] != null && row[1] != null)
                    .max(Comparator.comparingDouble(
                            row -> ((Number) row[1]).doubleValue()))
                    .map(row -> row[0].toString())
                    .orElse("N/A");
            summary.put("worstShift", worstShift);
        } catch (Exception e) {
            summary.put("worstShift", "N/A");
        }

        return summary;
    }

    // ─── Get predictions for dropdown ─────────────────────
    public List<Map<String, Object>> getPredictionsForDropdown() {
        List<Prediction> records = predictionRepository.findAllByOrderByCreatedAtDesc();
        List<Map<String, Object>> list = new ArrayList<>();
        for (Prediction r : records) {
            if (r.getPredictionResult() != null) {
                Map<String, Object> item = new HashMap<>();
                item.put("id",        r.getId().toString());
                item.put("date",      r.getCreatedAt().toLocalDate().toString());
                item.put("predicted", r.getPredictionResult());
                double p = r.getPredictionResult();
                item.put("risk", p <= 5 ? "Low" : p <= 10 ? "Medium" : "High");
                list.add(item);
            }
        }
        return list;
    }

    // ─── Save cutting risk record ──────────────────────────
    public Map<String, Object> saveCuttingRiskRecord(CuttingRiskRequest req) {

        // ── Input Validation ──
        List<String> errors = new ArrayList<>();

        // Prediction selection
        if (req.getPredictionId() == null || req.getPredictionId().trim().isEmpty())
            errors.add("Prediction must be selected");

        // Layers
        if (req.getNoOfLayers() == null || req.getNoOfLayers() <= 0)
            errors.add("Number of layers must be at least 1");
        else if (req.getNoOfLayers() > 200)
            errors.add("Number of layers must be between 1 and 200");

        // Fabric GSM
        if (req.getFabricGsm() != null && req.getFabricGsm() < 50)
            errors.add("Fabric GSM must be between 50 and 500");
        else if (req.getFabricGsm() != null && req.getFabricGsm() > 500)
            errors.add("Fabric GSM must be between 50 and 500");

        // Cutting Overlap
        if (req.getCuttingOverlapMm() != null && req.getCuttingOverlapMm() < 0)
            errors.add("Cutting overlap cannot be negative");
        else if (req.getCuttingOverlapMm() != null && req.getCuttingOverlapMm() > 50)
            errors.add("Cutting overlap must be between 0 and 50 mm");

        // Marker Efficiency
        if (req.getMarkerEfficiencyPct() != null && req.getMarkerEfficiencyPct() < 0)
            errors.add("Marker efficiency cannot be negative");
        else if (req.getMarkerEfficiencyPct() != null && req.getMarkerEfficiencyPct() > 100)
            errors.add("Marker efficiency must be between 0 and 100%");

        // Cutting Method
        if (req.getCuttingMethod() == null || req.getCuttingMethod().trim().isEmpty())
            errors.add("Cutting method is required");

        // Shift
        if (req.getShift() == null || req.getShift().trim().isEmpty())
            errors.add("Shift is required");

        // Actual Waste
        if (req.getActualWastagePct() != null && req.getActualWastagePct() < 0)
            errors.add("Actual waste cannot be negative");
        else if (req.getActualWastagePct() != null && req.getActualWastagePct() > 100)
            errors.add("Actual waste must be between 0 and 100%");

        if (!errors.isEmpty())
            throw new IllegalArgumentException(String.join(", ", errors));

        // ── Save Record ──
        Map<String, Object> response = new HashMap<>();

        Prediction prediction = predictionRepository
                .findById(Long.parseLong(req.getPredictionId()))
                .orElseThrow(() ->
                        new IllegalArgumentException("Prediction not found"));

        CuttingRiskRecord record = new CuttingRiskRecord();
        record.setPrediction(prediction);
        record.setNoOfLayers(req.getNoOfLayers());
        record.setFabricGsm(req.getFabricGsm());
        record.setCuttingMethod(
                req.getCuttingMethod() != null ?
                        CuttingMethod.valueOf(req.getCuttingMethod()) : null);
        record.setShift(
                req.getShift() != null ?
                        ShiftType.valueOf(req.getShift()) : null);
        record.setCuttingOverlapMm(req.getCuttingOverlapMm());
        record.setMarkerEfficiencyPct(req.getMarkerEfficiencyPct());
        record.setActualWastagePct(req.getActualWastagePct());
        record.setJobDate(java.time.LocalDate.now());
        record.setNotes(req.getNotes());

        cuttingRiskRecordRepository.save(record);

        response.put("status",  "saved");
        response.put("message", "Cutting risk record saved successfully!");
        return response;
    }

    // ─── Shift data from risk records ─────────────────────
    public String[] getRiskShiftLabels() {
        List<Object[]> results =
                cuttingRiskRecordRepository.getAvgWasteByShift();
        return results.stream()
                .map(row -> row[0] != null ?
                        row[0].toString() : "Unknown")
                .toArray(String[]::new);
    }

    public double[] getRiskShiftData() {
        List<Object[]> results =
                cuttingRiskRecordRepository.getAvgWasteByShift();
        return results.stream()
                .mapToDouble(row -> row[1] != null ?
                        ((Number) row[1]).doubleValue() : 0.0)
                .toArray();
    }

    // ─── GSM data from risk records ───────────────────────
    public String[] getGsmLabels() {
        List<Object[]> results =
                cuttingRiskRecordRepository.getAvgWasteByGsm();
        return results.stream()
                .map(row -> row[0] != null ?
                        row[0].toString() : "Unknown")
                .toArray(String[]::new);
    }

    public double[] getGsmData() {
        List<Object[]> results =
                cuttingRiskRecordRepository.getAvgWasteByGsm();
        return results.stream()
                .mapToDouble(row -> row[1] != null ?
                        ((Number) row[1]).doubleValue() : 0.0)
                .toArray();
    }

}