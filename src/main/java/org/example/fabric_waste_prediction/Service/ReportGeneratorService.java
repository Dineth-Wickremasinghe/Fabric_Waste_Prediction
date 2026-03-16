package org.example.fabric_waste_prediction.Service;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.example.fabric_waste_prediction.dto.FabricBreakdownDTO;
import org.example.fabric_waste_prediction.Entity.SustainabilityMetrics;
import org.example.fabric_waste_prediction.Repository.DailyWastageRepository;
import org.example.fabric_waste_prediction.Repository.SustainabilityRepository;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.DefaultCategoryDataset;
import org.springframework.stereotype.Service;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class ReportGeneratorService {

    private final SustainabilityRepository sustainabilityRepository;
    private final DailyWastageRepository dailyWastageRepository;

    public ReportGeneratorService(SustainabilityRepository sustainabilityRepository,
                                  DailyWastageRepository dailyWastageRepository) {
        this.sustainabilityRepository = sustainabilityRepository;
        this.dailyWastageRepository = dailyWastageRepository;
    }

    public byte[] generateReport(String reportType, LocalDate startDate, LocalDate endDate, Map<String, Object> summaryData) {
        return switch (reportType.toLowerCase()) {
            case "pdf" -> generatePdfReport(startDate, endDate, summaryData);
            case "excel" -> generateExcelReport(startDate, endDate, summaryData);
            default -> generatePdfReport(startDate, endDate, summaryData);
        };
    }

    private byte[] generatePdfReport(LocalDate startDate, LocalDate endDate, Map<String, Object> summary) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            // Title
            Paragraph title = new Paragraph("Sustainability Impact Report")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontSize(20);
            document.add(title);

            // Date range
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            Paragraph dateRange = new Paragraph("Period: " + startDate.format(formatter) +
                    " to " + endDate.format(formatter))
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontSize(12);
            document.add(dateRange);
            document.add(new Paragraph("\n"));

            // Key metrics table
            Table metricsTable = new Table(UnitValue.createPercentArray(new float[]{50, 50}))
                    .useAllAvailableWidth();

            metricsTable.addCell("Total Waste Reduced (tons):");
            metricsTable.addCell(String.format("%.2f", getDoubleValue(summary.get("totalWasteReduced"))));
            metricsTable.addCell("Total Carbon Avoided (tons):");
            metricsTable.addCell(String.format("%.2f", getDoubleValue(summary.get("totalCarbonAvoided"))));
            metricsTable.addCell("Total Water Saved (L):");
            metricsTable.addCell(String.format("%.2f", getDoubleValue(summary.get("totalWaterSaved"))));
            metricsTable.addCell("Total Cost Saved (LKR):");
            metricsTable.addCell(String.format("Rs. %.2f", getDoubleValue(summary.get("totalCostSaved"))));
            metricsTable.addCell("Sustainability Score:");
            metricsTable.addCell(String.format("%.1f%%", getDoubleValue(summary.get("overallSustainabilityScore"))));

            document.add(metricsTable);
            document.add(new Paragraph("\n"));

            // Fabric breakdown
            Paragraph fabricTitle = new Paragraph("Fabric Breakdown by Risk Level")
                    .setFontSize(16);
            document.add(fabricTitle);

            Table fabricTable = new Table(UnitValue.createPercentArray(new float[]{25, 20, 20, 15, 20}))
                    .useAllAvailableWidth();

            fabricTable.addCell("Fabric Type");
            fabricTable.addCell("Wastage %");
            fabricTable.addCell("Risk Level");
            fabricTable.addCell("Jobs");
            fabricTable.addCell("Waste (kg)");

            @SuppressWarnings("unchecked")
            List<FabricBreakdownDTO> breakdown = (List<FabricBreakdownDTO>) summary.getOrDefault("fabricBreakdown", new ArrayList<>());

            if (breakdown != null && !breakdown.isEmpty()) {
                for (FabricBreakdownDTO fabric : breakdown) {
                    fabricTable.addCell(fabric.getFabricType() != null ? fabric.getFabricType() : "N/A");
                    fabricTable.addCell(String.format("%.2f%%", fabric.getWastagePercentage() != null ? fabric.getWastagePercentage() : 0.0));
                    fabricTable.addCell(fabric.getRiskLevel() != null ? fabric.getRiskLevel() : "UNKNOWN");
                    fabricTable.addCell(String.valueOf(fabric.getJobCount() != null ? fabric.getJobCount() : 0));
                    fabricTable.addCell(String.format("%.2f", fabric.getTotalWasteKg() != null ? fabric.getTotalWasteKg() : 0.0));
                }
            } else {
                fabricTable.addCell("No data available");
                fabricTable.addCell("-");
                fabricTable.addCell("-");
                fabricTable.addCell("-");
                fabricTable.addCell("-");
            }

            document.add(fabricTable);

            // Recommendations
            document.add(new Paragraph("\n"));
            Paragraph recommendationsTitle = new Paragraph("Key Recommendations")
                    .setFontSize(16);
            document.add(recommendationsTitle);

            boolean hasRecommendations = false;
            if (breakdown != null && !breakdown.isEmpty()) {
                for (FabricBreakdownDTO fabric : breakdown) {
                    if ("HIGH".equals(fabric.getRiskLevel()) && fabric.getOptimizationTips() != null && !fabric.getOptimizationTips().isEmpty()) {
                        hasRecommendations = true;
                        document.add(new Paragraph(fabric.getFabricType() + ":"));
                        for (String tip : fabric.getOptimizationTips()) {
                            document.add(new Paragraph("• " + tip).setFontSize(10));
                        }
                        document.add(new Paragraph(" "));
                    }
                }
            }

            if (!hasRecommendations) {
                document.add(new Paragraph("No high-risk fabrics found. All fabrics are performing well.").setFontSize(10));
            }

            document.close();
            return baos.toByteArray();

        } catch (Exception e) {
            e.printStackTrace();
            return new byte[0];
        }
    }

    private byte[] generateExcelReport(LocalDate startDate, LocalDate endDate, Map<String, Object> summary) {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Sustainability Report");

            // Create styles
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setFontHeightInPoints((short) 12);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setBorderBottom(BorderStyle.THIN);
            headerStyle.setBorderTop(BorderStyle.THIN);
            headerStyle.setBorderLeft(BorderStyle.THIN);
            headerStyle.setBorderRight(BorderStyle.THIN);

            CellStyle subHeaderStyle = workbook.createCellStyle();
            Font subHeaderFont = workbook.createFont();
            subHeaderFont.setBold(true);
            subHeaderStyle.setFont(subHeaderFont);
            subHeaderStyle.setFillForegroundColor(IndexedColors.LIGHT_YELLOW.getIndex());
            subHeaderStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            subHeaderStyle.setBorderBottom(BorderStyle.THIN);
            subHeaderStyle.setBorderTop(BorderStyle.THIN);
            subHeaderStyle.setBorderLeft(BorderStyle.THIN);
            subHeaderStyle.setBorderRight(BorderStyle.THIN);

            CellStyle dataStyle = workbook.createCellStyle();
            dataStyle.setBorderBottom(BorderStyle.THIN);
            dataStyle.setBorderTop(BorderStyle.THIN);
            dataStyle.setBorderLeft(BorderStyle.THIN);
            dataStyle.setBorderRight(BorderStyle.THIN);
            dataStyle.setDataFormat(workbook.createDataFormat().getFormat("#,##0.00"));

            CellStyle currencyStyle = workbook.createCellStyle();
            currencyStyle.setBorderBottom(BorderStyle.THIN);
            currencyStyle.setBorderTop(BorderStyle.THIN);
            currencyStyle.setBorderLeft(BorderStyle.THIN);
            currencyStyle.setBorderRight(BorderStyle.THIN);
            currencyStyle.setDataFormat(workbook.createDataFormat().getFormat("\"Rs.\" #,##0.00"));

            CellStyle percentageStyle = workbook.createCellStyle();
            percentageStyle.setBorderBottom(BorderStyle.THIN);
            percentageStyle.setBorderTop(BorderStyle.THIN);
            percentageStyle.setBorderLeft(BorderStyle.THIN);
            percentageStyle.setBorderRight(BorderStyle.THIN);
            percentageStyle.setDataFormat(workbook.createDataFormat().getFormat("0.00%"));

            CellStyle integerStyle = workbook.createCellStyle();
            integerStyle.setBorderBottom(BorderStyle.THIN);
            integerStyle.setBorderTop(BorderStyle.THIN);
            integerStyle.setBorderLeft(BorderStyle.THIN);
            integerStyle.setBorderRight(BorderStyle.THIN);
            integerStyle.setDataFormat(workbook.createDataFormat().getFormat("0"));

            int rowNum = 0;

            // Title
            Row titleRow = sheet.createRow(rowNum++);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("SUSTAINABILITY IMPACT REPORT");
            titleCell.setCellStyle(headerStyle);
            sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(0, 0, 0, 7));

            // Date range
            Row dateRow = sheet.createRow(rowNum++);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            Cell dateCell = dateRow.createCell(0);
            dateCell.setCellValue("Period: " + startDate.format(formatter) + " to " + endDate.format(formatter));
            dateCell.setCellStyle(subHeaderStyle);
            sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(1, 1, 0, 7));

            rowNum++; // Empty row

            // Key Metrics Section
            Row metricsHeader = sheet.createRow(rowNum++);
            Cell metricsHeaderCell = metricsHeader.createCell(0);
            metricsHeaderCell.setCellValue("KEY METRICS");
            metricsHeaderCell.setCellStyle(headerStyle);
            sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(rowNum-1, rowNum-1, 0, 1));

            // Metrics data
            rowNum = addMetricRow(sheet, rowNum, "Total Waste Reduced (tons):", getDoubleValue(summary.get("totalWasteReduced")), dataStyle, subHeaderStyle);
            rowNum = addMetricRow(sheet, rowNum, "Total Carbon Avoided (tons):", getDoubleValue(summary.get("totalCarbonAvoided")), dataStyle, subHeaderStyle);
            rowNum = addMetricRow(sheet, rowNum, "Total Water Saved (L):", getDoubleValue(summary.get("totalWaterSaved")), dataStyle, subHeaderStyle);
            rowNum = addMetricRow(sheet, rowNum, "Total Cost Saved (LKR):", getDoubleValue(summary.get("totalCostSaved")), currencyStyle, subHeaderStyle);

            Row scoreRow = sheet.createRow(rowNum++);
            Cell scoreLabelCell = scoreRow.createCell(0);
            scoreLabelCell.setCellValue("Sustainability Score:");
            scoreLabelCell.setCellStyle(subHeaderStyle);
            Cell scoreValueCell = scoreRow.createCell(1);
            scoreValueCell.setCellValue(getDoubleValue(summary.get("overallSustainabilityScore")) + "%");
            scoreValueCell.setCellStyle(dataStyle);

            rowNum += 2; // Add space

            // Fabric Breakdown Section
            Row fabricHeader = sheet.createRow(rowNum++);
            String[] fabricColumns = {"Fabric Type", "Wastage %", "Risk Level", "Jobs", "Waste (kg)", "CO2 Impact (kg)", "Water Impact (L)", "Cost Impact (LKR)"};
            for (int i = 0; i < fabricColumns.length; i++) {
                Cell cell = fabricHeader.createCell(i);
                cell.setCellValue(fabricColumns[i]);
                cell.setCellStyle(headerStyle);
            }

            @SuppressWarnings("unchecked")
            List<FabricBreakdownDTO> breakdown = (List<FabricBreakdownDTO>) summary.getOrDefault("fabricBreakdown", new ArrayList<>());

            if (breakdown != null && !breakdown.isEmpty()) {
                for (FabricBreakdownDTO fabric : breakdown) {
                    Row row = sheet.createRow(rowNum++);

                    // Fabric Type
                    row.createCell(0).setCellValue(fabric.getFabricType() != null ? fabric.getFabricType() : "N/A");

                    // Wastage %
                    Cell wastageCell = row.createCell(1);
                    wastageCell.setCellValue(fabric.getWastagePercentage() != null ? fabric.getWastagePercentage() / 100 : 0.0);
                    wastageCell.setCellStyle(percentageStyle);

                    // Risk Level
                    row.createCell(2).setCellValue(fabric.getRiskLevel() != null ? fabric.getRiskLevel() : "UNKNOWN");

                    // Jobs
                    Cell jobsCell = row.createCell(3);
                    jobsCell.setCellValue(fabric.getJobCount() != null ? fabric.getJobCount() : 0);
                    jobsCell.setCellStyle(integerStyle);

                    // Waste (kg)
                    Cell wasteCell = row.createCell(4);
                    wasteCell.setCellValue(fabric.getTotalWasteKg() != null ? fabric.getTotalWasteKg() : 0.0);
                    wasteCell.setCellStyle(dataStyle);

                    // CO2 Impact
                    Cell co2Cell = row.createCell(5);
                    co2Cell.setCellValue(fabric.getCo2Impact() != null ? fabric.getCo2Impact() : 0.0);
                    co2Cell.setCellStyle(dataStyle);

                    // Water Impact
                    Cell waterCell = row.createCell(6);
                    waterCell.setCellValue(fabric.getWaterImpact() != null ? fabric.getWaterImpact() : 0.0);
                    waterCell.setCellStyle(dataStyle);

                    // Cost Impact
                    Cell costCell = row.createCell(7);
                    costCell.setCellValue(fabric.getCostImpact() != null ? fabric.getCostImpact() : 0.0);
                    costCell.setCellStyle(currencyStyle);
                }
            } else {
                Row row = sheet.createRow(rowNum++);
                Cell cell = row.createCell(0);
                cell.setCellValue("No fabric data available");
                cell.setCellStyle(subHeaderStyle);
                sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(rowNum-1, rowNum-1, 0, 7));
            }

            // Auto-size columns
            for (int i = 0; i < fabricColumns.length; i++) {
                sheet.autoSizeColumn(i);
            }

            // Add generated timestamp
            rowNum += 2;
            Row generatedRow = sheet.createRow(rowNum++);
            Cell generatedCell = generatedRow.createCell(0);
            generatedCell.setCellValue("Report generated on: " + LocalDate.now().format(formatter) + " at " +
                    java.time.LocalTime.now().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss")));
            generatedCell.setCellStyle(subHeaderStyle);
            sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(rowNum-1, rowNum-1, 0, 7));

            workbook.write(baos);
            return baos.toByteArray();

        } catch (Exception e) {
            e.printStackTrace();
            // Return a simple error message
            return ("Error generating Excel report: " + e.getMessage()).getBytes();
        }
    }

    private int addMetricRow(Sheet sheet, int rowNum, String label, double value, CellStyle valueStyle, CellStyle labelStyle) {
        Row row = sheet.createRow(rowNum++);
        Cell labelCell = row.createCell(0);
        labelCell.setCellValue(label);
        labelCell.setCellStyle(labelStyle);
        Cell valueCell = row.createCell(1);
        valueCell.setCellValue(value);
        valueCell.setCellStyle(valueStyle);
        return rowNum;
    }

    private double getDoubleValue(Object obj) {
        if (obj == null) return 0.0;
        if (obj instanceof Number) return ((Number) obj).doubleValue();
        if (obj instanceof String) {
            try {
                return Double.parseDouble((String) obj);
            } catch (NumberFormatException e) {
                return 0.0;
            }
        }
        return 0.0;
    }

    public byte[] generateChartImage(LocalDate startDate, LocalDate endDate) throws IOException {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        List<SustainabilityMetrics> metrics = sustainabilityRepository
                .findByMetricDateBetween(startDate, endDate);

        if (metrics != null && !metrics.isEmpty()) {
            for (SustainabilityMetrics m : metrics) {
                if (m.getWasteReducedTons() != null) {
                    dataset.addValue(m.getWasteReducedTons(), "Waste Reduced", m.getMetricDate().toString());
                }
                if (m.getCarbonAvoidedTons() != null) {
                    dataset.addValue(m.getCarbonAvoidedTons(), "Carbon Avoided", m.getMetricDate().toString());
                }
            }
        }

        JFreeChart chart = ChartFactory.createLineChart(
                "Environmental Impact Trend",
                "Date",
                "Tons",
                dataset
        );

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ChartUtils.writeChartAsPNG(baos, chart, 800, 400);
        return baos.toByteArray();
    }
}