package org.example.fabric_waste_prediction.test;

import org.example.fabric_waste_prediction.DTO.SustainabilityInputDTO;
import org.example.fabric_waste_prediction.Validation.SustainabilityInputValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import static org.junit.jupiter.api.Assertions.*;

class SustainabilityInputValidationTest {

    private Validator validator;
    private SustainabilityInputDTO input;
    private SustainabilityInputValidator customValidator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
        input = new SustainabilityInputDTO();
        customValidator = new SustainabilityInputValidator();
    }

    @Test
    void testValidInput() {
        // Arrange - Using your simple DTO field names (6 questions)
        input.setWasteDisposal("RECYCLE");
        input.setRecyclingRate(75.0);
        input.setEnergySource("SOLAR");
        input.setRenewablePercentage(40.0);
        input.setCertification("GOTS");
        input.setFollowsLaws(true);

        // Act
        var violations = validator.validate(input);
        boolean isValid = customValidator.isValid(input, null);

        // Assert
        assertTrue(violations.isEmpty());
        assertTrue(isValid);
    }

    @Test
    void testValidInputWithDefaultValues() {
        // Arrange - Test with default values that should pass
        input.setWasteDisposal("RECYCLE");
        input.setRecyclingRate(60.0);
        input.setEnergySource("MIXED");
        input.setRenewablePercentage(30.0);
        input.setCertification("OEKO_TEX");
        input.setFollowsLaws(true);

        // Act
        var violations = validator.validate(input);
        boolean isValid = customValidator.isValid(input, null);

        // Assert
        assertTrue(violations.isEmpty());
        assertTrue(isValid);
    }

    @Test
    void testValidInputWithBluesign() {
        // Arrange - Test BLUESIGN certification
        input.setWasteDisposal("RECYCLE");
        input.setRecyclingRate(80.0);
        input.setEnergySource("WIND");
        input.setRenewablePercentage(50.0);
        input.setCertification("BLUESIGN");
        input.setFollowsLaws(true);

        // Act
        var violations = validator.validate(input);
        boolean isValid = customValidator.isValid(input, null);

        // Assert
        assertTrue(violations.isEmpty());
        assertTrue(isValid);
    }

    @Test
    void testValidInputWithIncineration() {
        // Arrange - Test INCINERATE disposal method
        input.setWasteDisposal("INCINERATE");
        input.setRecyclingRate(70.0);
        input.setEnergySource("SOLAR");
        input.setRenewablePercentage(35.0);
        input.setCertification("NONE");
        input.setFollowsLaws(true);

        // Act
        var violations = validator.validate(input);
        boolean isValid = customValidator.isValid(input, null);

        // Assert
        assertTrue(violations.isEmpty());
        assertTrue(isValid);
    }

    @ParameterizedTest
    @CsvSource({
            "INVALID, 75, Invalid waste disposal method",
            "RECYCLE, 101, Recycling rate must be between 0 and 100",
            "RECYCLE, -1, Recycling rate must be between 0 and 100",
            "LANDFILL, 50, Valid",
            "INCINERATE, 30, Valid"
    })
    void testBasicValidation(String wasteDisposal, Double recyclingRate, String expectedMessage) {
        // Arrange
        input.setWasteDisposal(wasteDisposal);
        input.setRecyclingRate(recyclingRate);
        input.setEnergySource("SOLAR");
        input.setRenewablePercentage(30.0);
        input.setCertification("NONE");
        input.setFollowsLaws(true);

        // Act
        var violations = validator.validate(input);

        // Assert
        if (expectedMessage.equals("Valid")) {
            assertTrue(violations.isEmpty(), "Should be valid");
        } else {
            assertFalse(violations.isEmpty());
            assertTrue(violations.stream()
                    .anyMatch(v -> v.getMessage().contains(expectedMessage)));
        }
    }

    @ParameterizedTest
    @CsvSource({
            "SOLAR, 30, Valid",
            "WIND, 30, Valid",
            "GRID, 30, Valid",
            "MIXED, 30, Valid",
            "INVALID, 30, Select: SOLAR, WIND, GRID, or MIXED"
    })
    void testEnergySourceValidation(String energySource, Double renewablePercentage, String expectedMessage) {
        // Arrange
        input.setWasteDisposal("RECYCLE");
        input.setRecyclingRate(50.0);
        input.setEnergySource(energySource);
        input.setRenewablePercentage(renewablePercentage);
        input.setCertification("NONE");
        input.setFollowsLaws(true);

        // Act
        var violations = validator.validate(input);

        // Assert
        if (expectedMessage.equals("Valid")) {
            assertTrue(violations.isEmpty(), "Should be valid");
        } else {
            assertFalse(violations.isEmpty());
            assertTrue(violations.stream()
                    .anyMatch(v -> v.getMessage().contains(expectedMessage)));
        }
    }

    @Test
    void testHighRecyclingRateWithLandfill() {
        // Arrange
        input.setWasteDisposal("LANDFILL");
        input.setRecyclingRate(80.0);
        input.setEnergySource("SOLAR");
        input.setRenewablePercentage(30.0);
        input.setCertification("NONE");
        input.setFollowsLaws(true);

        // Act
        boolean isValid = customValidator.isValid(input, null);

        // Assert - Should be allowed but with warning
        assertTrue(isValid, "High recycling rate with landfill should be allowed but with warning");
    }

    @Test
    void testLowRenewablePercentage() {
        // Arrange
        input.setWasteDisposal("RECYCLE");
        input.setRecyclingRate(50.0);
        input.setEnergySource("SOLAR");
        input.setRenewablePercentage(15.0);  // Low renewable percentage
        input.setCertification("NONE");
        input.setFollowsLaws(true);

        // Act
        boolean isValid = customValidator.isValid(input, null);

        // Assert - Should pass but with warning (not a hard error)
        assertTrue(isValid);
    }

    @Test
    void testGOTSCertification() {
        // Arrange
        input.setWasteDisposal("RECYCLE");
        input.setRecyclingRate(60.0);
        input.setEnergySource("SOLAR");
        input.setRenewablePercentage(40.0);
        input.setCertification("GOTS");
        input.setFollowsLaws(true);

        // Act
        boolean isValid = customValidator.isValid(input, null);

        // Assert - Should pass
        assertTrue(isValid);
    }

    @Test
    void testBluesignCertification() {
        // Arrange
        input.setWasteDisposal("RECYCLE");
        input.setRecyclingRate(70.0);
        input.setEnergySource("WIND");
        input.setRenewablePercentage(50.0);
        input.setCertification("BLUESIGN");
        input.setFollowsLaws(true);

        // Act
        boolean isValid = customValidator.isValid(input, null);

        // Assert - Should pass
        assertTrue(isValid);
    }

    @Test
    void testFollowsLawsRequired() {
        // Arrange
        input.setWasteDisposal("RECYCLE");
        input.setRecyclingRate(50.0);
        input.setEnergySource("SOLAR");
        input.setRenewablePercentage(30.0);
        input.setCertification("NONE");
        input.setFollowsLaws(false);  // Not following laws - should fail

        // Act
        boolean isValid = customValidator.isValid(input, null);

        // Assert - Should fail because following laws is required
        assertFalse(isValid, "Following laws is required");
    }

    @Test
    void testCombinedRateExceeds150() {
        // Arrange
        input.setWasteDisposal("RECYCLE");
        input.setRecyclingRate(90.0);
        input.setEnergySource("SOLAR");
        input.setRenewablePercentage(80.0);  // Combined = 170% > 150%
        input.setCertification("NONE");
        input.setFollowsLaws(true);

        // Act
        boolean isValid = customValidator.isValid(input, null);

        // Assert - Should fail because combined rate exceeds 150%
        assertFalse(isValid, "Combined recycling and renewable energy should not exceed 150%");
    }

    @Test
    void testSecurityValidation() {
        // Test for XSS prevention
        input.setWasteDisposal("<script>alert('xss')</script>");
        input.setRecyclingRate(50.0);
        input.setEnergySource("SOLAR");
        input.setRenewablePercentage(30.0);
        input.setCertification("NONE");
        input.setFollowsLaws(true);

        // Act
        var violations = validator.validate(input);

        // Assert
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("Invalid waste disposal method")));
    }

    @Test
    void testNullSafety() {
        // Arrange - All fields null
        input = new SustainabilityInputDTO();

        // Act
        var violations = validator.validate(input);

        // Assert
        assertFalse(violations.isEmpty());
        // Check for required fields that are @NotNull
        long requiredViolations = violations.stream()
                .filter(v -> {
                    String path = v.getPropertyPath().toString();
                    return path.equals("recyclingRate") ||
                            path.equals("wasteDisposal") ||
                            path.equals("energySource") ||
                            path.equals("renewablePercentage") ||
                            path.equals("followsLaws");
                })
                .count();
        assertTrue(requiredViolations > 0);
    }

    @Test
    void testEdgeCases() {
        // Test boundary values
        input.setWasteDisposal("RECYCLE");
        input.setRecyclingRate(0.0);  // Minimum
        input.setEnergySource("SOLAR");
        input.setRenewablePercentage(0.0);  // Minimum
        input.setCertification("NONE");
        input.setFollowsLaws(true);

        var violations = validator.validate(input);
        assertTrue(violations.isEmpty(), "Boundary values should be valid");

        // Test maximum values
        input.setRecyclingRate(100.0);
        input.setRenewablePercentage(100.0);
        violations = validator.validate(input);
        assertTrue(violations.isEmpty(), "Maximum values should be valid");
    }

    @Test
    void testScoreCalculation() {
        // Test that the simple score calculation works (updated for 6 questions)
        input.setWasteDisposal("RECYCLE");
        input.setRecyclingRate(80.0);
        input.setEnergySource("SOLAR");
        input.setRenewablePercentage(50.0);
        input.setCertification("BLUESIGN");  // Highest certification
        input.setFollowsLaws(true);

        double score = input.calculateSimpleScore();
        String rating = input.getRating();

        // 80% recycling = 28 points, 50% renewable = 17.5 points, compliance = 20, BLUESIGN = 10, RECYCLE bonus = 10
        // Total = 85.5 points
        assertTrue(score >= 80, "Good inputs should score 80+");
    }

    @Test
    void testAllValidCertifications() {
        // Test NONE certification
        input.setWasteDisposal("RECYCLE");
        input.setRecyclingRate(50.0);
        input.setEnergySource("SOLAR");
        input.setRenewablePercentage(30.0);
        input.setCertification("NONE");
        input.setFollowsLaws(true);

        var violations = validator.validate(input);
        assertTrue(violations.isEmpty(), "NONE certification should be valid");

        // Test OEKO_TEX certification
        input.setCertification("OEKO_TEX");
        violations = validator.validate(input);
        assertTrue(violations.isEmpty(), "OEKO_TEX certification should be valid");

        // Test GOTS certification
        input.setCertification("GOTS");
        violations = validator.validate(input);
        assertTrue(violations.isEmpty(), "GOTS certification should be valid");

        // Test BLUESIGN certification
        input.setCertification("BLUESIGN");
        violations = validator.validate(input);
        assertTrue(violations.isEmpty(), "BLUESIGN certification should be valid");
    }

    @Test
    void testAllValidEnergySources() {
        String[] sources = {"SOLAR", "WIND", "GRID", "MIXED"};

        for (String source : sources) {
            input.setWasteDisposal("RECYCLE");
            input.setRecyclingRate(50.0);
            input.setEnergySource(source);
            input.setRenewablePercentage(30.0);
            input.setCertification("NONE");
            input.setFollowsLaws(true);

            var violations = validator.validate(input);
            assertTrue(violations.isEmpty(), source + " should be valid");
        }
    }

    @Test
    void testAllValidDisposalMethods() {
        String[] methods = {"RECYCLE", "LANDFILL", "INCINERATE"};

        for (String method : methods) {
            input.setWasteDisposal(method);
            input.setRecyclingRate(50.0);
            input.setEnergySource("SOLAR");
            input.setRenewablePercentage(30.0);
            input.setCertification("NONE");
            input.setFollowsLaws(true);

            var violations = validator.validate(input);
            assertTrue(violations.isEmpty(), method + " should be valid");
        }
    }

    @Test
    void testScoreForDifferentCertifications() {
        // Test score with different certifications
        input.setWasteDisposal("RECYCLE");
        input.setRecyclingRate(70.0);
        input.setEnergySource("SOLAR");
        input.setRenewablePercentage(40.0);
        input.setFollowsLaws(true);

        // No certification
        input.setCertification("NONE");
        double scoreNone = input.calculateSimpleScore();

        // OEKO_TEX
        input.setCertification("OEKO_TEX");
        double scoreOeko = input.calculateSimpleScore();
        assertTrue(scoreOeko > scoreNone, "OEKO_TEX should give higher score");

        // GOTS
        input.setCertification("GOTS");
        double scoreGots = input.calculateSimpleScore();
        assertTrue(scoreGots > scoreOeko, "GOTS should give higher score than OEKO_TEX");

        // BLUESIGN
        input.setCertification("BLUESIGN");
        double scoreBluesign = input.calculateSimpleScore();
        assertTrue(scoreBluesign > scoreGots, "BLUESIGN should give highest score");
    }

    @Test
    void testScoreForDifferentDisposalMethods() {
        // Test score with different disposal methods
        input.setRecyclingRate(70.0);
        input.setEnergySource("SOLAR");
        input.setRenewablePercentage(40.0);
        input.setCertification("NONE");
        input.setFollowsLaws(true);

        // Landfill
        input.setWasteDisposal("LANDFILL");
        double scoreLandfill = input.calculateSimpleScore();

        // Incineration
        input.setWasteDisposal("INCINERATE");
        double scoreIncinerate = input.calculateSimpleScore();
        assertTrue(scoreIncinerate > scoreLandfill, "Incineration should give higher score than landfill");

        // Recycle
        input.setWasteDisposal("RECYCLE");
        double scoreRecycle = input.calculateSimpleScore();
        assertTrue(scoreRecycle > scoreIncinerate, "Recycle should give highest score");
    }
}