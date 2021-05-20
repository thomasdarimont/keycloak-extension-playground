package com.github.thomasdarimont.keycloak.backupcodes.auth;

import com.github.thomasdarimont.keycloak.backupcodes.BackupCode;
import com.github.thomasdarimont.keycloak.backupcodes.action.GenerateBackupCodeAction;
import org.keycloak.authentication.AbstractFormAuthenticator;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.AuthenticationFlowError;
import org.keycloak.events.Errors;
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

public class BackupCodeAuthenticator extends AbstractFormAuthenticator {

    public static final String ID = "auth-backup-code";
    public static final String FIELD_BACKUP_CODE = "backupCode";

    @Override
    public void authenticate(AuthenticationFlowContext context) {
        Response challengeResponse = createLoginForm(context.form());
        context.challenge(challengeResponse);
    }

    @Override
    public void action(AuthenticationFlowContext context) {
        MultivaluedMap<String, String> formData = context.getHttpRequest().getDecodedFormParameters();
        if (formData.containsKey("cancel")) {
            context.cancelLogin();
            return;
        }
        if (!validateForm(context, formData)) {
            return;
        }
        context.success();
    }

    protected boolean validateForm(AuthenticationFlowContext context, MultivaluedMap<String, String> formData) {
        return validatePassword(context, context.getUser(), formData);
    }

    @Override
    public boolean requiresUser() {
        return false;
    }

    @Override
    public boolean configuredFor(KeycloakSession session, RealmModel realm, UserModel user) {

        boolean otpConfigured = session.userCredentialManager().isConfiguredFor(realm, user, OTPCredentialModel.TYPE);
        // only allow checking for backup codes if another MFA is registered
        if (!otpConfigured) {
            return false;
        }

        boolean backupCodesConfigured = session.userCredentialManager().isConfiguredFor(realm, user, BackupCode.CREDENTIAL_TYPE);

        return backupCodesConfigured;
    }

    @Override
    public void setRequiredActions(KeycloakSession session, RealmModel realm, UserModel user) {
        user.addRequiredAction(GenerateBackupCodeAction.ID);
    }

    public boolean validatePassword(AuthenticationFlowContext context, UserModel user, MultivaluedMap<String, String> inputData) {

        String backupCodeInput = inputData.getFirst(FIELD_BACKUP_CODE);
        if (backupCodeInput == null || backupCodeInput.isEmpty()) {
            return badBackupCodeHandler(context, user, true);
        }

        UserCredentialModel backupCode = new UserCredentialModel(null, BackupCode.CREDENTIAL_TYPE, backupCodeInput, false);
        if (!context.getSession().userCredentialManager().isValid(context.getRealm(), user, backupCode)) {
            return badBackupCodeHandler(context, user, false);
        }

        return true;
    }

    protected Response challenge(AuthenticationFlowContext context, String error, String field) {
        LoginFormsProvider form = context.form()
                .setExecution(context.getExecution().getId());
        if (error != null) {
            if (field != null) {
                form.addError(new FormMessage(field, error));
            } else {
                form.setError(error);
            }
        }
        return createLoginForm(form);
    }

    protected Response createLoginForm(LoginFormsProvider form) {
        return form.createForm("login-backup-codes.ftl");
    }

    private boolean badBackupCodeHandler(AuthenticationFlowContext context, UserModel user, boolean emptyBackupCode) {
        context.getEvent().user(user);
        context.getEvent().error(Errors.INVALID_USER_CREDENTIALS);
        Response challengeResponse = challenge(context, Messages.INVALID_USER, FIELD_BACKUP_CODE);
        if (emptyBackupCode) {
            context.forceChallenge(challengeResponse);
        } else {
            context.failureChallenge(AuthenticationFlowError.INVALID_CREDENTIALS, challengeResponse);
        }
        return false;
    }

}
