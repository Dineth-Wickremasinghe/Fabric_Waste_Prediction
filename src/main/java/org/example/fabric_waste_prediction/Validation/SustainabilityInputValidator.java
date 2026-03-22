package org.example.fabric_waste_prediction.Validation;

import org.example.fabric_waste_prediction.dto.SustainabilityInputDTO;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class SustainabilityInputValidator implements
        ConstraintValidator<ValidSustainabilityInput, SustainabilityInputDTO> {

    private String scenario;
    private boolean checkBusinessRules;
    private Set<String> validDisposalMethods = new HashSet<>(Arrays.asList(
            "RECYCLE", "INCINERATE", "LANDFILL"
    ));
    private Set<String> validEnergySources = new HashSet<>(Arrays.asList(
            "SOLAR", "WIND", "GRID", "MIXED"
    ));
    private Set<String> validCertifications = new HashSet<>(Arrays.asList(
            "NONE", "GOTS", "OEKO_TEX", "BLUESIGN"
    ));

    @Override
    public void initialize(ValidSustainabilityInput constraintAnnotation) {
        this.scenario = constraintAnnotation.scenario();
        this.checkBusinessRules = constraintAnnotation.checkBusinessRules();
    }

    @Override
    public boolean isValid(SustainabilityInputDTO input,
                           ConstraintValidatorContext context) {
        boolean isValid = true;

        context.disableDefaultConstraintViolation();

        // Rule 1: Waste disposal method must be valid
        if (!validDisposalMethods.contains(input.getWasteDisposal())) {
            addViolation(context, "wasteDisposal",
                    "Invalid waste disposal method. Must be one of: RECYCLE, INCINERATE, LANDFILL");
            isValid = false;
        }

        // Rule 2: High recycling rate should use RECYCLE method
        if (input.getRecyclingRate() > 50 && !"RECYCLE".equals(input.getWasteDisposal())) {
            addViolation(context, "wasteDisposal",
                    "High recycling rate (>50%) should use RECYCLE disposal method");
            isValid = false;
        }

        // Rule 3: Energy source validation
        if (!validEnergySources.contains(input.getEnergySource())) {
            addViolation(context, "energySource",
                    "Invalid energy source. Must be one of: SOLAR, WIND, GRID, MIXED");
            isValid = false;
        }

        // Rule 4: Renewable energy recommendation
        if (input.getRenewablePercentage() < 30 && input.getRenewablePercentage() > 0) {
            // Warning only, not a hard error
        }

        // Rule 5: Certification validation
        if (!validCertifications.contains(input.getCertification())) {
            addViolation(context, "certification",
                    "Invalid certification. Must be one of: NONE, GOTS, OEKO_TEX, BLUESIGN");
            isValid = false;
        }

        // Rule 6: GOTS certification requires high recycling
        if ("GOTS".equals(input.getCertification()) && input.getRecyclingRate() < 70) {
            addViolation(context, "certification",
                    "GOTS certification is recommended but requires 70%+ recycling rate");
            // This is a suggestion, not a hard error
        }

        // Rule 7: Compliance check
        if (!Boolean.TRUE.equals(input.getFollowsLaws())) {
            addViolation(context, "followsLaws",
                    "Following local environmental laws is required for compliance");
            isValid = false;
        }

        // Rule 8: Combined recycling and renewable energy check
        if (input.getRecyclingRate() + input.getRenewablePercentage() > 150) {
            addViolation(context, "recyclingRate",
                    "Combined recycling rate and renewable energy percentage exceeds 150%");
            isValid = false;
        }

        return isValid;
    }

    private void addViolation(ConstraintValidatorContext context,
                              String field, String message) {
        context.buildConstraintViolationWithTemplate(message)
                .addPropertyNode(field)
                .addConstraintViolation();
    }
}