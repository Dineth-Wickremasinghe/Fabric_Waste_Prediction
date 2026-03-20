package org.example.fabric_waste_prediction.Service;



import java.util.List;


public class MetricsUtil {

    public static Double calculateR2(List<Double> actual, List<Double> predicted) {

        if (actual.size() < 2) return null; // not enough data

        double mean = actual.stream()
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0);

        double ssTotal = 0.0;
        double ssResidual = 0.0;

        for (int i = 0; i < actual.size(); i++) {
            ssTotal += Math.pow(actual.get(i) - mean, 2);
            ssResidual += Math.pow(actual.get(i) - predicted.get(i), 2);
        }

        return 1 - (ssResidual / ssTotal);
    }

    public static Double calculateMAE(List<Double> actual, List<Double> predicted) {
        double sum = 0.0;

        for (int i = 0; i < actual.size(); i++) {
            sum += Math.abs(actual.get(i) - predicted.get(i));
        }

        return sum / actual.size();
    }
}