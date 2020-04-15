package com.github.thomasdarimont.keycloak.auth.accesspolicy;

import com.github.thomasdarimont.keycloak.auth.accesspolicy.support.AccessPolicyParser;
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

import static org.keycloak.provider.ProviderConfigProperty.SCRIPT_TYPE;

@AutoService(AuthenticatorFactory.class)
public class AccessPolicyAuthenticatorFactory implements AuthenticatorFactory {

    private static final String PROVIDER_ID = "auth-check-access-policy";

    static final String ACCESS_POLICY = "accessPolicy";

    static final AccessPolicyAuthenticator AUTHENTICATOR = new AccessPolicyAuthenticator(new AccessPolicyParser());

    @Override
    public String getDisplayType() {
        return "Check Access Policy";
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
        return "Evaluates an Access Policy to determine client access";
    }

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {

        ProviderConfigProperty accessPolicy = new ProviderConfigProperty();
        accessPolicy.setType(SCRIPT_TYPE);
        accessPolicy.setName(ACCESS_POLICY);
        accessPolicy.setLabel("Access Policy");
        accessPolicy.setHelpText("An access-policy can be defined as a JSON document which holds a list 'p' of access-policy entries. " +
                "An access-policy-entry consists of a client-id regex pattern 'app' and a list of allowed realm- or client-role names. " +
                "Client roles have the form 'clientId.roleName'. " +
                "If a client is not contained in the access-policy the access is always granted. " +
                "If a client is contained in the access-policy but contains an role list with the sole value 'NONE', then access is always denied. " +
                "{\"p\":[\n" +
                "    { \"app\": \"clientIdRegex\", \"allow\": [\"role1\",\"role2\"] }\n" +
                "]}");

        return Arrays.asList(accessPolicy);
    }

    @Override
    public void close() {
        // NOOP
    }

    @Override
    public Authenticator create(KeycloakSession session) {
        return AUTHENTICATOR;
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
