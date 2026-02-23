package org.example.fabric_waste_prediction.Service;

import org.example.fabric_waste_prediction.Entity.Prediction;
import org.example.fabric_waste_prediction.Repository.PredictionRepository;
import org.springframework.stereotype.Service;


import java.util.List;

@Service
public class MetricsService {


    private final PredictionRepository predictionRepository;

        public MetricsService(PredictionRepository predictionRepository) {
            this.predictionRepository = predictionRepository;
        }

    public Double computeR2() {

        List<Prediction> completed = predictionRepository.findByActualResultNotNull();

        if (completed.size() < 100) return null;

        List<Double> actual = completed.stream()
                .map(Prediction::getActualResult)
                .toList();

        List<Double> predicted = completed.stream()
                .map(Prediction::getPredictionResult)
                .toList();

        return MetricsUtil.calculateR2(actual, predicted);
    }

    public Double computeMAE() {

        List<Prediction> completed = predictionRepository.findByActualResultNotNull();

        // Optional threshold (recommended)
        if (completed.size() < 50) return null;

        List<Double> actual = completed.stream()
                .map(Prediction::getActualResult)
                .toList();

        List<Double> predicted = completed.stream()
                .map(Prediction::getPredictionResult)
                .toList();

        return MetricsUtil.calculateMAE(actual, predicted);
    }
}
