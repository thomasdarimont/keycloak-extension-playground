package com.github.thomasdarimont.keycloak.trustdevice.auth;

import com.google.auto.service.AutoService;
import org.keycloak.Config;
import org.keycloak.authentication.AuthenticatorFactory;
import org.keycloak.authentication.authenticators.conditional.ConditionalAuthenticator;
import org.keycloak.authentication.authenticators.conditional.ConditionalAuthenticatorFactory;
import org.keycloak.models.AuthenticationExecutionModel;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.provider.ProviderConfigProperty;

import java.util.Arrays;
import java.util.List;

@AutoService(AuthenticatorFactory.class)
public class TrustedDeviceConditionFactory implements ConditionalAuthenticatorFactory {

    private static final TrustedDeviceCondition INSTANCE = new TrustedDeviceCondition();

    @Override
    public ConditionalAuthenticator getSingleton() {
        return INSTANCE;
    }

    @Override
    public String getDisplayType() {
        return "Condition - Trusted Device";
    }

    @Override
    public String getReferenceCategory() {
        return "condition";
    }

    @Override
    public boolean isConfigurable() {
        return true;
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
    public String getHelpText() {
        return "Flow is executed only if the current device is trusted.";
    }

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {

        ProviderConfigProperty inverted = new ProviderConfigProperty();
        inverted.setType(ProviderConfigProperty.BOOLEAN_TYPE);
        inverted.setName("negated");
        inverted.setLabel("Negated");
        inverted.setHelpText("If this is on, the matching logic is negated.");

        return Arrays.asList(inverted);
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

    @Override
    public String getId() {
        return TrustedDeviceCondition.ID;
    }
}
