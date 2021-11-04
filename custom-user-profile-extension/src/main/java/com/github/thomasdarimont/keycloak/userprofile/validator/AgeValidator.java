package com.github.thomasdarimont.keycloak.userprofile.validator;

import com.google.auto.service.AutoService;
import org.keycloak.models.KeycloakSession;
import org.keycloak.validate.AbstractStringValidator;
import org.keycloak.validate.ValidationContext;
import org.keycloak.validate.ValidationError;
import org.keycloak.validate.ValidationResult;
import org.keycloak.validate.ValidatorConfig;
import org.keycloak.validate.ValidatorFactory;

import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;

@AutoService(ValidatorFactory.class)
public class AgeValidator extends AbstractStringValidator {

    public static final String ID = "custom-age";

    public static final String MESSAGE_AGE_INVALID = "error-invalid-age";

    public static final String MESSAGE_AGE_TOO_YOUNG = "error-age-too-young";

    public static final String MESSAGE_AGE_TOO_OLD = "error-age-too-old";

    public static final AgeValidator INSTANCE = new AgeValidator();

    @Override
    public String getId() {
        return ID;
    }


    @Override
    protected void doValidate(String value, String inputHint, ValidationContext context, ValidatorConfig config) {

        LocalDate date;

        try {
            date = LocalDate.parse(value);
        } catch (DateTimeParseException dtpe) {
            context.addError(new ValidationError(ID, inputHint, MESSAGE_AGE_INVALID, value));
            return;
        }

        int ageInYears = date.until(LocalDate.now()).getYears();

        Integer minAge = config.getInt("min-age");
        if (minAge != null && ageInYears < minAge) {
            context.addError(new ValidationError(ID, inputHint, MESSAGE_AGE_TOO_YOUNG, value));
        }

        Integer maxAge = config.getInt("max-age");
        if (maxAge != null && ageInYears > maxAge) {
            context.addError(new ValidationError(ID, inputHint, MESSAGE_AGE_TOO_OLD, value));
        }
    }

    @Override
    public ValidationResult validateConfig(KeycloakSession session, ValidatorConfig config) {
        return super.validateConfig(session, config);
    }
}
