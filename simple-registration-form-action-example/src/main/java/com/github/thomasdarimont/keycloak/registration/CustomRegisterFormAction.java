package com.github.thomasdarimont.keycloak.registration;

import lombok.extern.jbosslog.JBossLog;
import org.keycloak.authentication.FormAction;
import org.keycloak.authentication.FormContext;
import org.keycloak.authentication.ValidationContext;
import org.keycloak.events.Errors;
import org.keycloak.forms.login.LoginFormsProvider;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.utils.FormMessage;

import javax.ws.rs.core.MultivaluedMap;
import java.util.List;

@JBossLog
public class CustomRegisterFormAction implements FormAction {

    private static final String CUSTOM_FIELD = "customField";

    private static final String CUSTOM_FIELD_REQUIRED_MESSAGE = "customFieldRequired";

    @Override
    public void buildPage(FormContext context, LoginFormsProvider form) {
        // render user input form
    }

    @Override
    public void validate(ValidationContext context) {
        // validate user input

        MultivaluedMap<String, String> formData = context.getHttpRequest().getDecodedFormParameters();

        if (!formData.containsKey(CUSTOM_FIELD)) {
            context.error(Errors.INVALID_REGISTRATION);
            formData.remove(CUSTOM_FIELD);

            List<FormMessage> errors = List.of(new FormMessage(CUSTOM_FIELD, CUSTOM_FIELD_REQUIRED_MESSAGE));
            context.validationError(formData, errors);
            return;
        }

        context.success();
    }

    @Override
    public void success(FormContext context) {
        // handle successful form submission
        log.info("Custom Form Action success!");
    }

    @Override
    public boolean requiresUser() {
        return true;
    }

    @Override
    public boolean configuredFor(KeycloakSession session, RealmModel realm, UserModel user) {
        return true;
    }

    @Override
    public void setRequiredActions(KeycloakSession session, RealmModel realm, UserModel user) {
        // add required actions if required
    }

    @Override
    public void close() {
        // NOOP
    }
}
