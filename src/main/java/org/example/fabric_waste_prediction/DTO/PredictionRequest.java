package org.example.fabric_waste_prediction.DTO;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

// PredictionRequest.java
@Getter
@Setter
public class PredictionRequest {
    @JsonIgnore
    private Double patternComplexity;
    @JsonIgnore
    private Double operatorExperience;
    @JsonIgnore
    private String fabricPattern;
    @JsonIgnore
    private Double cuttingMethod;
    @JsonIgnore
    private String fabricType;
    @JsonIgnore
    private Double markerLossPct;
    @JsonIgnore
    private Double fabricPatternEncoded;
    @JsonIgnore
    private Double fabricTypeEncoded;


    public PredictionRequest(Double patternComplexity, Double operatorExperience,
                             String fabricPattern, Double cuttingMethod,
                             String fabricType, Double markerLossPct) {
        this.patternComplexity = patternComplexity;
        this.operatorExperience = operatorExperience;
        this.fabricPattern = fabricPattern;
        this.cuttingMethod = cuttingMethod;
        this.fabricType = fabricType;
        this.markerLossPct = markerLossPct;
    }
    @JsonProperty("features")
    public List<Double> getFeatures() {
        return List.of(
                patternComplexity,
                operatorExperience,
                fabricPatternEncoded,  // encoded by TargetEncoderService
                cuttingMethod,
                fabricTypeEncoded,     // encoded by TargetEncoderService
                markerLossPct
        );
    }

}