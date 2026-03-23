package org.example.fabric_waste_prediction.Service;

import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.HorizontalAlignment;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.layout.properties.VerticalAlignment;
import com.itextpdf.io.image.ImageDataFactory;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.List;
import java.util.Map;

@Service
public class PdfGenerationService {

    private static final DeviceRgb BLUE      = new DeviceRgb(37,  99,  235);
    private static final DeviceRgb LIGHT_GRAY= new DeviceRgb(226, 232, 240);
    private static final DeviceRgb GREEN     = new DeviceRgb(34,  197, 94);
    private static final DeviceRgb RED       = new DeviceRgb(220, 38,  38);

    // ── DATA TABLE PDF ──
    public byte[] generateReportPdf(List<Map<String, Object>> reportData,
                                    Map<String, String> filters,
                                    Map<String, Object> kpiData) throws IOException {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfDocument pdf = new PdfDocument(new PdfWriter(baos));
        Document doc = new Document(pdf, PageSize.A4.rotate());
        doc.setMargins(20, 20, 20, 20);

        PdfFont bold   = PdfFontFactory.createFont(com.itextpdf.io.font.constants.StandardFonts.HELVETICA_BOLD);
        PdfFont normal = PdfFontFactory.createFont(com.itextpdf.io.font.constants.StandardFonts.HELVETICA);

        // Title
        doc.add(new Paragraph("Fabric Waste Prediction — Detailed Report")
                .setFont(bold).setFontSize(18).setFontColor(BLUE).setTextAlignment(TextAlignment.CENTER));
        doc.add(new Paragraph("Generated: " + now())
                .setFont(normal).setFontSize(9).setTextAlignment(TextAlignment.RIGHT));

        // Filters
        doc.add(sectionTitle("Applied Filters", bold));
        Table ft = new Table(UnitValue.createPercentArray(new float[]{1,1,1,1,1,1}))
                .setWidth(UnitValue.createPercentValue(100)).setMarginBottom(10);
        addFilterCell(ft, "Year",        filters.getOrDefault("year",      "All"), bold, normal);
        addFilterCell(ft, "Month From",  filters.getOrDefault("monthFrom", "All"), bold, normal);
        addFilterCell(ft, "Month To",    filters.getOrDefault("monthTo",   "All"), bold, normal);
        addFilterCell(ft, "Fabric Type", filters.getOrDefault("fabric",    "All"), bold, normal);
        addFilterCell(ft, "Shift",       filters.getOrDefault("shift",     "All"), bold, normal);
        addFilterCell(ft, "Cutting",     filters.getOrDefault("cutting",   "All"), bold, normal);
        doc.add(ft);

        // KPIs
        doc.add(sectionTitle("Key Performance Indicators", bold));
        Table kt = new Table(UnitValue.createPercentArray(new float[]{1,1,1,1,1}))
                .setWidth(UnitValue.createPercentValue(100)).setMarginBottom(15);
        addKpiCell(kt, "Total Records",    str(kpiData.get("totalRecords")),         bold, normal);
        addKpiCell(kt, "Avg Actual Waste", str(kpiData.get("avgActual"))    + "%",   bold, normal);
        addKpiCell(kt, "Avg Predicted",    str(kpiData.get("avgPredicted")) + "%",   bold, normal);
        addKpiCell(kt, "Highest Waste",    str(kpiData.get("maxWaste"))     + "%",   bold, normal);
        addKpiCell(kt, "Lowest Waste",     str(kpiData.get("minWaste"))     + "%",   bold, normal);
        doc.add(kt);

        // Data table
        doc.add(sectionTitle("Detailed Records (" + reportData.size() + " entries)", bold));
        Table dt = new Table(UnitValue.createPercentArray(new float[]{1.2f,1.4f,1.4f,1f,1.2f,0.8f,1f,1.2f,1.2f,1f}))
                .setWidth(UnitValue.createPercentValue(100));

        for (String h : new String[]{"Job ID","Date","Fabric","Shift","Cutting","Layers","Marker %","Actual %","Predicted %","Diff %"}) {
            dt.addHeaderCell(new Cell().add(new Paragraph(h).setFont(bold).setFontSize(8))
                    .setBackgroundColor(BLUE).setFontColor(ColorConstants.WHITE)
                    .setTextAlignment(TextAlignment.CENTER).setVerticalAlignment(VerticalAlignment.MIDDLE).setPadding(5));
        }

        for (Map<String, Object> row : reportData) {
            double actual    = toDouble(row.get("actual"));
            double predicted = toDouble(row.get("predicted"));
            double diff      = predicted - actual;

            dt.addCell(dc(str(row.get("jobId")),   normal, TextAlignment.LEFT));
            dt.addCell(dc(str(row.get("jobDate")), normal, TextAlignment.CENTER));
            dt.addCell(dc(str(row.get("fabric")),  normal, TextAlignment.LEFT));
            dt.addCell(dc(str(row.get("shift")),   normal, TextAlignment.CENTER));
            dt.addCell(dc(str(row.get("cutting")), normal, TextAlignment.CENTER));
            dt.addCell(dc(str(row.get("layers")),  normal, TextAlignment.CENTER));
            dt.addCell(dc(str(row.get("marker")),  normal, TextAlignment.CENTER));
            dt.addCell(dc(String.format("%.2f%%", actual),    normal, TextAlignment.CENTER));
            dt.addCell(dc(String.format("%.2f%%", predicted), normal, TextAlignment.CENTER));
            Cell diffCell = dc(String.format("%+.2f%%", diff), normal, TextAlignment.CENTER);
            if (diff > 0) diffCell.setFontColor(RED); else if (diff < 0) diffCell.setFontColor(GREEN);
            dt.addCell(diffCell);
        }
        doc.add(dt);

        doc.add(new Paragraph("Fabric Waste Prediction System — Confidential Report")
                .setFont(normal).setFontSize(8).setTextAlignment(TextAlignment.CENTER).setMarginTop(15));
        doc.close();
        return baos.toByteArray();
    }

    // ── TRENDS PDF (charts) ──
    public byte[] generateTrendsPdf(Map<String, String> filters,
                                     String trendChartBase64,
                                     String fabricChartBase64,
                                     String shiftChartBase64,
                                     String cuttingChartBase64,
                                     int recordCount) throws IOException {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfDocument pdf = new PdfDocument(new PdfWriter(baos));
        Document doc = new Document(pdf, PageSize.A4);
        doc.setMargins(25, 25, 25, 25);

        PdfFont bold   = PdfFontFactory.createFont(com.itextpdf.io.font.constants.StandardFonts.HELVETICA_BOLD);
        PdfFont normal = PdfFontFactory.createFont(com.itextpdf.io.font.constants.StandardFonts.HELVETICA);

        // Title
        doc.add(new Paragraph("Fabric Waste Prediction — Trends Report")
                .setFont(bold).setFontSize(18).setFontColor(BLUE).setTextAlignment(TextAlignment.CENTER));
        doc.add(new Paragraph("Generated: " + now() + "   |   Records Analysed: " + recordCount)
                .setFont(normal).setFontSize(9).setTextAlignment(TextAlignment.CENTER).setMarginBottom(5));

        // Filters summary
        doc.add(sectionTitle("Applied Filters", bold));
        Table ft = new Table(UnitValue.createPercentArray(new float[]{1,1,1,1,1,1}))
                .setWidth(UnitValue.createPercentValue(100)).setMarginBottom(12);
        addFilterCell(ft, "Year",        filters.getOrDefault("year",      "All"), bold, normal);
        addFilterCell(ft, "Month From",  filters.getOrDefault("monthFrom", "All"), bold, normal);
        addFilterCell(ft, "Month To",    filters.getOrDefault("monthTo",   "All"), bold, normal);
        addFilterCell(ft, "Fabric Type", filters.getOrDefault("fabric",    "All"), bold, normal);
        addFilterCell(ft, "Shift",       filters.getOrDefault("shift",     "All"), bold, normal);
        addFilterCell(ft, "Cutting",     filters.getOrDefault("cutting",   "All"), bold, normal);
        doc.add(ft);

        // Charts — 2x2 layout
        doc.add(sectionTitle("Trend Analysis Charts", bold));

        addChartToPdf(doc, trendChartBase64,   "Actual vs Predicted Waste Trend",    bold, normal);
        addChartToPdf(doc, fabricChartBase64,  "Waste by Fabric Type",               bold, normal);
        addChartToPdf(doc, shiftChartBase64,   "Day vs Night Shift Comparison",      bold, normal);
        addChartToPdf(doc, cuttingChartBase64, "Cutting Method Impact",              bold, normal);

        doc.add(new Paragraph("Fabric Waste Prediction System — Trends Report — Confidential")
                .setFont(normal).setFontSize(8).setTextAlignment(TextAlignment.CENTER).setMarginTop(10));
        doc.close();
        return baos.toByteArray();
    }

    private void addChartToPdf(Document doc, String base64, String title, PdfFont bold, PdfFont normal) {
        try {
            if (base64 == null || base64.isEmpty()) return;
            String b64data = base64.contains(",") ? base64.split(",")[1] : base64;
            byte[] imageBytes = Base64.getDecoder().decode(b64data);
            Image img = new Image(ImageDataFactory.create(imageBytes));
            img.setWidth(UnitValue.createPercentValue(90));
            img.setHorizontalAlignment(HorizontalAlignment.CENTER);

            doc.add(new Paragraph(title).setFont(bold).setFontSize(11)
                    .setFontColor(BLUE).setMarginTop(8).setMarginBottom(4));
            doc.add(img);
        } catch (Exception e) {
            doc.add(new Paragraph("Chart unavailable: " + title).setFont(normal).setFontSize(9));
        }
    }

    // ── Shared helpers ──
    private Paragraph sectionTitle(String text, PdfFont bold) {
        return new Paragraph(text).setFont(bold).setFontSize(12)
                .setFontColor(BLUE).setMarginTop(8).setMarginBottom(4);
    }

    private void addFilterCell(Table table, String label, String value, PdfFont bold, PdfFont normal) {
        Cell cell = new Cell().setBorder(new SolidBorder(LIGHT_GRAY, 0.5f)).setPadding(5);
        cell.add(new Paragraph(label + ":").setFont(bold).setFontSize(9));
        cell.add(new Paragraph(value).setFont(normal).setFontSize(9));
        table.addCell(cell);
    }

    private void addKpiCell(Table table, String label, String value, PdfFont bold, PdfFont normal) {
        Cell cell = new Cell().setBorder(new SolidBorder(BLUE, 1)).setPadding(8).setTextAlignment(TextAlignment.CENTER);
        cell.add(new Paragraph(value).setFont(bold).setFontSize(13).setFontColor(BLUE));
        cell.add(new Paragraph(label).setFont(normal).setFontSize(8));
        table.addCell(cell);
    }

    private Cell dc(String text, PdfFont font, TextAlignment align) {
        return new Cell().add(new Paragraph(text).setFont(font).setFontSize(8))
                .setBorder(new SolidBorder(LIGHT_GRAY, 0.5f)).setTextAlignment(align).setPadding(4);
    }

    private String str(Object val)      { return val != null ? val.toString() : "—"; }
    private double toDouble(Object val) {
        if (val == null) return 0.0;
        if (val instanceof Number) return ((Number) val).doubleValue();
        try { return Double.parseDouble(val.toString()); } catch (Exception e) { return 0.0; }
    }
    private String now() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
    }
}
