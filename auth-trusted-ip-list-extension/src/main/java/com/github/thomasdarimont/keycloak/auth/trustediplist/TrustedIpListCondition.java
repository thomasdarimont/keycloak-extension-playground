package com.github.thomasdarimont.keycloak.auth.trustediplist;

import lombok.extern.jbosslog.JBossLog;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.authenticators.conditional.ConditionalAuthenticator;
import org.keycloak.models.AuthenticatorConfigModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.util.JsonSerialization;

import java.io.IOException;
import java.util.Map;

@JBossLog
public class TrustedIpListCondition implements ConditionalAuthenticator {
    public static final String ID = "custom-trusted-ip-list-condition";

    static final String NEGATED_CONDITION = "negated";

    public static final String TRUSTED_IP_LIST_PROPERTY = "trusted-ip-list";


    public TrustedIpListCondition() {
    }

    private boolean ipMatches(String ip, String subnet) {
        IpAddressMatcher ipAddressMatcher = new IpAddressMatcher(subnet);
        return ipAddressMatcher.matches(ip);
    }

    @Override
    public boolean matchCondition(AuthenticationFlowContext context) {
        UserModel user = context.getUser();
        if (user == null) {
            return false;
        }

        String[] trustedIpList = determineTrustedIpListAttribute(user);

        boolean trusted;

        if (trustedIpList.length == 0) {
            trusted = true;
        } else{
            trusted = false;

            String userIP = context.getConnection().getRemoteAddr();
            for (String s : trustedIpList) {
                if (ipMatches(userIP, s)) {
                    trusted = true;
                    break;
                }
            }

        }


        if (isNegated(context)) {
            return !trusted;
        }

        return trusted;
    }

    private String[] determineTrustedIpListAttribute(UserModel user) {
        String attr = user.getFirstAttribute(TRUSTED_IP_LIST_PROPERTY);
        if (attr == null) {
            return new String[]{};
        }
        try {
            return JsonSerialization.readValue(attr, String[].class);
        } catch (IOException e) {
            return new String[]{};
        }
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
        return Boolean.parseBoolean(config.get(NEGATED_CONDITION));
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
