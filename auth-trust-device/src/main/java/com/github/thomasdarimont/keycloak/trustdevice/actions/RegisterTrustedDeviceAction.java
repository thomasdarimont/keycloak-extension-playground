package com.github.thomasdarimont.keycloak.trustdevice.actions;

import com.github.thomasdarimont.keycloak.trustdevice.DeviceCookie;
import com.github.thomasdarimont.keycloak.trustdevice.DeviceToken;
import com.github.thomasdarimont.keycloak.trustdevice.model.SimpleTrustedDeviceManager;
import com.github.thomasdarimont.keycloak.trustdevice.model.TrustedDeviceManager;
import com.github.thomasdarimont.keycloak.trustdevice.support.UserAgentParser;
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
import org.owasp.html.HtmlPolicyBuilder;
import org.owasp.html.PolicyFactory;
import ua_parser.UserAgent;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.function.Consumer;

@JBossLog
public class RegisterTrustedDeviceAction implements RequiredActionProvider {

    public static final String ID = "register-trusted-device";

    private static final PolicyFactory TEXT_ONLY_SANITIZATION_POLICY = new HtmlPolicyBuilder().toFactory();

    private static final TrustedDeviceManager DEVICE_MANAGER = new SimpleTrustedDeviceManager();

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

            String deviceName = sanitizeDeviceName(formParams.getFirst("device"));
            DEVICE_MANAGER.registerTrustedDevice(session, realm, user, deviceToken.getDeviceId(), deviceName);

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

    private String sanitizeDeviceName(String deviceNameInput) {

        String deviceName = deviceNameInput;

        if (deviceName == null || deviceName.isEmpty()) {
            deviceName = "Browser";
        } else if (deviceName.length() > 32) {
            deviceName = deviceName.substring(0, 32);
        }

        deviceName = TEXT_ONLY_SANITIZATION_POLICY.sanitize(deviceName);
        deviceName = deviceName.trim();

        return deviceName;
    }

    private void removeTrustedDevices(RequiredActionContext context) {
        KeycloakSession session = context.getSession();

        RealmModel realm = context.getRealm();
        UserModel user = context.getUser();

        DEVICE_MANAGER.removeAllTrustedDevices(session, realm, user);
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

        // TODO generate a better device name based on the user agent
        UserAgent userAgent = UserAgentParser.parseUserAgent(userAgentString);
        if (userAgent == null) {
            return name;
        }
        return name + " " + userAgent.family;
    }

    @Override
    public void close() {
        // NOOP
    }
}
