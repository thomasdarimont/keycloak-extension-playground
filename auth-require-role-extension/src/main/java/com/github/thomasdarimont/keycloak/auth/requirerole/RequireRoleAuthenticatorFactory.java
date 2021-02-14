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

public class RequireRoleAuthenticatorFactory implements AuthenticatorFactory {

    private static final String PROVIDER_ID = "require-role";

    static final String ROLE = "role";

    public static final RequireRoleAuthenticator ROLE_AUTHENTICATOR = new RequireRoleAuthenticator();

    @Override
    public String getDisplayType() {
        return "Require Role";
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
        role.setType(ProviderConfigProperty.STRING_TYPE);
        role.setName(ROLE);
        role.setLabel("Role name");
        role.setHelpText("Required role name that a user needs to have to proceed with the authentication. " +
                "This can be a realm or a client role name. Client roles have the form clientId.roleName. " +
                "One can use the expression ${clientId}.roleName to check for the same role across many different clients. " +
                "If the given role is not found / defined for a client the authenticator will be marked as successful. ");

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
