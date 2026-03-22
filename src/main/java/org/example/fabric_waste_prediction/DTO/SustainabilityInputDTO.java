package org.example.fabric_waste_prediction.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SustainabilityInputDTO {

    // ==================== 1. WASTE MANAGEMENT ====================

    @NotNull(message = "Please tell us how much you recycle")
    @Min(value = 0, message = "Recycling rate must be between 0 and 100%")
    @Max(value = 100, message = "Recycling rate must be between 0 and 100%")
    private Double recyclingRate;  // "How much fabric waste do you recycle?"

    @NotBlank(message = "Please select where your waste goes")
    @Pattern(regexp = "^(RECYCLE|INCINERATE|LANDFILL)$",
            message = "Select: RECYCLE, INCINERATE, or LANDFILL")
    private String wasteDisposal;  // "RECYCLE", "INCINERATE", "LANDFILL"

    // ==================== 2. ENERGY ====================

    @NotBlank(message = "Please select your energy source")
    @Pattern(regexp = "^(SOLAR|WIND|GRID|MIXED)$",
            message = "Select: SOLAR, WIND, GRID, or MIXED")
    private String energySource;  // "SOLAR", "WIND", "GRID", "MIXED"

    @NotNull(message = "Please tell us your renewable energy percentage")
    @Min(value = 0, message = "Renewable energy must be between 0 and 100%")
    @Max(value = 100, message = "Renewable energy must be between 0 and 100%")
    private Double renewablePercentage;  // "How much clean energy do you use?"

    // ==================== 3. CERTIFICATION ====================

    @Pattern(regexp = "^(NONE|GOTS|OEKO_TEX|BLUESIGN)$",
            message = "Select: NONE, GOTS, OEKO_TEX, or BLUESIGN")
    private String certification;  // "NONE", "GOTS", "OEKO_TEX", "BLUESIGN"

    // ==================== 4. COMPLIANCE ====================

    @NotNull(message = "Please tell us if you follow local environmental laws")
    private Boolean followsLaws;  // "Do you follow local environmental regulations?"

    // ==================== HELPER METHODS ====================

    public String getSustainabilitySummary() {
        return String.format(
                "♻️ Recycles %d%% | ⚡ %d%% renewable | 📜 %s",
                recyclingRate.intValue(),
                renewablePercentage.intValue(),
                certification != null ? certification : "No certification"
        );
    }

    public boolean isEnvironmentallyFriendly() {
        return recyclingRate >= 50 && renewablePercentage >= 30 && Boolean.TRUE.equals(followsLaws);
    }

    public String getRecommendation() {
        List<String> tips = new ArrayList<>();
        if (recyclingRate < 50) tips.add("♻️ Recycle more fabric waste - aim for 50%+");
        if (renewablePercentage < 30) tips.add("⚡ Use more clean energy - aim for 30%+");
        if (!Boolean.TRUE.equals(followsLaws)) tips.add("📋 Follow environmental laws");
        if ("NONE".equals(certification)) tips.add("🏆 Get eco-certification (GOTS, OEKO-TEX, or BLUESIGN)");
        if ("LANDFILL".equals(wasteDisposal)) tips.add("🗑️ Switch from landfill to recycling or incineration");

        return tips.isEmpty() ? "🎉 Great job! You're environmentally friendly!" :
                "💡 Tips to improve: " + String.join(", ", tips);
    }

    public double calculateSimpleScore() {
        double score = 0;
        // Recycling: up to 35 points
        score += (recyclingRate / 100) * 35;
        // Renewable energy: up to 35 points
        score += (renewablePercentage / 100) * 35;
        // Compliance: 20 points
        if (Boolean.TRUE.equals(followsLaws)) score += 20;
        // Certification: up to 10 points
        if ("OEKO_TEX".equals(certification)) score += 5;
        if ("GOTS".equals(certification)) score += 8;
        if ("BLUESIGN".equals(certification)) score += 10;
        // Waste disposal bonus: up to 10 points
        if ("RECYCLE".equals(wasteDisposal)) score += 10;
        if ("INCINERATE".equals(wasteDisposal)) score += 5;

        return Math.min(100, score);
    }

    public String getRating() {
        double score = calculateSimpleScore();
        if (score >= 80) return "PLATINUM";
        if (score >= 70) return "GOLD";
        if (score >= 60) return "SILVER";
        if (score >= 50) return "BRONZE";
        return "NEEDS IMPROVEMENT";
    }
}