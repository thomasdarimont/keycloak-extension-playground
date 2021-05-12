package com.github.thomasdarimont.keycloak.trustdevice.auth;

import com.github.thomasdarimont.keycloak.trustdevice.DeviceCookie;
import com.github.thomasdarimont.keycloak.trustdevice.DeviceToken;
import com.github.thomasdarimont.keycloak.trustdevice.model.jpa.TrustedDeviceEntity;
import com.github.thomasdarimont.keycloak.trustdevice.model.jpa.TrustedDeviceRepository;
import lombok.extern.jbosslog.JBossLog;
import org.jboss.resteasy.spi.HttpRequest;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.Authenticator;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;

@JBossLog
public class TrustedDeviceAuthenticator implements Authenticator {

    public static final String ID = "auth-trust-device";

    @Override
    public void authenticate(AuthenticationFlowContext context) {

        if (isTrustedDevice(context)) {
            log.info("Found trusted device.");
            context.success();
        } else {
            log.info("Unknown device detected!");
            context.attempted();
        }
    }

    public static boolean isTrustedDevice(AuthenticationFlowContext context) {

        HttpRequest httpRequest = context.getHttpRequest();
        KeycloakSession session = context.getSession();
        UserModel user = context.getAuthenticationSession().getAuthenticatedUser();
        if (user == null) {
            return false;
        }

        DeviceToken deviceToken = DeviceCookie.parseDeviceTokenFromCookie(httpRequest, session);
        if (deviceToken == null) {
            return false;
        }

        RealmModel realm = context.getRealm();
        TrustedDeviceRepository repo = new TrustedDeviceRepository(session);
        TrustedDeviceEntity trustedDeviceEntity = repo.lookupTrustedDevice(
                realm.getId(), user.getId(), deviceToken.getDeviceId());

        return trustedDeviceEntity != null;
    }

    @Override
    public void action(AuthenticationFlowContext context) {
        // NOOP as this authenticator is headless
    }

    @Override
    public boolean requiresUser() {
        return true;
    }

    @Override
    public boolean configuredFor(KeycloakSession session, RealmModel realm, UserModel user) {
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
