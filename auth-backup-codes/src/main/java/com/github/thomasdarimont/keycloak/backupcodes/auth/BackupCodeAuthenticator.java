package com.github.thomasdarimont.keycloak.backupcodes.auth;

import com.github.thomasdarimont.keycloak.backupcodes.BackupCodeCredentialModel;
import com.github.thomasdarimont.keycloak.backupcodes.action.GenerateBackupCodeAction;
import org.keycloak.authentication.AbstractFormAuthenticator;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.AuthenticationFlowError;
import org.keycloak.events.Errors;
import org.keycloak.events.EventBuilder;
import org.keycloak.forms.login.LoginFormsProvider;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserCredentialModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.credential.OTPCredentialModel;
import org.keycloak.models.utils.FormMessage;
import org.keycloak.services.messages.Messages;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import static org.keycloak.authentication.authenticators.util.AuthenticatorUtils.getDisabledByBruteForceEventError;
import static org.keycloak.services.validation.Validation.FIELD_USERNAME;

public class BackupCodeAuthenticator extends AbstractFormAuthenticator {

    public static final String ID = "auth-backup-code";

    public static final String FIELD_BACKUP_CODE = "backupCode";

    public static final String MESSAGE_BACKUP_CODE_INVALID = "backup-code-invalid";

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
        // we only allow checking for backup codes if another MFA is registered
        boolean otpConfigured = session.userCredentialManager().isConfiguredFor(realm, user, OTPCredentialModel.TYPE);
        if (!otpConfigured) {
            return false;
        }

        return session.userCredentialManager().isConfiguredFor(realm, user, BackupCodeCredentialModel.TYPE);
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
        if (!context.getSession().userCredentialManager().isValid(context.getRealm(), user, backupCode)) {
            return badBackupCodeHandler(context, user, false);
        }

        return true;
    }

    protected boolean isDisabledByBruteForce(AuthenticationFlowContext context, UserModel user) {
        String bruteForceError = getDisabledByBruteForceEventError(context.getProtector(), context.getSession(), context.getRealm(), user);
        if (bruteForceError != null) {
            context.getEvent().user(user);
            context.getEvent().error(bruteForceError);
            Response challengeResponse = challenge(context, disabledByBruteForceError(), disabledByBruteForceFieldError());
            context.forceChallenge(challengeResponse);
            return true;
        }
        return false;
    }

    protected Response challenge(AuthenticationFlowContext context, String error, String field) {
        return createLoginForm(context, error, field).createForm("login-backup-codes.ftl");
    }

    protected LoginFormsProvider createLoginForm(AuthenticationFlowContext context, String error, String field) {
        LoginFormsProvider form = context.form()
                .setExecution(context.getExecution().getId());
        if (error != null) {
            if (field != null) {
                form.addError(new FormMessage(field, error));
            } else {
                form.setError(error);
            }
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
