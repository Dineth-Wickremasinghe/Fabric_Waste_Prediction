package org.example.fabric_waste_prediction.Exception;

public class PredictionServiceUnavailableException extends RuntimeException {
    public PredictionServiceUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}