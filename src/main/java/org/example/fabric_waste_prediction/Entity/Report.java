package org.example.fabric_waste_prediction.Entity;

import jakarta.persistence.*;
import java.time.ZonedDateTime;
import java.util.UUID;

@Entity
@Table(name = "reports")
public class Report {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "report_name", nullable = false, length = 100)
    private String reportName;

    @Column(name = "generated_by")
    private UUID generatedBy;

    @Column(name = "generated_at")
    private ZonedDateTime generatedAt;

    // Constructors
    public Report() {}

    public Report(String reportName, UUID generatedBy) {
        this.reportName = reportName;
        this.generatedBy = generatedBy;
        this.generatedAt = ZonedDateTime.now();
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getReportName() { return reportName; }
    public void setReportName(String reportName) { this.reportName = reportName; }

    public UUID getGeneratedBy() { return generatedBy; }
    public void setGeneratedBy(UUID generatedBy) { this.generatedBy = generatedBy; }

    public ZonedDateTime getGeneratedAt() { return generatedAt; }
    public void setGeneratedAt(ZonedDateTime generatedAt) { this.generatedAt = generatedAt; }
}
