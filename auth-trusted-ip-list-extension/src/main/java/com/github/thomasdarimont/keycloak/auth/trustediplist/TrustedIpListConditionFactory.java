package com.github.thomasdarimont.keycloak.auth.trustediplist;

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
public class TrustedIpListConditionFactory implements ConditionalAuthenticatorFactory {

    private static final TrustedIpListCondition INSTANCE = new TrustedIpListCondition();

    @Override
    public ConditionalAuthenticator getSingleton() {
        return INSTANCE;
    }

    @Override
    public String getDisplayType() {
        return "Condition - IP address is not trusted";
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
        return "Flow is executed only if the current IP is not trusted. Use user attr. 'trusted-ip-list' as a json array of IPs and CIDRs.";
    }

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {

        ProviderConfigProperty inverted = new ProviderConfigProperty();
        inverted.setType(ProviderConfigProperty.BOOLEAN_TYPE);
        inverted.setName("negated");
        inverted.setLabel("Negated");
        inverted.setDefaultValue(false);
        inverted.setHelpText("If this is on, the matching logic is negated.");

        ProviderConfigProperty skipEmptyList = new ProviderConfigProperty();
        skipEmptyList.setType(ProviderConfigProperty.BOOLEAN_TYPE);
        skipEmptyList.setName("skip_empty_list");
        skipEmptyList.setLabel("Skip empty list");
        skipEmptyList.setDefaultValue(true);
        skipEmptyList.setHelpText("If this is on and user not provided trusted list, always returning true.");

        return Arrays.asList(inverted, skipEmptyList);
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
        return TrustedIpListCondition.ID;
    }
}
