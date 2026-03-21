package org.example.fabric_waste_prediction.Entity;

public class WasteResponse {
    private double predictedWaste;
    private String riskLevel;
    private String message;

    public WasteResponse(double predictedWaste, String riskLevel, String message) {
        this.predictedWaste = predictedWaste;
        this.riskLevel = riskLevel;
        this.message = message;
    }

    public double getPredictedWaste() { return predictedWaste; }
    public String getRiskLevel() { return riskLevel; }
    public String getMessage() { return message; }
}
