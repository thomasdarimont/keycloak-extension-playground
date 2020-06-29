package com.github.thomasdarimont.keycloak.auth.requirerole;

import org.keycloak.Config;
import org.keycloak.authentication.Authenticator;
import org.keycloak.authentication.AuthenticatorFactory;
import org.keycloak.models.AuthenticationExecutionModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.provider.ProviderConfigProperty;

import java.util.Arrays;
import java.util.List;

import static org.keycloak.provider.ProviderConfigProperty.ROLE_TYPE;

public class RequireRoleDirectGrantAuthenticatorFactory implements AuthenticatorFactory {

    private static final String PROVIDER_ID = "require-role-direct-grant";

    static final String ROLE = "role";

    public static final RequireRoleDirectGrantAuthenticator ROLE_AUTHENTICATOR = new RequireRoleDirectGrantAuthenticator();

    @Override
    public String getDisplayType() {
        return "Require Role for Direct Grant";
    }

    @Override
    public String getReferenceCategory() {
        return null;
    }

    @Override
    public boolean isConfigurable() {
        return true;
    }

    public static final AuthenticationExecutionModel.Requirement[] REQUIREMENT_CHOICES = {
            AuthenticationExecutionModel.Requirement.REQUIRED, AuthenticationExecutionModel.Requirement.DISABLED
    };

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
        return "Requires the user to have a given role.";
    }

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {

        ProviderConfigProperty role = new ProviderConfigProperty();
        role.setType(ROLE_TYPE);
        role.setName(ROLE);
        role.setLabel("Role");
        role.setHelpText("Require role for direct grant.");

        return Arrays.asList(role);
    }

    @Override
    public void close() {
        // NOOP
    }

    @Override
    public Authenticator create(KeycloakSession session) {
        return ROLE_AUTHENTICATOR;
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
    public String getId() {
        return PROVIDER_ID;
    }
}
