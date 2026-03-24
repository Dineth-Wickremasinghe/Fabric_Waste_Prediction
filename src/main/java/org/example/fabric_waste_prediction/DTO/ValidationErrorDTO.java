package org.example.fabric_waste_prediction.DTO;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.List;
import java.util.Map;
import org.example.fabric_waste_prediction.DTO.ValidationErrorDTO;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ValidationErrorDTO {
    private String field;
    private String message;
    private String rejectedValue;
    private String errorCode;

    // For multiple errors
    private Map<String, List<String>> fieldErrors;
    private List<String> globalErrors;

    // For real-time validation
    private Boolean isValid;
    private List<String> warnings;
    private Map<String, String> suggestions;

    public ValidationErrorDTO(String field, String message) {
        this.field = field;
        this.message = message;
    }
}