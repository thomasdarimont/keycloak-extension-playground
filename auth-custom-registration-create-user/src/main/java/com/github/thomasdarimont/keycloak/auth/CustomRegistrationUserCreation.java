package com.github.thomasdarimont.keycloak.auth;

import com.google.auto.service.AutoService;
import org.keycloak.authentication.FormActionFactory;
import org.keycloak.authentication.FormContext;
import org.keycloak.authentication.ValidationContext;
import org.keycloak.authentication.forms.RegistrationUserCreation;
import org.keycloak.events.Errors;
import org.keycloak.forms.login.LoginFormsProvider;
import org.keycloak.models.utils.FormMessage;

import javax.ws.rs.core.MultivaluedMap;
import java.util.List;

@AutoService(FormActionFactory.class)
public class CustomRegistrationUserCreation extends RegistrationUserCreation {

    public static final String ID = "demo-registration-user-creation";
    public static final String TERMS_FIELD = "terms";
    public static final String TERMS_ACCEPTED_ATTRIBUTE = "terms_accepted";
    public static final String ACCEPT_TERMS_REQUIRED = "acceptTermsRequired";
    public static final String TERMS_REQUIRED_MESSAGE = "termsRequired";

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public String getDisplayType() {
        return "Demo: Registration User Creation with Terms";
    }

    @Override
    public void validate(ValidationContext context) {

        MultivaluedMap<String, String> formData = context.getHttpRequest().getDecodedFormParameters();
        boolean termsAccepted = formData.containsKey(TERMS_FIELD);

        if (!termsAccepted) {
            context.error(Errors.INVALID_REGISTRATION);
            formData.remove(TERMS_FIELD);

            List<FormMessage> errors = List.of(new FormMessage(TERMS_FIELD, TERMS_REQUIRED_MESSAGE));
            context.validationError(formData, errors);
            return;
        }
        super.validate(context);
    }

    @Override
    public void success(FormContext context) {
        super.success(context);

        context.getUser().setSingleAttribute(TERMS_ACCEPTED_ATTRIBUTE, String.valueOf(System.currentTimeMillis()));
    }

    @Override
    public void buildPage(FormContext context, LoginFormsProvider form) {
        form.setAttribute(ACCEPT_TERMS_REQUIRED, true);
    }
}
