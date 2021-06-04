package com.github.thomasdarimont.keycloak.trustdevice.auth;

import com.github.thomasdarimont.keycloak.trustdevice.model.TrustedDeviceModel;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.authenticators.conditional.ConditionalAuthenticator;
import org.keycloak.models.AuthenticatorConfigModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;

import java.util.Map;

public class TrustedDeviceCondition implements ConditionalAuthenticator {

    public static final String ID = "custom-trusted-device-condition";

    static final String NEGATED_CONDITION = "negated";

    @Override
    public boolean matchCondition(AuthenticationFlowContext context) {

        boolean negated = isNegated(context);

        TrustedDeviceModel device = TrustedDeviceAuthenticator.lookupTrustedDevice(context);

        boolean trusted = device != null;
        if (negated) {
            trusted = !trusted;
        }

        return trusted;
    }

    private boolean isNegated(AuthenticationFlowContext context) {
        AuthenticatorConfigModel authenticatorConfig = context.getAuthenticatorConfig();
        if (authenticatorConfig == null) {
            return false;
        }
        Map<String, String> config = authenticatorConfig.getConfig();
        if (config == null) {
            return false;
        }
        return Boolean.parseBoolean(config.getOrDefault(NEGATED_CONDITION, "false"));
    }

    @Override
    public void action(AuthenticationFlowContext context) {
        // NOOP
    }

    @Override
    public boolean requiresUser() {
        return true;
    }

    @Override
    public void setRequiredActions(KeycloakSession session, RealmModel realm, UserModel user) {
        // NOOP
    }

    @Override
    public void close() {
        // NOOP
    }
}
