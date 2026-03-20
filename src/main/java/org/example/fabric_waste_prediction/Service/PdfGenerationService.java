package org.example.fabric_waste_prediction.Service;

import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.layout.properties.VerticalAlignment;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Service
public class PdfGenerationService {

    public byte[] generateReportPdf(List<Map<String, Object>> reportData,
                                    Map<String, String> filters,
                                    Map<String, Object> kpiData) throws IOException {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(baos);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf, PageSize.A4.rotate());
        document.setMargins(20, 20, 20, 20);

        // Add logo/header
        PdfFont boldFont = PdfFontFactory.createFont(com.itextpdf.io.font.constants.StandardFonts.HELVETICA_BOLD);
        PdfFont normalFont = PdfFontFactory.createFont(com.itextpdf.io.font.constants.StandardFonts.HELVETICA);

        // Title
        Paragraph title = new Paragraph("Fabric Waste Prediction Report")
                .setFont(boldFont)
                .setFontSize(20)
                .setFontColor(new DeviceRgb(37, 99, 235))
                .setTextAlignment(TextAlignment.CENTER);
        document.add(title);

        // Generation info
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
        Paragraph info = new Paragraph("Generated on: " + timestamp)
                .setFont(normalFont)
                .setFontSize(10)
                .setTextAlignment(TextAlignment.RIGHT);
        document.add(info);

        // Filters section
        Paragraph filterTitle = new Paragraph("Report Filters")
                .setFont(boldFont)
                .setFontSize(14)
                .setFontColor(new DeviceRgb(37, 99, 235))
                .setMarginTop(10);
        document.add(filterTitle);

        Table filterTable = new Table(UnitValue.createPercentArray(new float[]{1, 1, 1, 1, 1, 1}))
                .setWidth(UnitValue.createPercentValue(100))
                .setMarginTop(5)
                .setMarginBottom(10);

        addFilterCell(filterTable, "Year", filters.getOrDefault("year", "All"), boldFont, normalFont);
        addFilterCell(filterTable, "Month From", filters.getOrDefault("monthFrom", "All"), boldFont, normalFont);
        addFilterCell(filterTable, "Month To", filters.getOrDefault("monthTo", "All"), boldFont, normalFont);
        addFilterCell(filterTable, "Fabric Type", filters.getOrDefault("fabric", "All"), boldFont, normalFont);
        addFilterCell(filterTable, "Shift", filters.getOrDefault("shift", "All"), boldFont, normalFont);
        addFilterCell(filterTable, "Cutting", filters.getOrDefault("cutting", "All"), boldFont, normalFont);

        document.add(filterTable);

        // KPI Cards
        Paragraph kpiTitle = new Paragraph("Key Performance Indicators")
                .setFont(boldFont)
                .setFontSize(14)
                .setFontColor(new DeviceRgb(37, 99, 235))
                .setMarginTop(15);
        document.add(kpiTitle);

        Table kpiTable = new Table(UnitValue.createPercentArray(new float[]{1, 1, 1, 1, 1}))
                .setWidth(UnitValue.createPercentValue(100))
                .setMarginTop(5)
                .setMarginBottom(15);

        addKpiCell(kpiTable, "Total Records", kpiData.get("totalRecords").toString(), boldFont, normalFont);
        addKpiCell(kpiTable, "Avg Actual Waste", kpiData.get("avgActual") + "%", boldFont, normalFont);
        addKpiCell(kpiTable, "Avg Predicted", kpiData.get("avgPredicted") + "%", boldFont, normalFont);
        addKpiCell(kpiTable, "Highest Waste", kpiData.get("maxWaste") + "%", boldFont, normalFont);
        addKpiCell(kpiTable, "Lowest Waste", kpiData.get("minWaste") + "%", boldFont, normalFont);

        document.add(kpiTable);

        // Data Table
        Paragraph tableTitle = new Paragraph("Detailed Report Data")
                .setFont(boldFont)
                .setFontSize(14)
                .setFontColor(new DeviceRgb(37, 99, 235))
                .setMarginTop(15);
        document.add(tableTitle);

        Paragraph recordCount = new Paragraph("Showing " + reportData.size() + " records")
                .setFont(normalFont)
                .setFontSize(10)
                .setMarginBottom(5);
        document.add(recordCount);

        // Create data table
        Table dataTable = new Table(UnitValue.createPercentArray(new float[]{1, 1, 1.5f, 1.5f, 0.8f, 1, 1, 1.2f, 1.2f, 1.2f}))
                .setWidth(UnitValue.createPercentValue(100))
                .setMarginTop(5);

        // Table headers
        String[] headers = {"Year", "Month", "Fabric", "Pattern", "GSM", "Shift", "Cutting", "Actual %", "Predicted %", "Diff %"};
        for (String header : headers) {
            dataTable.addHeaderCell(new Cell().add(new Paragraph(header).setFont(boldFont).setFontSize(9))
                    .setBackgroundColor(new DeviceRgb(37, 99, 235))
                    .setFontColor(ColorConstants.WHITE)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setVerticalAlignment(VerticalAlignment.MIDDLE)
                    .setPadding(5));
        }

        // Table data
        String[] months = {"", "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};

        for (Map<String, Object> row : reportData) {
            double actual = (double) row.get("actual");
            double predicted = (double) row.get("predicted");
            double diff = predicted - actual;
            String diffStr = String.format("%+.2f%%", diff);

            dataTable.addCell(createDataCell(row.get("year").toString(), normalFont, TextAlignment.CENTER));
            dataTable.addCell(createDataCell(months[(int) row.get("month")], normalFont, TextAlignment.CENTER));
            dataTable.addCell(createDataCell(row.get("fabric").toString(), normalFont, TextAlignment.LEFT));
            dataTable.addCell(createDataCell(row.get("pattern").toString(), normalFont, TextAlignment.LEFT));
            dataTable.addCell(createDataCell(row.get("gsm").toString(), normalFont, TextAlignment.CENTER));
            dataTable.addCell(createDataCell(row.get("shift").toString(), normalFont, TextAlignment.CENTER));
            dataTable.addCell(createDataCell(row.get("cutting").toString(), normalFont, TextAlignment.CENTER));
            dataTable.addCell(createDataCell(String.format("%.2f%%", actual), normalFont, TextAlignment.CENTER));
            dataTable.addCell(createDataCell(String.format("%.2f%%", predicted), normalFont, TextAlignment.CENTER));

            Cell diffCell = createDataCell(diffStr, normalFont, TextAlignment.CENTER);
            if (diff > 0) {
                diffCell.setFontColor(new DeviceRgb(220, 38, 38)); // Red for over-prediction
            } else if (diff < 0) {
                diffCell.setFontColor(new DeviceRgb(34, 197, 94)); // Green for under-prediction
            }
            dataTable.addCell(diffCell);
        }

        document.add(dataTable);

        // Footer
        Paragraph footer = new Paragraph("Fabric Waste Prediction System v1.0 - Confidential Report")
                .setFont(normalFont)
                .setFontSize(8)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginTop(20);
        document.add(footer);

        document.close();
        return baos.toByteArray();
    }

    private void addFilterCell(Table table, String label, String value, PdfFont boldFont, PdfFont normalFont) {
        Cell cell = new Cell().setBorder(new SolidBorder(new DeviceRgb(226, 232, 240), 0.5f))
                .setPadding(5);

        Paragraph labelPara = new Paragraph(label + ":")
                .setFont(boldFont)
                .setFontSize(9);
        Paragraph valuePara = new Paragraph(value)
                .setFont(normalFont)
                .setFontSize(9);

        cell.add(labelPara).add(valuePara);
        table.addCell(cell);
    }

    private void addKpiCell(Table table, String label, String value, PdfFont boldFont, PdfFont normalFont) {
        Cell cell = new Cell()
                .setBorder(new SolidBorder(new DeviceRgb(37, 99, 235), 1))
                .setPadding(8)
                .setTextAlignment(TextAlignment.CENTER);

        Paragraph valuePara = new Paragraph(value)
                .setFont(boldFont)
                .setFontSize(14)
                .setFontColor(new DeviceRgb(37, 99, 235));
        Paragraph labelPara = new Paragraph(label)
                .setFont(normalFont)
                .setFontSize(8);

        cell.add(valuePara).add(labelPara);
        table.addCell(cell);
    }

    private Cell createDataCell(String text, PdfFont font, TextAlignment alignment) {
        return new Cell().add(new Paragraph(text).setFont(font).setFontSize(8))
                .setBorder(new SolidBorder(new DeviceRgb(226, 232, 240), 0.5f))
                .setTextAlignment(alignment)
                .setPadding(5);
    }
}
