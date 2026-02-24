package org.example.fabric_waste_prediction;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class FabricWastePredictionApplication {

    public static void main(String[] args) {
        SpringApplication.run(FabricWastePredictionApplication.class, args);
    }

}
