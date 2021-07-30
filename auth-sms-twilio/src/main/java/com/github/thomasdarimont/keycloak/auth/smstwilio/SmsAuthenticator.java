package com.github.thomasdarimont.keycloak.auth.smstwilio;

import lombok.extern.jbosslog.JBossLog;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.Authenticator;
import org.keycloak.models.AuthenticatorConfigModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.utils.FormMessage;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

@JBossLog
public class SmsAuthenticator implements Authenticator {

    public static final String PROVIDER_ID = "sms-authenticator-with-twilio";

    private static final String ATTR_PHONE_NUMBER = "phoneNumber";

    static final String CONFIG_SMS_API_KEY = "api-key";
    static final String CONFIG_CODE_LENGTH = "code-length";

    public void authenticate(AuthenticationFlowContext context) {

        log.debug("Begin authentication");

        UserModel user = context.getUser();
        String phoneNumber = extractPhoneNumber(user);

        if (phoneNumber == null) {
            Response challenge = context.form().addError(new FormMessage("missingTelNumberMessage"))
                    .createForm("sms-validation-error.ftl");
            context.challenge(challenge);
            return;
        }

        TwilioApi twilioApi = getTwilioApi(context.getAuthenticatorConfig(), context.getSession());

        if (!twilioApi.sendSms(phoneNumber)) {
            Response challenge = context.form().addError(new FormMessage("sendSMSCodeErrorMessage"))
                    .createForm("sms-validation-error.ftl");
            context.challenge(challenge);
            return;
        }

        Response challenge = context.form().createForm("sms-validation.ftl");
        context.challenge(challenge);
    }

    private TwilioApi getTwilioApi(AuthenticatorConfigModel config, KeycloakSession session) {
        return new TwilioApi(session, getConfigString(config, CONFIG_SMS_API_KEY), getConfigString(config, CONFIG_CODE_LENGTH));
    }

    public void action(AuthenticationFlowContext context) {

        log.debug("Process user input");
        MultivaluedMap<String, String> inputData = context.getHttpRequest().getDecodedFormParameters();
        String enteredCode = inputData.getFirst("smsCode");

        UserModel user = context.getUser();
        String phoneNumber = extractPhoneNumber(user);
        log.debugv("phoneNumber : {0}", phoneNumber);

        TwilioApi twilioApi = getTwilioApi(context.getAuthenticatorConfig(), context.getSession());

        if (!twilioApi.verifySmsCode(phoneNumber, enteredCode)) {
            Response challenge = context.form()
                    .setAttribute("username", user.getUsername())
                    .addError(new FormMessage("invalidSMSCodeMessage")).createForm("sms-validation-error.ftl");
            context.challenge(challenge);
            return;
        }

        log.info("verify code check : OK");
        context.success();
    }

    public boolean requiresUser() {
        return true;
    }

    public boolean configuredFor(KeycloakSession session, RealmModel realm, UserModel user) {
        return extractPhoneNumber(user) != null;
    }

    public void setRequiredActions(KeycloakSession session, RealmModel realm, UserModel user) {
        // NOOP
    }

    public void close() {
        // NOOP
    }

    protected String extractPhoneNumber(UserModel user) {
        return user.getFirstAttribute(ATTR_PHONE_NUMBER);
    }

    private String getConfigString(AuthenticatorConfigModel config, String configName) {
        String value = null;
        if (config.getConfig() != null) {
            // Get value
            value = config.getConfig().get(configName);
        }
        return value;
    }
}