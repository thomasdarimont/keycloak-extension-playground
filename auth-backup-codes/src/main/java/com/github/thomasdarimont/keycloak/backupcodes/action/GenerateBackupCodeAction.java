package com.github.thomasdarimont.keycloak.backupcodes.action;

import com.github.thomasdarimont.keycloak.backupcodes.BackupCode;
import org.keycloak.authentication.InitiatedActionSupport;
import org.keycloak.authentication.RequiredActionContext;
import org.keycloak.authentication.RequiredActionProvider;
import org.keycloak.common.util.RandomString;
import org.keycloak.common.util.Time;
import org.keycloak.credential.CredentialModel;
import org.keycloak.credential.hash.PasswordHashProvider;
import org.keycloak.credential.hash.Pbkdf2PasswordHashProviderFactory;
import org.keycloak.forms.login.LoginFormsProvider;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserCredentialManager;
import org.keycloak.models.UserModel;
import org.keycloak.models.credential.PasswordCredentialModel;
import org.keycloak.util.JsonSerialization;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <pre>
 * keycloak.login({action: "generate-backup-codes"});
 * </pre>
 */
public class GenerateBackupCodeAction implements RequiredActionProvider {

    public static final String ID = "generate-backup-codes";

    @Override
    public InitiatedActionSupport initiatedActionSupport() {
        return InitiatedActionSupport.SUPPORTED;
    }

    @Override
    public void evaluateTriggers(RequiredActionContext context) {
        // NOOP
    }

    @Override
    public void requiredActionChallenge(RequiredActionContext context) {

        int backupCodeCount = 10;
        int backupCodeLength = 8;
        int backupCodeHashIterations = 100;

        UserCredentialManager userCredentialManager = context.getSession().userCredentialManager();
        RealmModel realm = context.getRealm();
        UserModel user = context.getUser();
        // TODO remove existing backup codes
        for (CredentialModel credential : userCredentialManager.getStoredCredentialsByTypeStream(realm, user, BackupCode.CREDENTIAL_TYPE).collect(Collectors.toList())) {
            userCredentialManager.removeStoredCredential(realm, user, credential.getId());
        }

        PasswordHashProvider passwordHashProvider = context.getSession().getProvider(PasswordHashProvider.class, Pbkdf2PasswordHashProviderFactory.ID);

        List<BackupCode> backupCodes = new ArrayList<>();
        // TODO generate codes
        for (int i = 1; i <= backupCodeCount; i++) {
            String codeId = Integer.toString(i);
            String code = RandomString.randomCode(backupCodeLength);

            BackupCode backupCode = new BackupCode(codeId, code);
            long now = Time.currentTimeMillis();

            try {
                PasswordCredentialModel backupCodePassword = passwordHashProvider.encodedCredential(code, backupCodeHashIterations);
                backupCodePassword.getPasswordCredentialData().getAdditionalParameters().putSingle("codeId", codeId);
                backupCodePassword.setType(BackupCode.CREDENTIAL_TYPE);
                fillCredentialModelFields(backupCodePassword);
                backupCodePassword.setCreatedDate(now);
                backupCodePassword.setUserLabel("Backup-Code: " + codeId);

                CredentialModel unusedCredential = userCredentialManager.createCredential(realm, user, backupCodePassword);
                backupCodes.add(backupCode);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        // remove required action
        context.getUser().removeRequiredAction(ID);

        // Show backup code download form
        context.challenge(createDownloadForm(context, backupCodes));
    }

    private void fillCredentialModelFields(PasswordCredentialModel credentialModel) {
        try {
            credentialModel.setCredentialData(JsonSerialization.writeValueAsString(credentialModel.getPasswordCredentialData()));
            credentialModel.setSecretData(JsonSerialization.writeValueAsString(credentialModel.getPasswordSecretData()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected Response createDownloadForm(RequiredActionContext context, List<BackupCode> backupCodes) {

        context.success();

        // use form from src/main/resources/theme-resources/templates/
        LoginFormsProvider form = context.form();
        form.setAttribute("backupCodes", backupCodes);
        form.setMediaType(MediaType.TEXT_PLAIN_TYPE);
        return form.createForm("backup-codes.ftl");
    }

    @Override
    public void processAction(RequiredActionContext context) {
        context.success();
    }

    @Override
    public void close() {
        // NOOP
    }
}
