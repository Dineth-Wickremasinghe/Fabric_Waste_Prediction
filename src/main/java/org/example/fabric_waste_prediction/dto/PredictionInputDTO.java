package org.example.fabric_waste_prediction.DTO;

import jakarta.validation.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PredictionInputDTO {

    @NotBlank(message = "Fabric type is required")
    private String fabricType;

    @NotBlank(message = "Fabric pattern is required")
    private String fabricPattern;

    @NotNull(message = "Fabric GSM is required")
    @Min(value = 50, message = "GSM must be at least 50")
    @Max(value = 500, message = "GSM must not exceed 500")
    private Integer fabricGSM;

    @NotNull(message = "Fabric width is required")
    @Min(value = 30, message = "Width must be at least 30 inches")
    @Max(value = 120, message = "Width must not exceed 120 inches")
    private Integer fabricWidth;

    @NotNull(message = "Number of layers is required")
    @Min(value = 1, message = "At least 1 layer required")
    @Max(value = 100, message = "Layers must not exceed 100")
    private Integer numberOfLayers;

    @NotNull(message = "Order quantity is required")
    @Min(value = 10, message = "Order quantity must be at least 10 meters")
    private Double orderQuantity;
}