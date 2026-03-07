package org.example.fabric_waste_prediction.client;

import org.example.fabric_waste_prediction.DTO.PredictionRequest;
import org.example.fabric_waste_prediction.DTO.PredictionResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@Service
public class MlApiClient {

    private final WebClient webClient;

    public MlApiClient(@Value("${ml.api.url}") String mlApiUrl) {
        this.webClient = WebClient.builder()
                .baseUrl(mlApiUrl)
                .build();
    }

    public PredictionResponse predict(PredictionRequest request) {

        return webClient.post()
                .uri("/predict")
                .header("Content-Type", "application/json")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(PredictionResponse.class)
                .block();  // block() makes it synchronous; use .subscribe() for async
    }
}