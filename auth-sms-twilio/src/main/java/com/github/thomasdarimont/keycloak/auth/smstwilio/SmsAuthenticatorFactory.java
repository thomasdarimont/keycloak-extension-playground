package com.github.thomasdarimont.keycloak.auth.smstwilio;

import com.google.auto.service.AutoService;
import lombok.extern.jbosslog.JBossLog;
import org.keycloak.Config.Scope;
import org.keycloak.authentication.Authenticator;
import org.keycloak.authentication.AuthenticatorFactory;
import org.keycloak.models.AuthenticationExecutionModel.Requirement;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.provider.ProviderConfigurationBuilder;

import java.util.Collections;
import java.util.List;

@JBossLog
@AutoService(AuthenticatorFactory.class)
public class SmsAuthenticatorFactory implements AuthenticatorFactory {

    private static final SmsAuthenticator SINGLETON = new SmsAuthenticator();

    private static final List<ProviderConfigProperty> CONFIG_PROPERTIES;

    static {
        List<ProviderConfigProperty> list = ProviderConfigurationBuilder
                .create()
                .property()
                .name(SmsAuthenticator.CONFIG_SMS_API_KEY)
                .label("Twilio API-KEY")
                .type(ProviderConfigProperty.STRING_TYPE)
                .helpText("")
                .add()
                .property()
                .name(SmsAuthenticator.CONFIG_CODE_LENGTH)
                .label("Code Length")
                .type(ProviderConfigProperty.STRING_TYPE)
                .helpText("")
                .defaultValue(8)
                .add()
                .build();

        CONFIG_PROPERTIES = Collections.unmodifiableList(list);
    }

    public Authenticator create(KeycloakSession session) {
        return SINGLETON;
    }

    public String getId() {
        return SmsAuthenticator.PROVIDER_ID;
    }

    public void init(Scope scope) {
        // NOOP
    }

    public void postInit(KeycloakSessionFactory factory) {
        // NOOP
    }

    public List<ProviderConfigProperty> getConfigProperties() {
        return CONFIG_PROPERTIES;
    }

    public String getHelpText() {
        return "SMS Authenticate using Twilio.";
    }

    public String getDisplayType() {
        return "Twilio SMS Authentication";
    }

    public String getReferenceCategory() {
        log.debug("Method [getReferenceCategory]");
        return "sms-auth-code";
    }

    public boolean isConfigurable() {
        return true;
    }

    public Requirement[] getRequirementChoices() {
        return REQUIREMENT_CHOICES;
    }

    public boolean isUserSetupAllowed() {
        return true;
    }

    public void close() {
        // NOOP
    }
}