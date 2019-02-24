package com.github.thomasdarimont.keycloak.register;

import org.keycloak.authentication.FormAction;
import org.keycloak.authentication.FormContext;
import org.keycloak.authentication.ValidationContext;
import org.keycloak.events.Details;
import org.keycloak.events.Errors;
import org.keycloak.forms.login.LoginFormsProvider;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.utils.FormMessage;
import org.keycloak.services.validation.Validation;

import javax.ws.rs.core.MultivaluedMap;
import java.util.ArrayList;
import java.util.List;

/**
 * Adapted from
 */
public class RegistrationValidateMobileFormAction implements FormAction {

    private static final String MOBILE_NUMBER_FIELD = "mobile";
    private static final String MOBILE_NUMBER_USER_ATTRIBUTE = "mobile";

    @Override
    public void buildPage(FormContext context, LoginFormsProvider form) {
        // NOOP
    }

    @Override
    public void validate(ValidationContext context) {

        // called when the user submits the registration form

        MultivaluedMap<String, String> formData = context.getHttpRequest().getDecodedFormParameters();
        List<FormMessage> errors = new ArrayList<>();

        context.getEvent().detail(Details.REGISTER_METHOD, "form");
        String eventError = Errors.INVALID_REGISTRATION;

        String mobilePhoneNumber = formData.getFirst(MOBILE_NUMBER_FIELD);
        if (Validation.isBlank(mobilePhoneNumber)) {
            errors.add(new FormMessage(MOBILE_NUMBER_FIELD, "missingMobileMessage"));
        } else if (!MobileValidation.isPhoneNumberValid(mobilePhoneNumber)) {
            context.getEvent().detail("mobile_phone_number", mobilePhoneNumber);
            errors.add(new FormMessage(MOBILE_NUMBER_FIELD, "invalidMobileMessage"));
        }

        if (errors.isEmpty()) {
            context.success();
            return;
        }

        context.error(eventError);
        context.validationError(formData, errors);
    }

    @Override
    public void success(FormContext context) {

        // called after successful validation

        UserModel user = context.getUser();
        MultivaluedMap<String, String> formData = context.getHttpRequest().getDecodedFormParameters();
        user.setSingleAttribute(MOBILE_NUMBER_USER_ATTRIBUTE, formData.getFirst(MOBILE_NUMBER_FIELD));
    }

    @Override
    public boolean requiresUser() {
        return false;
    }

    @Override
    public boolean configuredFor(KeycloakSession session, RealmModel realm, UserModel user) {
        return true;
    }

    @Override
    public void setRequiredActions(KeycloakSession session, RealmModel realm, UserModel user) {
    }

    @Override
    public void close() {
    }
}