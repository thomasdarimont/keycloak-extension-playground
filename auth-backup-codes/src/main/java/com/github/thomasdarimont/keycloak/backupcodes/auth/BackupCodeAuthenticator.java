package com.github.thomasdarimont.keycloak.backupcodes.auth;

import com.github.thomasdarimont.keycloak.backupcodes.BackupCodeCredentialModel;
import com.github.thomasdarimont.keycloak.backupcodes.action.GenerateBackupCodeAction;
import org.keycloak.authentication.AbstractFormAuthenticator;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.AuthenticationFlowError;
import org.keycloak.events.Errors;
import org.keycloak.events.EventBuilder;
import org.keycloak.forms.login.LoginFormsProvider;
import org.keycloak.models.AuthenticatorConfigModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserCredentialModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.credential.OTPCredentialModel;
import org.keycloak.models.utils.FormMessage;
import org.keycloak.services.messages.Messages;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import java.util.Map;

import static org.keycloak.authentication.authenticators.util.AuthenticatorUtils.getDisabledByBruteForceEventError;
import static org.keycloak.services.validation.Validation.FIELD_USERNAME;

public class BackupCodeAuthenticator extends AbstractFormAuthenticator {

    public static final String ID = "auth-backup-code";

    public static final String FIELD_BACKUP_CODE = "backupCode";

    public static final String MESSAGE_BACKUP_CODE_INVALID = "backup-code-invalid";

    public static final String CONFIG_RENEW_BACKUP_CODES_ON_EXHAUSTION = "renew-backup-codes-on-exhaustion";

    public static final String DEFAULT_RENEW_BACKUP_CODES_ON_EXHAUSTION = "true";

    @Override
    public void authenticate(AuthenticationFlowContext context) {
        Response challengeResponse = context.form().createForm("login-backup-codes.ftl");
        context.challenge(challengeResponse);
    }

    @Override
    public void action(AuthenticationFlowContext context) {
        MultivaluedMap<String, String> formData = context.getHttpRequest().getDecodedFormParameters();
        if (formData.containsKey("cancel")) {
            context.cancelLogin();
            return;
        }
        if (validateBackupCode(context, context.getUser(), formData)) {
            context.success();
        }
    }

    @Override
    public boolean requiresUser() {
        return true;
    }

    @Override
    public boolean configuredFor(KeycloakSession session, RealmModel realm, UserModel user) {

        // TODO revise handling of backup code auth prompt -> should we always ask for backup codes if present and no other 2FA is configured?
        if (isSecondFactorRequired(session, realm, user) && !isSecondFactorConfigured(session, realm, user)) {
            // we only allow checking for backup codes if another MFA is registered
            return false;
        }

        return session.userCredentialManager().isConfiguredFor(realm, user, BackupCodeCredentialModel.TYPE);
    }

    protected boolean isSecondFactorRequired(KeycloakSession session, RealmModel realm, UserModel user) {
        return true;
    }

    protected boolean isSecondFactorConfigured(KeycloakSession session, RealmModel realm, UserModel user) {
        return session.userCredentialManager().isConfiguredFor(realm, user, OTPCredentialModel.TYPE);
    }

    @Override
    public void setRequiredActions(KeycloakSession session, RealmModel realm, UserModel user) {
        user.addRequiredAction(GenerateBackupCodeAction.ID);
    }

    public boolean validateBackupCode(AuthenticationFlowContext context, UserModel user, MultivaluedMap<String, String> inputData) {

        String backupCodeInput = inputData.getFirst(FIELD_BACKUP_CODE);
        if (backupCodeInput == null || backupCodeInput.isEmpty()) {
            return badBackupCodeHandler(context, user, true);
        }

        // note backup_code usage in event
        context.getEvent().detail("backup_code", "true");

        if (isDisabledByBruteForce(context, user)) {
            return false;
        }

        UserCredentialModel backupCode = new UserCredentialModel(null, BackupCodeCredentialModel.TYPE, backupCodeInput, false);
        KeycloakSession session = context.getSession();
        RealmModel realm = context.getRealm();

        boolean backupCodeValid = session.userCredentialManager().isValid(realm, user, backupCode);
        if (!backupCodeValid) {
            return badBackupCodeHandler(context, user, false);
        }

        checkForRemainingBackupCodes(context, session, realm, user);

        return true;
    }

    protected void checkForRemainingBackupCodes(AuthenticationFlowContext context, KeycloakSession session, RealmModel realm, UserModel user) {

        // check if there are remaining backup-codes left, otherwise add required action to user
        boolean remainingBackupCodesPresent = session.userCredentialManager().isConfiguredFor(realm, user, BackupCodeCredentialModel.TYPE);
        if (remainingBackupCodesPresent) {
            return;
        }

        boolean renewBackupCodesOnExhaustion =
                Boolean.parseBoolean(getConfig(context, CONFIG_RENEW_BACKUP_CODES_ON_EXHAUSTION, DEFAULT_RENEW_BACKUP_CODES_ON_EXHAUSTION));
        if (renewBackupCodesOnExhaustion) {
            user.addRequiredAction(GenerateBackupCodeAction.ID);
        }
    }

    protected String getConfig(AuthenticationFlowContext context, String key, String defaultValue) {

        AuthenticatorConfigModel configModel = context.getAuthenticatorConfig();
        if (configModel == null) {
            return defaultValue;
        }

        Map<String, String> config = configModel.getConfig();
        if (config == null) {
            return defaultValue;
        }

        return config.getOrDefault(key, defaultValue);
    }

    protected boolean isDisabledByBruteForce(AuthenticationFlowContext context, UserModel user) {

        String bruteForceError = getDisabledByBruteForceEventError(context.getProtector(), context.getSession(), context.getRealm(), user);
        if (bruteForceError == null) {
            return false;
        }

        context.getEvent().user(user);
        context.getEvent().error(bruteForceError);
        Response challengeResponse = challenge(context, disabledByBruteForceError(), disabledByBruteForceFieldError());
        context.forceChallenge(challengeResponse);
        return true;
    }

    protected Response challenge(AuthenticationFlowContext context, String error, String field) {
        return createLoginForm(context, error, field).createForm("login-backup-codes.ftl");
    }

    protected LoginFormsProvider createLoginForm(AuthenticationFlowContext context, String error, String field) {

        LoginFormsProvider form = context.form()
                .setExecution(context.getExecution().getId());
        if (error == null) {
            return form;
        }

        if (field != null) {
            form.addError(new FormMessage(field, error));
        } else {
            form.setError(error);
        }
        return form;
    }

    protected boolean badBackupCodeHandler(AuthenticationFlowContext context, UserModel user, boolean emptyBackupCode) {

        EventBuilder event = context.getEvent();

        event.user(user);
        event.error(Errors.INVALID_USER_CREDENTIALS);

        Response challengeResponse = challenge(context, MESSAGE_BACKUP_CODE_INVALID, FIELD_BACKUP_CODE);
        if (emptyBackupCode) {
            context.forceChallenge(challengeResponse);
        } else {
            context.failureChallenge(AuthenticationFlowError.INVALID_CREDENTIALS, challengeResponse);
        }
        return false;
    }

    protected String disabledByBruteForceError() {
        return Messages.INVALID_USER;
    }

    protected String disabledByBruteForceFieldError() {
        return FIELD_USERNAME;
    }

}
