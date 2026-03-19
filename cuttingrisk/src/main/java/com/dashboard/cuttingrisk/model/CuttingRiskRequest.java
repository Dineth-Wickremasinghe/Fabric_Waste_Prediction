package com.dashboard.cuttingrisk.model;

public class CuttingRiskRequest {

    private String predictionId;
    private Integer noOfLayers;
    private Integer fabricGsm;
    private String cuttingMethod;
    private String shift;
    private Integer cuttingOverlapMm;
    private Double markerEfficiencyPct;
    private Double actualWastagePct;
    private String notes;

    // ── Getters ──
    public String getPredictionId()          { return predictionId; }
    public Integer getNoOfLayers()           { return noOfLayers; }
    public Integer getFabricGsm()            { return fabricGsm; }
    public String getCuttingMethod()         { return cuttingMethod; }
    public String getShift()                 { return shift; }
    public Integer getCuttingOverlapMm()     { return cuttingOverlapMm; }
    public Double getMarkerEfficiencyPct()   { return markerEfficiencyPct; }
    public Double getActualWastagePct()      { return actualWastagePct; }
    public String getNotes()                 { return notes; }

    // ── Setters ──
    public void setPredictionId(String id)        { this.predictionId = id; }
    public void setNoOfLayers(Integer n)           { this.noOfLayers = n; }
    public void setFabricGsm(Integer g)            { this.fabricGsm = g; }
    public void setCuttingMethod(String c)         { this.cuttingMethod = c; }
    public void setShift(String s)                 { this.shift = s; }
    public void setCuttingOverlapMm(Integer o)     { this.cuttingOverlapMm = o; }
    public void setMarkerEfficiencyPct(Double m)   { this.markerEfficiencyPct = m; }
    public void setActualWastagePct(Double a)      { this.actualWastagePct = a; }
    public void setNotes(String n)                 { this.notes = n; }
}