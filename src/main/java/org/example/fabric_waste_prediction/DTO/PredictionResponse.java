package org.example.fabric_waste_prediction.DTO;

public class PredictionResponse {
    private Double prediction;
    private Double confidence;

    public Object getPrediction() { return prediction; }
    public void setPrediction(Double prediction) { this.prediction = prediction; }
    public Double getConfidence() { return confidence; }
    public void setConfidence(Double confidence) { this.confidence = confidence; }
}
