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

    @Column(name = "report_name", nullable = false, length = 150)
    private String reportName;

    @Column(name = "generated_by")
    private UUID generatedBy;

    @Column(name = "generated_at")
    private ZonedDateTime generatedAt;

    @Column(name = "filters_used", length = 300)
    private String filtersUsed;

    @Column(name = "record_count")
    private Integer recordCount;

    @Column(name = "status", length = 20)
    private String status;

    // Constructors
    public Report() {}

    // Getters and Setters
    public UUID getId()                        { return id; }
    public void setId(UUID id)                 { this.id = id; }

    public String getReportName()              { return reportName; }
    public void setReportName(String v)        { this.reportName = v; }

    public UUID getGeneratedBy()               { return generatedBy; }
    public void setGeneratedBy(UUID v)         { this.generatedBy = v; }

    public ZonedDateTime getGeneratedAt()      { return generatedAt; }
    public void setGeneratedAt(ZonedDateTime v){ this.generatedAt = v; }

    public String getFiltersUsed()             { return filtersUsed; }
    public void setFiltersUsed(String v)       { this.filtersUsed = v; }

    public Integer getRecordCount()            { return recordCount; }
    public void setRecordCount(Integer v)      { this.recordCount = v; }

    public String getStatus()                  { return status; }
    public void setStatus(String v)            { this.status = v; }
}
