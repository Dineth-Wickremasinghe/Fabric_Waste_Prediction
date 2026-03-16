package org.example.fabric_waste_prediction.Entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDate;

@Entity
@Table(name = "waste_tracking")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WasteTracking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tracking_date", nullable = false)
    private LocalDate trackingDate;

    @Column(name = "destination")
    private String destination;

    @Column(name = "amount_kg")
    private Double amountKg;

    @Column(name = "fabric_type")
    private String fabricType;

    @Column(name = "recycled_percentage")
    private Double recycledPercentage;

    @Column(name = "created_at")
    private LocalDate createdAt;
}