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

        //        String authNote = context.getAuthenticationSession().getAuthNote("trust-device");
//
//        if ("on".equals(authNote)) {
//            context.getUser().addRequiredAction(ID);
//        }
    }

    public InitiatedActionSupport initiatedActionSupport() {
        return InitiatedActionSupport.SUPPORTED;
    }

    @Override
    public void requiredActionChallenge(RequiredActionContext context) {
        // Show form
        context.challenge(createForm(context, null));
    }

    @Override
    public void processAction(RequiredActionContext context) {

        MultivaluedMap<String, String> params = context.getHttpRequest().getDecodedFormParameters();

        if (params.containsKey("remove-all-devices")) {
            log.info("Remove all trusted device registrations");
            removeTrustedDevices(context);
            DeviceCookie.removeDeviceCookie(context.getSession(), context.getRealm());

            context.getUser().removeRequiredAction(ID);
            context.success();
            return;
        }

        if (params.containsKey("dont-trust-device")) {
            log.info("Skip trusted device registration");

            DeviceCookie.removeDeviceCookie(context.getSession(), context.getRealm());

            context.getUser().removeRequiredAction(ID);
            context.success();
            return;
        }

        if (params.containsKey("trust-device")) {

            KeycloakSession session = context.getSession();
            DeviceToken deviceToken = createDeviceToken();

            UserModel user = context.getUser();
            RealmModel realm = context.getRealm();

            String deviceName = sanitizeDeviceName(params);
            registerTrustedDevice(deviceToken.getDeviceId(), deviceName, session, realm, user);
            String deviceTokenString = session.tokens().encode(deviceToken);

            DeviceCookie.addDeviceCookie(deviceTokenString, session, realm);
            log.info("Registered Trusted device");
        }

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

    private DeviceToken createDeviceToken() {

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
        // TODO generate a better device name based on the user agent

        if (USER_AGENT_PARSER == null) {
            return name;
        }

        UserAgent userAgent = USER_AGENT_PARSER.parseUserAgent(userAgentString);
        return name + " " + userAgent.family;
    }

    @Override
    public void close() {
        // NOOP
    }
}
