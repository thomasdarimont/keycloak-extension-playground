package com.github.thomasdarimont.keycloak.trustdevice.actions;

import com.github.thomasdarimont.keycloak.trustdevice.DeviceCookie;
import com.github.thomasdarimont.keycloak.trustdevice.DeviceToken;
import com.github.thomasdarimont.keycloak.trustdevice.model.jpa.TrustedDeviceRepository;
import lombok.extern.jbosslog.JBossLog;
import org.jboss.resteasy.spi.HttpRequest;
import org.keycloak.authentication.InitiatedActionSupport;
import org.keycloak.authentication.RequiredActionContext;
import org.keycloak.authentication.RequiredActionProvider;
import org.keycloak.common.util.Time;
import org.keycloak.forms.login.LoginFormsProvider;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import ua_parser.Parser;
import ua_parser.UserAgent;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.function.Consumer;

@JBossLog
public class RegisterTrustedDeviceAction implements RequiredActionProvider {

    public static final String ID = "register-trusted-device";

    static final Parser USER_AGENT_PARSER;

    static {
        Parser parser = null;
        try {
            parser = new Parser();
        } catch (IOException e) {
            log.errorf(e, "Could not initialize user_agent parser");
        }
        USER_AGENT_PARSER = parser;
    }

    @Override
    public void evaluateTriggers(RequiredActionContext context) {
        // NOOP
    }

    public InitiatedActionSupport initiatedActionSupport() {
        return InitiatedActionSupport.SUPPORTED;
    }

    @Override
    public void requiredActionChallenge(RequiredActionContext context) {
        context.challenge(createForm(context, null));
    }

    @Override
    public void processAction(RequiredActionContext context) {

        HttpRequest httpRequest = context.getHttpRequest();
        MultivaluedMap<String, String> formParams = httpRequest.getDecodedFormParameters();

        if (formParams.containsKey("remove-other-trusted-devices")) {
            log.info("Remove all trusted device registrations");
            removeTrustedDevices(context);
        }

        if (formParams.containsKey("dont-trust-device")) {
            log.info("Skip trusted device registration");
            DeviceCookie.removeDeviceCookie(context.getSession(), context.getRealm());
        }

        if (formParams.containsKey("trust-device")) {
            log.info("Register trusted device");
            KeycloakSession session = context.getSession();
            DeviceToken deviceToken = createDeviceToken(httpRequest);

            UserModel user = context.getUser();
            RealmModel realm = context.getRealm();

            String deviceName = sanitizeDeviceName(formParams);
            registerTrustedDevice(deviceToken.getDeviceId(), deviceName, session, realm, user);

            int numberOfDaysToTrustDevice = 120; //FIXME make name of days to remember deviceToken configurable
            int maxAge = numberOfDaysToTrustDevice * 24 * 60 * 60;
            String deviceTokenString = session.tokens().encode(deviceToken);
            DeviceCookie.addDeviceCookie(deviceTokenString, maxAge, session, realm);
            log.info("Registered trusted device");
        }

        // remove required action if present
        context.getUser().removeRequiredAction(ID);
        context.success();
    }

    private String sanitizeDeviceName(MultivaluedMap<String, String> params) {

        String deviceName = params.getFirst("device");
        if (deviceName == null || deviceName.isEmpty()) {
            deviceName = "Browser";
        } else if (deviceName.length() > 32) {
            deviceName = deviceName.substring(0, 32);
        }
        return deviceName;
    }

    private void removeTrustedDevices(RequiredActionContext context) {
        KeycloakSession session = context.getSession();

        RealmModel realm = context.getRealm();
        UserModel user = context.getUser();

        TrustedDeviceRepository repo = new TrustedDeviceRepository(session);
        int deleted = repo.deleteTrustedDevicesForUser(realm.getId(), user.getId());
        if (deleted > 0) {
            log.infof("Deleted trusted devices for user. realm=%s userId=%s devices=%s", realm.getId(), user.getId(), deleted);
        }
    }

    private void registerTrustedDevice(String deviceId, String deviceName, KeycloakSession session, RealmModel realm, UserModel user) {
        TrustedDeviceRepository repo = new TrustedDeviceRepository(session);
        repo.registerTrustedDevice(realm.getId(), user.getId(), deviceId, deviceName);
    }

    protected DeviceToken createDeviceToken(HttpRequest httpRequest) {

        // TODO enhance generated device id with information from httpRequest, e.g. browser fingerprint

        // generate a unique but short device id
        String deviceId = BigInteger.valueOf(new SecureRandom().nextLong()).toString(36);
        DeviceToken deviceToken = new DeviceToken();

        deviceToken.iat((long) Time.currentTime());
        deviceToken.setDeviceId(deviceId);
        return deviceToken;
    }

    protected Response createForm(RequiredActionContext context, Consumer<LoginFormsProvider> formCustomizer) {

        LoginFormsProvider form = context.form();
        form.setAttribute("device", generateDeviceName(context));

        if (formCustomizer != null) {
            formCustomizer.accept(form);
        }

        // use form from src/main/resources/theme-resources/templates/
        return form.createForm("register-trusted-device.ftl");
    }

    private String generateDeviceName(RequiredActionContext context) {
        HttpRequest request = context.getHttpRequest();

        String userAgentString = request.getHttpHeaders().getHeaderString(HttpHeaders.USER_AGENT);
        String name = "Browser";

        if (USER_AGENT_PARSER == null) {
            return name;
        }

        // TODO generate a better device name based on the user agent
        UserAgent userAgent = USER_AGENT_PARSER.parseUserAgent(userAgentString);
        return name + " " + userAgent.family;
    }

    @Override
    public void close() {
        // NOOP
    }
}
