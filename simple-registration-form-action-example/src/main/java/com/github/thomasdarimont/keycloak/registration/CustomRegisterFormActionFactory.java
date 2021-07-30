package com.github.thomasdarimont.keycloak.registration;

import com.google.auto.service.AutoService;
import org.keycloak.Config;
import org.keycloak.authentication.FormAction;
import org.keycloak.authentication.FormActionFactory;
import org.keycloak.models.AuthenticationExecutionModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.provider.ProviderConfigProperty;

import java.util.List;

@AutoService(FormActionFactory.class)
public class CustomRegisterFormActionFactory implements FormActionFactory {

    private static final CustomRegisterFormAction INSTANCE = new CustomRegisterFormAction();

    private static final String ID = "custom-registration-example-form-action";

    private static AuthenticationExecutionModel.Requirement[] REQUIREMENT_CHOICES = {
            AuthenticationExecutionModel.Requirement.REQUIRED,
            AuthenticationExecutionModel.Requirement.DISABLED
    };

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public FormAction create(KeycloakSession session) {
        return INSTANCE;
    }

    @Override
    public String getHelpText() {
        return "Custom registration action example help text.";
    }

    @Override
    public String getDisplayType() {
        return "Custom Registration: Example action";
    }

    @Override
    public String getReferenceCategory() {
        return null; // null ok
    }

    @Override
    public boolean isConfigurable() {
        return false;
    }

    @Override
    public AuthenticationExecutionModel.Requirement[] getRequirementChoices() {
        return REQUIREMENT_CHOICES;
    }

    @Override
    public boolean isUserSetupAllowed() {
        return false;
    }

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return null; // null ok
    }

    @Override
    public void init(Config.Scope config) {
        // NOOP
    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {
        // NOOP
    }

    @Override
    public void close() {
        // NOOP
    }
}
