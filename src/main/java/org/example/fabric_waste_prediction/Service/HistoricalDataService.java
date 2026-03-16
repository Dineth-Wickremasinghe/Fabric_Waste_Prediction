package org.example.fabric_waste_prediction.Service;

import org.example.fabric_waste_prediction.Entity.CuttingJob;
import org.example.fabric_waste_prediction.Entity.ModelPerformance;
import org.example.fabric_waste_prediction.Repository.CuttingJobRepository;
import org.example.fabric_waste_prediction.Repository.ModelPerformanceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class HistoricalDataService {

    @Autowired
    private CuttingJobRepository cuttingJobRepository;

    @Autowired
    private ModelPerformanceRepository modelPerformanceRepository;

    // ── Cutting Jobs ──────────────────────────────────────────────────────────

    public List<CuttingJob> getAllCuttingJobs() {
        return cuttingJobRepository.findAll();
    }

    public Optional<CuttingJob> getCuttingJobById(Long id) {
        return cuttingJobRepository.findById(id);
    }

    public String updateCuttingJob(Long id, CuttingJob updated) {
        Optional<CuttingJob> existing = cuttingJobRepository.findById(id);
        if (existing.isEmpty()) return "Cutting job not found!";

        CuttingJob job = existing.get();

        // Check jobId conflict with another record
        Optional<CuttingJob> byJobId = cuttingJobRepository.findByJobId(updated.getJobId());
        if (byJobId.isPresent() && !byJobId.get().getId().equals(id)) {
            return "Job ID already exists!";
        }

        job.setJobId(updated.getJobId());
        job.setFabricLotId(updated.getFabricLotId());
        job.setMaterialId(updated.getMaterialId());
        job.setOperatorId(updated.getOperatorId());
        job.setCuttingOverlapMm(updated.getCuttingOverlapMm());
        job.setNoOfLayers(updated.getNoOfLayers());
        job.setCuttingMethod(updated.getCuttingMethod());
        job.setShift(updated.getShift());
        job.setActualWastagePct(updated.getActualWastagePct());
        job.setPredictedWastePct(updated.getPredictedWastePct());
        job.setMarkerEfficiencyPct(updated.getMarkerEfficiencyPct());
        job.setStatus(updated.getStatus());
        job.setJobDate(updated.getJobDate());

        cuttingJobRepository.save(job);
        return "success";
    }

    public void deleteCuttingJob(Long id) {
        cuttingJobRepository.deleteById(id);
    }

    // ── Model Performance ─────────────────────────────────────────────────────

    public List<ModelPerformance> getAllModelPerformance() {
        return modelPerformanceRepository.findAll();
    }

    public Optional<ModelPerformance> getModelPerformanceById(Long id) {
        return modelPerformanceRepository.findById(id);
    }

    public String updateModelPerformance(Long id, ModelPerformance updated) {
        Optional<ModelPerformance> existing = modelPerformanceRepository.findById(id);
        if (existing.isEmpty()) return "Record not found!";

        ModelPerformance record = existing.get();
        record.setRecordedAt(updated.getRecordedAt());
        record.setMseError(updated.getMseError());
        record.setHealthStatus(updated.getHealthStatus());

        modelPerformanceRepository.save(record);
        return "success";
    }

    public void deleteModelPerformance(Long id) {
        modelPerformanceRepository.deleteById(id);
    }
}
