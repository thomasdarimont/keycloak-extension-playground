package com.github.thomasdarimont.keycloak.auth.requiregroup;

import org.keycloak.Config;
import org.keycloak.authentication.Authenticator;
import org.keycloak.authentication.AuthenticatorFactory;
import org.keycloak.models.AuthenticationExecutionModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.provider.ProviderConfigProperty;

import java.util.Arrays;
import java.util.List;

public class RequireGroupAuthenticatorFactory implements AuthenticatorFactory {

    private static final String PROVIDER_ID = "require-group";

    static final String GROUP = "group";

    public static final RequireGroupAuthenticator GROUP_AUTHENTICATOR = new RequireGroupAuthenticator();

    @Override
    public String getDisplayType() {
        return "Require Group";
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
        return "Requires the user to be a member of a given group. Note that nested group paths have the form: /parentGroup/childGroup";
    }

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {

        // TODO add support for selecting an existing group, similar to role selection

        ProviderConfigProperty group = new ProviderConfigProperty();
        group.setType(ProviderConfigProperty.STRING_TYPE);
        group.setName(GROUP);
        group.setLabel("Group");
        group.setHelpText("Required group");

        return Arrays.asList(group);
    }

    @Override
    public void close() {
        // NOOP
    }

    @Override
    public Authenticator create(KeycloakSession session) {
        return GROUP_AUTHENTICATOR;
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
