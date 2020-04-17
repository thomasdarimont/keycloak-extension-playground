package com.github.thomasdarimont.keycloak.auth.authzpolicy;

import com.google.auto.service.AutoService;
import org.keycloak.Config;
import org.keycloak.authentication.Authenticator;
import org.keycloak.authentication.AuthenticatorFactory;
import org.keycloak.models.AuthenticationExecutionModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.provider.ProviderConfigProperty;

import java.util.Arrays;
import java.util.List;

@AutoService(AuthenticatorFactory.class)
public class AuthzPolicyAuthenticatorFactory implements AuthenticatorFactory {

    private static final String PROVIDER_ID = "auth-check-authz-policy";

    @Override
    public String getDisplayType() {
        return "Check Authz Policy";
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
            AuthenticationExecutionModel.Requirement.REQUIRED, AuthenticationExecutionModel.Requirement.ALTERNATIVE, AuthenticationExecutionModel.Requirement.DISABLED
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
        return "Evaluates an Authorization Policy to determine client access";
    }

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {

        ProviderConfigProperty clientListPolicy = new ProviderConfigProperty();
        clientListPolicy.setType(ProviderConfigProperty.TEXT_TYPE);
        clientListPolicy.setName(AuthzPolicyAuthenticator.CLIENTS_POLICY);
        clientListPolicy.setLabel("Clients Policy");
        clientListPolicy.setHelpText("References the clients policy defined in the Authorization/Policies section of the realm-management client.");

        ProviderConfigProperty rolePolicy = new ProviderConfigProperty();
        rolePolicy.setType(ProviderConfigProperty.TEXT_TYPE);
        rolePolicy.setName(AuthzPolicyAuthenticator.ROLES_POLICY);
        rolePolicy.setLabel("Roles Policy");
        rolePolicy.setHelpText("References the roles policy defined in the Authorization/Policies section of the realm-management client.");

        return Arrays.asList(clientListPolicy, rolePolicy);
    }

    @Override
    public void close() {
        // NOOP
    }

    @Override
    public Authenticator create(KeycloakSession session) {
        return new AuthzPolicyAuthenticator(session);
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
