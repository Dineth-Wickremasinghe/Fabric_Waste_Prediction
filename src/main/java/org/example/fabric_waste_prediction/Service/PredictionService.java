package org.example.fabric_waste_prediction.Service;

import org.example.fabric_waste_prediction.DTO.PredictionRequest;
import org.example.fabric_waste_prediction.DTO.PredictionResponse;
import org.example.fabric_waste_prediction.Entity.Prediction;
import org.example.fabric_waste_prediction.Exception.PredictionServiceUnavailableException;
import org.example.fabric_waste_prediction.Repository.PredictionRepository;
import org.example.fabric_waste_prediction.client.MlApiClient;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.List;

@Service
public class PredictionService {
    private final MlApiClient mlApiClient;
    private final PredictionRepository predictionRepository;
    private final TargetEncoderService targetEncoderService;

    public PredictionService(MlApiClient mlApiClient, PredictionRepository predictionRepository, TargetEncoderService targetEncoderService) {
        this.targetEncoderService = targetEncoderService;
        this.mlApiClient = mlApiClient;
        this.predictionRepository = predictionRepository;
    }

    public PredictionResponse getPredictionAndSave(PredictionRequest request) {


        request.setFabricTypeEncoded(targetEncoderService.encodeFabricType(request.getFabricType()));
        request.setFabricPatternEncoded(targetEncoderService.encodeFabricPattern(request.getFabricPattern()));

        PredictionResponse response;
        try {
            response = mlApiClient.predict(request);
        } catch (WebClientRequestException e) {
            // WebClient couldn't reach FastAPI — connection refused, timeout, etc.
            throw new PredictionServiceUnavailableException("ML service is unreachable", e);
        } catch (WebClientResponseException e) {
            // WebClient reached FastAPI but got a 4xx/5xx back
            throw new PredictionServiceUnavailableException("ML service returned an error", e);
        }

        // 2. Save to PostgreSQL
        Prediction record = new Prediction();

        record.setPatternComplexity(request.getPatternComplexity());
        record.setOperatorExperience(request.getOperatorExperience());
        record.setFabricPattern(request.getFabricPattern());
        record.setCuttingMethod(String.valueOf(request.getCuttingMethod()));
        record.setFabricType(request.getFabricType());
        record.setMarkerLossPct(request.getMarkerLossPct());
        record.setInputFeatures(String.format("[%s, %s, %s, %s, %s, %s]",
                request.getPatternComplexity(), request.getOperatorExperience(),
                request.getFabricPattern(), request.getCuttingMethod(),
                request.getFabricType(), request.getMarkerLossPct()));
        record.setPredictionResult((Double) response.getPrediction());
        predictionRepository.save(record);


        return response;
    }

    public List<Prediction> getAllPredictions() {
        return predictionRepository.findAllByOrderByCreatedAtDesc();
    }
}
