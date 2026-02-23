package org.example.fabric_waste_prediction.DTO;

import java.util.List;

// PredictionRequest.java
public class PredictionRequest {
    private List<Double> features;

    public PredictionRequest(List<Double> features) {
        this.features = features;
    }

    public List<Double> getFeatures() { return features; }
    public void setFeatures(List<Double> features) { this.features = features; }
}