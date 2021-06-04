package com.github.thomasdarimont.keycloak.trustdevice.auth;

import com.github.thomasdarimont.keycloak.trustdevice.DeviceCookie;
import com.github.thomasdarimont.keycloak.trustdevice.DeviceToken;
import com.github.thomasdarimont.keycloak.trustdevice.model.SimpleTrustedDeviceManager;
import com.github.thomasdarimont.keycloak.trustdevice.model.TrustedDeviceManager;
import com.github.thomasdarimont.keycloak.trustdevice.model.TrustedDeviceModel;
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

    private static final TrustedDeviceManager DEVICE_MANAGER = new SimpleTrustedDeviceManager();

    @Override
    public void authenticate(AuthenticationFlowContext context) {

        TrustedDeviceModel candidate = lookupTrustedDevice(context);
        if (candidate != null) {
            log.info("Found trusted device.");
            context.getEvent().detail("trusted_device", "true");
            context.getEvent().detail("trusted_device_id", candidate.getDeviceId());
            context.success();
        } else {
            log.info("Unknown device detected!");
            context.attempted();
        }
    }

    public static TrustedDeviceModel lookupTrustedDevice(AuthenticationFlowContext context) {

        HttpRequest httpRequest = context.getHttpRequest();
        KeycloakSession session = context.getSession();
        UserModel user = context.getAuthenticationSession().getAuthenticatedUser();
        if (user == null) {
            return null;
        }

        DeviceToken deviceToken = DeviceCookie.parseDeviceTokenFromCookie(httpRequest, session);
        if (deviceToken == null) {
            return null;
        }

        RealmModel realm = context.getRealm();
        return DEVICE_MANAGER.lookupTrustedDevice(session, realm, user, deviceToken.getDeviceId());
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
