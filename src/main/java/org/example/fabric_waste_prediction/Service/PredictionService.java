package org.example.fabric_waste_prediction.Service;

import org.example.fabric_waste_prediction.DTO.PredictionRequest;
import org.example.fabric_waste_prediction.DTO.PredictionResponse;
import org.example.fabric_waste_prediction.Entity.Prediction;
import org.example.fabric_waste_prediction.Repository.PredictionRepository;
import org.example.fabric_waste_prediction.client.MlApiClient;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PredictionService {
    private final MlApiClient mlApiClient;
    private final PredictionRepository predictionRepository;

    public PredictionService(MlApiClient mlApiClient, PredictionRepository predictionRepository) {
        this.mlApiClient = mlApiClient;
        this.predictionRepository = predictionRepository;
    }

    public PredictionResponse getPredictionAndSave(PredictionRequest request) {

        // 1. Call FastAPI
        PredictionResponse response = mlApiClient.predict(request);

        // 2. Save to PostgreSQL
        Prediction record = new Prediction();
        record.setInputFeatures(request.getFeatures().toString()); // e.g. "[0.0, 13.0, 9.02...]"
        record.setPredictionResult((Double) response.getPrediction());
        predictionRepository.save(record);

        // 3. Return the response back to whoever called Spring Boot
        return response;
    }

    public List<Prediction> getAllPredictions() {
        return predictionRepository.findAllByOrderByCreatedAtDesc();
    }
}
