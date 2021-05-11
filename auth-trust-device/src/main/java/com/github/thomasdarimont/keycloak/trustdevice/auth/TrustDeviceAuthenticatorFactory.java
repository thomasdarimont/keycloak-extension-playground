package com.github.thomasdarimont.keycloak.trustdevice.auth;

import com.google.auto.service.AutoService;
import org.keycloak.Config;
import org.keycloak.authentication.Authenticator;
import org.keycloak.authentication.AuthenticatorFactory;
import org.keycloak.models.AuthenticationExecutionModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.provider.ProviderConfigProperty;

import java.util.List;

@AutoService(AuthenticatorFactory.class)
public class TrustDeviceAuthenticatorFactory implements AuthenticatorFactory {

    private static final TrustDeviceAuthenticator INSTANCE = new TrustDeviceAuthenticator();

    @Override
    public String getId() {
        return TrustDeviceAuthenticator.ID;
    }

    @Override
    public String getDisplayType() {
        return "Verify Trusted Device";
    }

    @Override
    public String getHelpText() {
        return "Validates the device cookie set by the auth server.";
    }

    @Override
    public String getReferenceCategory() {
        return "cookie";
    }

    @Override
    public AuthenticationExecutionModel.Requirement[] getRequirementChoices() {
        return REQUIREMENT_CHOICES;
    }

    @Override
    public Authenticator create(KeycloakSession session) {
        return INSTANCE;
    }

    @Override
    public boolean isConfigurable() {
        return false;
    }

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return null;
    }

    @Override
    public boolean isUserSetupAllowed() {
        return false;
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
