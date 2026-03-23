package org.example.fabric_waste_prediction.Controller;

import org.example.fabric_waste_prediction.Entity.CuttingJob;
import org.example.fabric_waste_prediction.Entity.Report;
import org.example.fabric_waste_prediction.Repository.CuttingJobRepository;
import org.example.fabric_waste_prediction.Repository.ReportRepository;
import org.example.fabric_waste_prediction.Service.PdfGenerationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

@Controller
public class ReportController {

    @Autowired private ReportRepository reportRepository;
    @Autowired private CuttingJobRepository cuttingJobRepository;
    @Autowired private PdfGenerationService pdfGenerationService;

    // ── Load report page ──
    @GetMapping("/report")
    public String showReport(Model model) {
        List<CuttingJob> allJobs = cuttingJobRepository.findAll();

        List<Integer> years = allJobs.stream()
                .filter(j -> j.getJobDate() != null)
                .map(j -> j.getJobDate().getYear())
                .distinct().sorted().collect(Collectors.toList());

        List<String> fabricTypes = allJobs.stream()
                .filter(j -> j.getFabricType() != null)
                .map(CuttingJob::getFabricType)
                .distinct().sorted().collect(Collectors.toList());

        List<String> shifts = allJobs.stream()
                .filter(j -> j.getShift() != null)
                .map(CuttingJob::getShift)
                .distinct().sorted().collect(Collectors.toList());

        List<String> cuttingMethods = allJobs.stream()
                .filter(j -> j.getCuttingMethod() != null)
                .map(CuttingJob::getCuttingMethod)
                .distinct().sorted().collect(Collectors.toList());

        List<Map<String, Object>> jobList = jobsToMapList(allJobs);
        Map<String, Object> kpis = calculateKPI(jobList);

        // Dashboard summary stats
        List<Report> reports = reportRepository.findAllByOrderByGeneratedAtDesc();
        long todayCount = 0;
        String lastReportName = "No reports yet";
        String lastReportTime = "—";

        ZonedDateTime startOfDay = LocalDate.now().atStartOfDay(ZoneId.systemDefault());
        for (Report r : reports) {
            if (r.getGeneratedAt() != null && r.getGeneratedAt().isAfter(startOfDay)) {
                todayCount++;
            }
        }
        if (!reports.isEmpty() && reports.get(0).getGeneratedAt() != null) {
            lastReportName = reports.get(0).getReportName();
            lastReportTime = reports.get(0).getGeneratedAt()
                    .withZoneSameInstant(ZoneId.of("Asia/Colombo"))
                    .format(DateTimeFormatter.ofPattern("dd MMM yyyy · HH:mm"));
        }

        model.addAttribute("jobs",           jobList);
        model.addAttribute("totalRecords",   allJobs.size());
        model.addAttribute("years",          years);
        model.addAttribute("fabricTypes",    fabricTypes);
        model.addAttribute("shifts",         shifts);
        model.addAttribute("cuttingMethods", cuttingMethods);
        model.addAttribute("kpis",           kpis);
        model.addAttribute("reports",        reports);
        model.addAttribute("reportCount",    reports.size());
        model.addAttribute("todayCount",     todayCount);
        model.addAttribute("lastReportName", lastReportName);
        model.addAttribute("lastReportTime", lastReportTime);
        return "report";
    }

    // ── Filter API ──
    @GetMapping("/api/report/filter")
    @ResponseBody
    public Map<String, Object> filterJobs(
            @RequestParam(required = false) String year,
            @RequestParam(required = false) String monthFrom,
            @RequestParam(required = false) String monthTo,
            @RequestParam(required = false) String fabric,
            @RequestParam(required = false) String shift,
            @RequestParam(required = false) String cutting) {

        List<CuttingJob> allJobs = cuttingJobRepository.findAll();
        List<CuttingJob> filtered = allJobs.stream().filter(job -> {
            if (job.getJobDate() == null) return false;
            int jobYear  = job.getJobDate().getYear();
            int jobMonth = job.getJobDate().getMonthValue();
            if (year != null && !year.equals("all") && jobYear != Integer.parseInt(year)) return false;
            if (monthFrom != null && !monthFrom.equals("0") && jobMonth < Integer.parseInt(monthFrom)) return false;
            if (monthTo   != null && !monthTo.equals("0")   && jobMonth > Integer.parseInt(monthTo))   return false;
            if (fabric != null && !fabric.equals("all") && !fabric.equalsIgnoreCase(job.getFabricType())) return false;
            if (shift   != null && !shift.equals("all")   && !shift.equalsIgnoreCase(job.getShift()))         return false;
            if (cutting != null && !cutting.equals("all") && !cutting.equalsIgnoreCase(job.getCuttingMethod()))return false;
            return true;
        }).collect(Collectors.toList());

        List<Map<String, Object>> jobList = jobsToMapList(filtered);
        Map<String, Object> kpis = calculateKPI(jobList);

        Map<String, Object> response = new HashMap<>();
        response.put("jobs",  jobList);
        response.put("kpis",  kpis);
        response.put("count", filtered.size());
        return response;
    }

    // ── Save report to DB ──
    @PostMapping("/api/report/save")
    @ResponseBody
    public Map<String, Object> saveReport(
            @RequestParam String reportName,
            @RequestParam(required = false, defaultValue = "0") Integer recordCount,
            @RequestParam(required = false, defaultValue = "") String filtersUsed) {

        Report report = new Report();
        report.setReportName(reportName);
        report.setGeneratedAt(ZonedDateTime.now(ZoneId.of("Asia/Colombo")));
        report.setRecordCount(recordCount);
        report.setFiltersUsed(filtersUsed);
        report.setStatus("GENERATED");
        reportRepository.save(report);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Report saved to database");
        response.put("id",      report.getId());
        return response;
    }

    // ── Delete report ──
    @DeleteMapping("/api/report/{id}")
    @ResponseBody
    public Map<String, Object> deleteReport(@PathVariable UUID id) {
        Map<String, Object> response = new HashMap<>();
        try {
            reportRepository.deleteById(id);
            response.put("success", true);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        return response;
    }

    // ── Get all reports as JSON (for modal) ──
    @GetMapping("/api/report/list")
    @ResponseBody
    public List<Map<String, Object>> getReportList() {
        List<Report> reports = reportRepository.findAllByOrderByGeneratedAtDesc();
        return reports.stream().map(r -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id",          r.getId());
            map.put("reportName",  r.getReportName());
            map.put("generatedAt", r.getGeneratedAt() != null ?
                    r.getGeneratedAt().withZoneSameInstant(ZoneId.of("Asia/Colombo"))
                            .format(DateTimeFormatter.ofPattern("dd MMM yyyy · HH:mm")) : "—");
            map.put("recordCount", r.getRecordCount()  != null ? r.getRecordCount()  : 0);
            map.put("filtersUsed", r.getFiltersUsed()  != null ? r.getFiltersUsed()  : "—");
            map.put("status",      r.getStatus()       != null ? r.getStatus()       : "GENERATED");
            return map;
        }).collect(Collectors.toList());
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
        Map<String, String> filters = buildFiltersMap(year, monthFrom, monthTo, fabric, shift, cutting);
        Map<String, Object> kpiData = calculateKPI(reportData);
        byte[] pdfContents = pdfGenerationService.generateReportPdf(reportData, filters, kpiData);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("filename",
                "fabric_waste_report_" + System.currentTimeMillis() + ".pdf");
        headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");
        return ResponseEntity.ok().headers(headers).body(pdfContents);
    }

    // ── Trends PDF (charts image data) ──
    @PostMapping(value = "/api/report/trends-pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> generateTrendsPdf(
            @RequestParam(required = false) String year,
            @RequestParam(required = false) String monthFrom,
            @RequestParam(required = false) String monthTo,
            @RequestParam(required = false) String fabric,
            @RequestParam(required = false) String shift,
            @RequestParam(required = false) String cutting,
            @RequestParam String trendChart,
            @RequestParam String fabricChart,
            @RequestParam String shiftChart,
            @RequestParam String cuttingChart,
            @RequestParam(required = false, defaultValue = "0") Integer recordCount) throws IOException {

        Map<String, String> filters = buildFiltersMap(year, monthFrom, monthTo, fabric, shift, cutting);
        byte[] pdfContents = pdfGenerationService.generateTrendsPdf(
                filters, trendChart, fabricChart, shiftChart, cuttingChart, recordCount);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("filename",
                "fabric_waste_trends_" + System.currentTimeMillis() + ".pdf");
        headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");
        return ResponseEntity.ok().headers(headers).body(pdfContents);
    }

    // ── Excel Export ──
    @PostMapping(value = "/api/report/excel")
    public ResponseEntity<byte[]> generateExcelReport(
            @RequestParam(required = false) String year,
            @RequestParam(required = false) String monthFrom,
            @RequestParam(required = false) String monthTo,
            @RequestParam(required = false) String fabric,
            @RequestParam(required = false) String shift,
            @RequestParam(required = false) String cutting,
            @RequestParam String data) throws IOException {

        List<Map<String, Object>> reportData = parseReportData(data);

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Fabric Waste Report");

            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFillForegroundColor(IndexedColors.ROYAL_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            Font headerFont = workbook.createFont();
            headerFont.setColor(IndexedColors.WHITE.getIndex());
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            CellStyle kpiStyle = workbook.createCellStyle();
            kpiStyle.setFillForegroundColor(IndexedColors.LIGHT_CORNFLOWER_BLUE.getIndex());
            kpiStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            Font kpiFont = workbook.createFont(); kpiFont.setBold(true);
            kpiStyle.setFont(kpiFont);

            CellStyle altStyle = workbook.createCellStyle();
            altStyle.setFillForegroundColor(IndexedColors.LIGHT_TURQUOISE.getIndex());
            altStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            int rowNum = 0;

            // Title
            Row titleRow = sheet.createRow(rowNum++);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("Fabric Waste Prediction Report");
            CellStyle titleStyle = workbook.createCellStyle();
            Font titleFont = workbook.createFont();
            titleFont.setBold(true); titleFont.setFontHeightInPoints((short)14);
            titleStyle.setFont(titleFont);
            titleCell.setCellStyle(titleStyle);

            sheet.createRow(rowNum++).createCell(0).setCellValue("Generated: " +
                    java.time.LocalDateTime.now().format(
                            java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
            rowNum++;

            // Filters
            Row fhRow = sheet.createRow(rowNum++);
            Cell fhCell = fhRow.createCell(0); fhCell.setCellValue("Applied Filters"); fhCell.setCellStyle(kpiStyle);
            Row filterRow = sheet.createRow(rowNum++);
            String[] fLabels = {"Year","Month From","Month To","Fabric","Shift","Cutting"};
            String[] fVals   = {
                year      != null ? year      : "All",
                monthFrom != null ? monthFrom : "All",
                monthTo   != null ? monthTo   : "All",
                fabric    != null ? fabric    : "All",
                shift     != null ? shift     : "All",
                cutting   != null ? cutting   : "All"
            };
            for (int i = 0; i < fLabels.length; i++)
                filterRow.createCell(i).setCellValue(fLabels[i] + ": " + fVals[i]);
            rowNum++;

            // KPIs
            Map<String, Object> kpis = calculateKPI(reportData);
            Row khRow = sheet.createRow(rowNum++);
            Cell khCell = khRow.createCell(0); khCell.setCellValue("Key Performance Indicators"); khCell.setCellStyle(kpiStyle);
            Row kpiRow = sheet.createRow(rowNum++);
            kpiRow.createCell(0).setCellValue("Total Records: " + kpis.get("totalRecords"));
            kpiRow.createCell(1).setCellValue("Avg Actual: "    + kpis.get("avgActual")    + "%");
            kpiRow.createCell(2).setCellValue("Avg Predicted: " + kpis.get("avgPredicted") + "%");
            kpiRow.createCell(3).setCellValue("Highest: "       + kpis.get("maxWaste")     + "%");
            kpiRow.createCell(4).setCellValue("Lowest: "        + kpis.get("minWaste")     + "%");
            rowNum++;

            // Table header
            Row tableHeader = sheet.createRow(rowNum++);
            String[] cols = {"Job ID","Date","Fabric / Material","Shift","Cutting Method",
                             "Layers","Marker Eff. %","Actual Waste %","Predicted %","Difference %"};
            for (int i = 0; i < cols.length; i++) {
                Cell c = tableHeader.createCell(i);
                c.setCellValue(cols[i]); c.setCellStyle(headerStyle);
            }

            // Data rows
            int dataRowNum = 0;
            for (Map<String, Object> rowData : reportData) {
                Row dataRow = sheet.createRow(rowNum++);
                if (dataRowNum % 2 == 1) {
                    for (int i = 0; i < cols.length; i++) dataRow.createCell(i).setCellStyle(altStyle);
                }
                double actual    = toDouble(rowData.get("actual"));
                double predicted = toDouble(rowData.get("predicted"));
                double diff      = Math.round((predicted - actual) * 100.0) / 100.0;

                dataRow.createCell(0).setCellValue(str(rowData.get("jobId")));
                dataRow.createCell(1).setCellValue(str(rowData.get("jobDate")));
                dataRow.createCell(2).setCellValue(str(rowData.get("fabric")));
                dataRow.createCell(3).setCellValue(str(rowData.get("shift")));
                dataRow.createCell(4).setCellValue(str(rowData.get("cutting")));
                dataRow.createCell(5).setCellValue(str(rowData.get("layers")));
                dataRow.createCell(6).setCellValue(str(rowData.get("marker")));
                dataRow.createCell(7).setCellValue(actual);
                dataRow.createCell(8).setCellValue(predicted);
                dataRow.createCell(9).setCellValue(diff);
                dataRowNum++;
            }

            for (int i = 0; i < cols.length; i++) sheet.autoSizeColumn(i);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            workbook.write(baos);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
            headers.setContentDispositionFormData("filename",
                    "fabric_waste_report_" + System.currentTimeMillis() + ".xlsx");
            headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");
            return ResponseEntity.ok().headers(headers).body(baos.toByteArray());
        }
    }

    // ── Helpers ──
    private Map<String, String> buildFiltersMap(String year, String monthFrom, String monthTo,
                                                 String fabric, String shift, String cutting) {
        Map<String, String> filters = new HashMap<>();
        filters.put("year",      year      != null ? year      : "All");
        filters.put("monthFrom", monthFrom != null ? monthFrom : "All");
        filters.put("monthTo",   monthTo   != null ? monthTo   : "All");
        filters.put("fabric",    fabric    != null ? fabric    : "All");
        filters.put("shift",     shift     != null ? shift     : "All");
        filters.put("cutting",   cutting   != null ? cutting   : "All");
        return filters;
    }

    private List<Map<String, Object>> jobsToMapList(List<CuttingJob> jobs) {
        String[] months = {"","Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec"};
        return jobs.stream().map(job -> {
            Map<String, Object> map = new HashMap<>();
            map.put("jobId",  job.getId() != null ? job.getId().toString() : "");
            map.put("jobDate",   job.getJobDate() != null ? job.getJobDate().toString() : "");
            map.put("year",      job.getJobDate() != null ? job.getJobDate().getYear() : 0);
            map.put("month",     job.getJobDate() != null ? job.getJobDate().getMonthValue() : 0);
            map.put("monthName", job.getJobDate() != null ? months[job.getJobDate().getMonthValue()] : "");
            map.put("fabric", job.getFabricType());
            map.put("shift",     job.getShift());
            map.put("cutting",   job.getCuttingMethod());
            map.put("actual",    job.getActualWastagePct()    != null ? job.getActualWastagePct()    : 0.0);
            map.put("predicted", job.getPredictedWastePct()   != null ? job.getPredictedWastePct()   : 0.0);
            map.put("marker",    job.getMarkerEfficiencyPct() != null ? job.getMarkerEfficiencyPct() : 0.0);
            map.put("layers",    job.getNoOfLayers());
            return map;
        }).collect(Collectors.toList());
    }

    private Map<String, Object> calculateKPI(List<Map<String, Object>> data) {
        Map<String, Object> kpi = new HashMap<>();
        if (data.isEmpty()) {
            kpi.put("totalRecords", 0); kpi.put("avgActual", 0.0);
            kpi.put("avgPredicted", 0.0); kpi.put("maxWaste", 0.0); kpi.put("minWaste", 0.0);
            return kpi;
        }
        double sumActual = 0, sumPredicted = 0;
        double maxActual = Double.MIN_VALUE, minActual = Double.MAX_VALUE;
        for (Map<String, Object> row : data) {
            double actual    = toDouble(row.get("actual"));
            double predicted = toDouble(row.get("predicted"));
            sumActual    += actual;    sumPredicted += predicted;
            maxActual = Math.max(maxActual, actual);
            minActual = Math.min(minActual, actual);
        }
        kpi.put("totalRecords",  data.size());
        kpi.put("avgActual",     Math.round((sumActual    / data.size()) * 100.0) / 100.0);
        kpi.put("avgPredicted",  Math.round((sumPredicted / data.size()) * 100.0) / 100.0);
        kpi.put("maxWaste",      Math.round(maxActual * 100.0) / 100.0);
        kpi.put("minWaste",      Math.round(minActual * 100.0) / 100.0);
        return kpi;
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
                        } catch (NumberFormatException e) { map.put(key, val); }
                    }
                }
                if (!map.isEmpty()) result.add(map);
            }
        } catch (Exception e) { e.printStackTrace(); }
        return result;
    }

    private String str(Object val)    { return val != null ? val.toString() : ""; }
    private double toDouble(Object val) {
        if (val == null) return 0.0;
        if (val instanceof Number) return ((Number) val).doubleValue();
        try { return Double.parseDouble(val.toString()); } catch (Exception e) { return 0.0; }
    }
}
