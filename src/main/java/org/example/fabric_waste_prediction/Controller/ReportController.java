package org.example.fabric_waste_prediction.Controller;

import org.example.fabric_waste_prediction.Entity.Report;
import org.example.fabric_waste_prediction.Repository.ReportRepository;
import org.example.fabric_waste_prediction.Service.PdfGenerationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Controller
public class ReportController {

    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    private PdfGenerationService pdfGenerationService;

    // ── Load report page — fetch saved reports from DB ──
    @GetMapping("/report")
    public String showReport(Model model) {
        List<Report> reports = reportRepository.findAll();
        model.addAttribute("reports", reports);
        model.addAttribute("reportCount", reports.size());
        return "report";
    }

    // ── Save report to DB when Generate Report is clicked ──
    @PostMapping("/api/report/save")
    @ResponseBody
    public Map<String, Object> saveReport(@RequestParam String reportName) {
        Report report = new Report();
        report.setReportName(reportName);
        report.setGeneratedAt(ZonedDateTime.now());
        reportRepository.save(report);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Report saved to database");
        response.put("id", report.getId());
        return response;
    }

    // ── PDF Generation ──
    @PostMapping(value = "/api/report/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> generatePdfReport(
            @RequestParam(required = false) String year,
            @RequestParam(required = false) String monthFrom,
            @RequestParam(required = false) String monthTo,
            @RequestParam(required = false) String fabric,
            @RequestParam(required = false) String shift,
            @RequestParam(required = false) String cutting,
            @RequestParam String data) throws IOException {

        List<Map<String, Object>> reportData = parseReportData(data);


        Map<String, String> filters = new HashMap<>();
        filters.put("year",      year      != null ? year      : "All");
        filters.put("monthFrom", monthFrom != null ? monthFrom : "All");
        filters.put("monthTo",   monthTo   != null ? monthTo   : "All");
        filters.put("fabric",    fabric    != null ? fabric    : "All");
        filters.put("shift",     shift     != null ? shift     : "All");
        filters.put("cutting",   cutting   != null ? cutting   : "All");

        Map<String, Object> kpiData = calculateKPI(reportData);

        byte[] pdfContents = pdfGenerationService.generateReportPdf(reportData, filters, kpiData);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("filename",
                "fabric_waste_report_" + System.currentTimeMillis() + ".pdf");
        headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");

        return ResponseEntity.ok().headers(headers).body(pdfContents);
    }

    private List<Map<String, Object>> parseReportData(String jsonData) {
        List<Map<String, Object>> result = new ArrayList<>();
        try {
            jsonData = jsonData.trim().replaceAll("^\\[|\\]$", "");
            String[] objects = jsonData.split("\\},\\{");
            for (String obj : objects) {
                obj = obj.replaceAll("[\\[\\]{}]", "");
                Map<String, Object> map = new HashMap<>();
                for (String pair : obj.split(",(?![^\\[\\]]*\\])")) {
                    String[] kv = pair.split(":", 2);
                    if (kv.length == 2) {
                        String key = kv[0].replaceAll("\"", "").trim();
                        String val = kv[1].replaceAll("\"", "").trim();
                        try {
                            if (val.contains(".")) map.put(key, Double.parseDouble(val));
                            else map.put(key, Integer.parseInt(val));
                        } catch (NumberFormatException e) {
                            map.put(key, val);
                        }
                    }
                }
                if (!map.isEmpty()) result.add(map);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    // keep this exactly as before
    private List<Map<String, Object>> getDummyReportData() {
        List<Map<String, Object>> data = new ArrayList<>();
        Object[][] rows = {
                {2020,1,"Viscose","Checked",150,"Night","Manual",8.17,8.90},
                {2020,1,"Polyester","Checked",140,"Night","Manual",7.48,8.10},
                {2020,2,"Denim","Solid",210,"Day","Auto",5.18,5.85},
                {2020,3,"Cotton","Striped",170,"Night","Manual",12.24,13.09},
                {2020,4,"Denim","Floral",170,"Night","Manual",12.15,13.09},
                {2020,5,"Twill","Floral",150,"Night","Manual",8.10,8.83}
        };
        for (Object[] row : rows) {
            Map<String, Object> map = new HashMap<>();
            map.put("year", row[0]); map.put("month", row[1]);
            map.put("fabric", row[2]); map.put("pattern", row[3]);
            map.put("gsm", row[4]); map.put("shift", row[5]);
            map.put("cutting", row[6]); map.put("actual", row[7]);
            map.put("predicted", row[8]);
            data.add(map);
        }
        return data;
    }

    private Map<String, Object> calculateKPI(List<Map<String, Object>> data) {
        Map<String, Object> kpi = new HashMap<>();
        if (data.isEmpty()) {
            kpi.put("totalRecords", 0);
            kpi.put("avgActual", 0.0);
            kpi.put("avgPredicted", 0.0);
            kpi.put("maxWaste", 0.0);
            kpi.put("minWaste", 0.0);
            return kpi;
        }
        double sumActual = 0, sumPredicted = 0;
        double maxActual = Double.MIN_VALUE, minActual = Double.MAX_VALUE;
        for (Map<String, Object> row : data) {
            double actual    = (double) row.get("actual");
            double predicted = (double) row.get("predicted");
            sumActual    += actual;
            sumPredicted += predicted;
            maxActual = Math.max(maxActual, actual);
            minActual = Math.min(minActual, actual);
        }
        kpi.put("totalRecords",  data.size());
        kpi.put("avgActual",     Math.round((sumActual    / data.size()) * 100.0) / 100.0);
        kpi.put("avgPredicted",  Math.round((sumPredicted / data.size()) * 100.0) / 100.0);
        kpi.put("maxWaste",      maxActual);
        kpi.put("minWaste",      minActual);
        return kpi;
    }
}
