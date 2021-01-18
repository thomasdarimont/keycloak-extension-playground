package com.github.thomasdarimont.keycloak.auth.emailcode;

import lombok.extern.jbosslog.JBossLog;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.AuthenticationFlowError;
import org.keycloak.authentication.AuthenticationFlowException;
import org.keycloak.authentication.Authenticator;
import org.keycloak.email.EmailException;
import org.keycloak.email.EmailTemplateProvider;
import org.keycloak.events.Errors;
import org.keycloak.forms.login.LoginFormsProvider;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.utils.FormMessage;
import org.keycloak.services.messages.Messages;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

@JBossLog
public class EmailCodeAuthenticatorForm implements Authenticator {

    static final String ID = "demo-email-code-form";

    public static final String EMAIL_CODE = "emailCode";

    private final KeycloakSession session;

    public EmailCodeAuthenticatorForm(KeycloakSession session) {
        this.session = session;
    }

    @Override
    public void authenticate(AuthenticationFlowContext context) {
        challenge(context, null);
    }

    private void challenge(AuthenticationFlowContext context, FormMessage errorMessage) {

        generateAndSendEmailCode(context);

        LoginFormsProvider form = context.form().setExecution(context.getExecution().getId());
        if (errorMessage != null) {
            form.setErrors(List.of(errorMessage));
        }

        Response response = form.createForm("email-code-form.ftl");

        context.challenge(response);
    }

    private void generateAndSendEmailCode(AuthenticationFlowContext context) {

        if (context.getAuthenticationSession().getAuthNote(EMAIL_CODE) != null) {
            // skip sending email code
            return;
        }

        int emailCode = ThreadLocalRandom.current().nextInt(99999999);

        sendEmailWithCode(context.getRealm(), context.getUser(), String.valueOf(emailCode));

        context.getAuthenticationSession().setAuthNote(EMAIL_CODE, Integer.toString(emailCode));
    }

    @Override
    public void action(AuthenticationFlowContext context) {

        MultivaluedMap<String, String> formData = context.getHttpRequest().getDecodedFormParameters();

        if (formData.containsKey("resend")) {
            resetEmailCode(context);
            challenge(context, null);
            return;
        }

        if (formData.containsKey("cancel")) {
            resetEmailCode(context);
            context.resetFlow();
            return;
        }

        int givenEmailCode = Integer.parseInt(formData.getFirst(EMAIL_CODE));

        boolean valid = validateCode(context, givenEmailCode);

        if (!valid) {
            context.getEvent().error(Errors.INVALID_USER_CREDENTIALS);
            challenge(context, new FormMessage(Messages.INVALID_ACCESS_CODE));
            return;
        }

        resetEmailCode(context);

        context.success();
    }

    private void resetEmailCode(AuthenticationFlowContext context) {
        context.getAuthenticationSession().removeAuthNote(EMAIL_CODE);
    }

    private boolean validateCode(AuthenticationFlowContext context, int givenCode) {

        int emailCode = Integer.parseInt(context.getAuthenticationSession().getAuthNote(EMAIL_CODE));
        return givenCode == emailCode;
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

    private void sendEmailWithCode(RealmModel realm, UserModel user, String code) {

        if (user.getEmail() == null) {
            log.warnf("Could not send access code email due to missing email. realm=%s user=%s", realm.getId(), user.getUsername());
            throw new AuthenticationFlowException(AuthenticationFlowError.INVALID_USER);
        }

        Map<String, Object> mailBodyAttributes = new HashMap<>();
        mailBodyAttributes.put("username", user.getUsername());
        mailBodyAttributes.put("code", code);


        String realmName = realm.getDisplayName() != null ? realm.getDisplayName() : realm.getName();
        List<Object> subjectParams = List.of(realmName);

        try {
            EmailTemplateProvider emailProvider = session.getProvider(EmailTemplateProvider.class);
            emailProvider.setRealm(realm);
            emailProvider.setUser(user);
            // Don't forget to add the welcome-email.ftl (html and text) template to your theme.
            emailProvider.send("emailCodeSubject", subjectParams, "code-email.ftl", mailBodyAttributes);
        } catch (EmailException eex) {
            log.errorf(eex, "Failed to send access code email. realm=%s user=%s", realm.getId(), user.getUsername());
        }
    }
}
