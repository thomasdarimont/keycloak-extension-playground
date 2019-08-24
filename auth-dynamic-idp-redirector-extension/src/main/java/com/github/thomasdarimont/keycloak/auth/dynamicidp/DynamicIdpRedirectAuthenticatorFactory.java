package com.github.thomasdarimont.keycloak.auth.dynamicidp;

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
public class DynamicIdpRedirectAuthenticatorFactory implements AuthenticatorFactory {

    private static final String PROVIDER_ID = "auth-dynamic-idp-redirector";



    @Override
    public String getDisplayType() {
        return "Dynamic IDP Redirector";
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
        return "Dynamic IDP Redirector";
    }

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {

        ProviderConfigProperty emailToIdpMapping = new ProviderConfigProperty();
        emailToIdpMapping.setType(ProviderConfigProperty.STRING_TYPE);
        emailToIdpMapping.setName(DynamicIdpRedirectAuthenticator.EMAIL_TO_IDP_MAPPING_CONFIG_PROPERTY);
        emailToIdpMapping.setLabel("Email IDP Mapping");
        emailToIdpMapping.setHelpText("Email Suffix pattern to IDP Mapping. email-suffix/idp-id, multiple patterns can be delimited via ';', c.f.: example.com/idp1;.*foo.com/idp2;.*bar.(com|de)/idp3");

        return Arrays.asList(emailToIdpMapping);
    }

    @Override
    public void close() {
        // NOOP
    }

    @Override
    public Authenticator create(KeycloakSession session) {
        return new DynamicIdpRedirectAuthenticator(session);
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