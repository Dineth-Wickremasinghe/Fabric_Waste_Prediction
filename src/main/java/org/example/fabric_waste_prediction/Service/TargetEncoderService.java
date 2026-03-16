package org.example.fabric_waste_prediction.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.Map;

@Service
public class TargetEncoderService {

    private Map<String, Double> fabricTypeEncoding;
    private Map<String, Double> fabricPatternEncoding;

    @PostConstruct
    public void loadMappings() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        InputStream is = getClass().getResourceAsStream("/target_encoding_mappings.json");

        Map<String, Map<String, Double>> mappings = mapper.readValue(is,
                new TypeReference<>() {});

        fabricTypeEncoding    = mappings.get("Fabric_Type");
        fabricPatternEncoding = mappings.get("Fabric_Pattern");
    }

    public Double encodeFabricType(String value) {
        Double encoded = fabricTypeEncoding.get(value);
        if (encoded == null) throw new IllegalArgumentException("Unknown Fabric_Type: " + value);
        return encoded;
    }

    public Double encodeFabricPattern(String value) {
        Double encoded = fabricPatternEncoding.get(value);
        if (encoded == null) throw new IllegalArgumentException("Unknown Fabric_Pattern: " + value);
        return encoded;
    }
}