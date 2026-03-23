package org.example.fabric_waste_prediction.Service;

import org.example.fabric_waste_prediction.Entity.CuttingJob;
import org.example.fabric_waste_prediction.Entity.CuttingMethod;
import org.example.fabric_waste_prediction.Entity.ShiftType;
import org.example.fabric_waste_prediction.Repository.CuttingJobRepository;
import org.example.fabric_waste_prediction.Repository.CuttingRiskRecordRepository;
import org.example.fabric_waste_prediction.Repository.PredictionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class HistoricalDataService {

    @Autowired
    private CuttingJobRepository cuttingJobRepository;

    @Autowired
    private CuttingRiskRecordRepository cuttingRiskRecordRepository;

    @Autowired
    private PredictionRepository predictionRepository;

    // ── Get all cutting jobs ──────────────────────────────────────────────────
    public List<CuttingJob> getAllCuttingJobs() {
        return cuttingJobRepository.findAll();
    }

    // ── Update cutting job + sync back to prediction and cutting_risk_records ──
    public String updateCuttingJob(Long id, CuttingJob updated) {
        Optional<CuttingJob> existing = cuttingJobRepository.findById(id);
        if (existing.isEmpty()) return "Record not found!";

        CuttingJob job = existing.get();

        // Update cutting_jobs table
        job.setFabricType(updated.getFabricType());
        job.setFabricPattern(updated.getFabricPattern());
        job.setCuttingMethod(updated.getCuttingMethod());
        job.setMarkerLossPct(updated.getMarkerLossPct());
        job.setPatternComplexity(updated.getPatternComplexity());
        job.setOperatorExperience(updated.getOperatorExperience());
        job.setPredictedWastePct(updated.getPredictedWastePct());
        job.setNoOfLayers(updated.getNoOfLayers());
        job.setFabricGsm(updated.getFabricGsm());
        job.setShift(updated.getShift());
        job.setCuttingOverlapMm(updated.getCuttingOverlapMm());
        job.setMarkerEfficiencyPct(updated.getMarkerEfficiencyPct());
        job.setActualWastagePct(updated.getActualWastagePct());
        job.setJobDate(updated.getJobDate());
        job.setNotes(updated.getNotes());
        cuttingJobRepository.save(job);

        // Sync back to prediction table
        if (job.getPredictionId() != null) {
            predictionRepository.findById(job.getPredictionId()).ifPresent(prediction -> {
                prediction.setFabricType(updated.getFabricType());
                prediction.setFabricPattern(updated.getFabricPattern());
                prediction.setCuttingMethod(updated.getCuttingMethod());
                prediction.setMarkerLossPct(updated.getMarkerLossPct());
                prediction.setPatternComplexity(updated.getPatternComplexity());
                prediction.setOperatorExperience(updated.getOperatorExperience());
                prediction.setPredictionResult(updated.getPredictedWastePct());
                prediction.setActualResult(updated.getActualWastagePct()); // ✅ fixed
                predictionRepository.save(prediction);
                System.out.println("✅ prediction table synced!");
            });
        }

        // Sync back to cutting_risk_records table
        if (job.getCuttingRiskRecordId() != null) {
            cuttingRiskRecordRepository.findById(job.getCuttingRiskRecordId()).ifPresent(record -> {
                record.setNoOfLayers(updated.getNoOfLayers());
                record.setFabricGsm(updated.getFabricGsm());
                record.setCuttingOverlapMm(updated.getCuttingOverlapMm());
                record.setMarkerEfficiencyPct(updated.getMarkerEfficiencyPct());
                record.setActualWastagePct(updated.getActualWastagePct());
                record.setJobDate(updated.getJobDate());
                record.setNotes(updated.getNotes());

                // ✅ Convert String → Enum safely
                if (updated.getShift() != null) {
                    try {
                        record.setShift(ShiftType.valueOf(updated.getShift().toUpperCase()));
                    } catch (IllegalArgumentException e) {
                        System.out.println("⚠️ Invalid shift value: " + updated.getShift());
                    }
                }
                if (updated.getCuttingMethod() != null) {
                    try {
                        record.setCuttingMethod(CuttingMethod.valueOf(updated.getCuttingMethod().toUpperCase()));
                    } catch (IllegalArgumentException e) {
                        System.out.println("⚠️ Invalid cutting method value: " + updated.getCuttingMethod());
                    }
                }

                cuttingRiskRecordRepository.save(record);
                System.out.println("✅ cutting_risk_records table synced!");
            });
        }

        return "success";
    }

    // ── Delete cutting job + delete from other tables too ─────────────────────
    public void deleteCuttingJob(Long id) {
        cuttingJobRepository.findById(id).ifPresent(job -> {
            // Delete from cutting_risk_records first (FK constraint)
            if (job.getCuttingRiskRecordId() != null) {
                cuttingRiskRecordRepository.deleteById(job.getCuttingRiskRecordId());
                System.out.println("✅ cutting_risk_records deleted!");
            }
            // Delete from prediction table
            if (job.getPredictionId() != null) {
                predictionRepository.deleteById(job.getPredictionId());
                System.out.println("✅ prediction deleted!");
            }
            // Delete from cutting_jobs last
            cuttingJobRepository.deleteById(id);
            System.out.println("✅ cutting_jobs deleted!");
        });
    }
}