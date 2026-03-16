package org.example.fabric_waste_prediction;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EntityScan("org.example.fabric_waste_prediction.Entity")
@EnableJpaRepositories("org.example.fabric_waste_prediction.Repository")
public class FabricWastePredictionApplication {

    public static void main(String[] args) {
        SpringApplication.run(FabricWastePredictionApplication.class, args);
    }

}
