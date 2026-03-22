package org.example.fabric_waste_prediction.Validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import org.example.fabric_waste_prediction.Validation.SustainabilityInputValidator;

import java.lang.annotation.*;

@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = SustainabilityInputValidator.class)
@Documented
public @interface ValidSustainabilityInput {
    String message() default "Invalid sustainability input combination";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};

    // Validation groups for different scenarios
    String scenario() default "DEFAULT";

    // Severity levels
    String severity() default "ERROR";

    // Whether to check business rules
    boolean checkBusinessRules() default true;
}