package com.github.thomasdarimont.keycloak.backupcodes.action;

import com.github.thomasdarimont.keycloak.backupcodes.BackupCode;
import com.github.thomasdarimont.keycloak.backupcodes.BackupCodeConfig;
import com.github.thomasdarimont.keycloak.backupcodes.BackupCodeCredentialModel;
import lombok.extern.jbosslog.JBossLog;
import org.keycloak.authentication.InitiatedActionSupport;
import org.keycloak.authentication.RequiredActionContext;
import org.keycloak.authentication.RequiredActionProvider;
import org.keycloak.common.util.RandomString;
import org.keycloak.common.util.Time;
import org.keycloak.credential.CredentialModel;
import org.keycloak.forms.login.LoginFormsProvider;
import org.keycloak.forms.login.freemarker.model.RealmBean;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserCredentialManager;
import org.keycloak.models.UserModel;

import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <pre>
 * keycloak.login({action: "generate-backup-codes"});
 * </pre>
 */
@JBossLog
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
        context.challenge(createGenerateBackupCodesForm(context));
    }

    protected Response createGenerateBackupCodesForm(RequiredActionContext context) {

        LoginFormsProvider form = context.form();
        String username = context.getAuthenticationSession().getAuthenticatedUser().getUsername();
        form.setAttribute("username", username);
        return form.createForm("backup-codes.ftl");
    }

    protected List<BackupCode> createNewBackupCodes(RealmModel realm, UserModel user, KeycloakSession session) {

        BackupCodeConfig backupCodeConfig = BackupCodeConfig.getConfig(realm);
        UserCredentialManager userCredentialManager = session.userCredentialManager();

        List<BackupCode> backupCodes = new ArrayList<>();
        long now = Time.currentTimeMillis();
        for (int i = 1, count = backupCodeConfig.getBackupCodeCount(); i <= count; i++) {
            String codeId = Integer.toString(i);
            String code = RandomString.randomCode(backupCodeConfig.getBackupCodeLength());
            BackupCode backupCode = new BackupCode(codeId, code, now);
            try {
                // create and store new backup-code credential model
                userCredentialManager.createCredentialThroughProvider(realm, user, new BackupCodeCredentialModel(backupCode));
                backupCodes.add(backupCode);
            } catch (Exception ex) {
                log.warnf(ex, "Cloud not create backup code for user. realm=%s user=%s", realm.getId(), user.getId());
            }
        }
        return backupCodes;
    }

    @Override
    public void processAction(RequiredActionContext context) {

        KeycloakSession session = context.getSession();
        RealmModel realm = context.getRealm();
        UserModel user = context.getUser();

        removeExistingBackupCodes(realm, user, session);

        List<BackupCode> backupCodes = createNewBackupCodes(realm, user, session);

        // remove required action
        context.getUser().removeRequiredAction(ID);

        context.success();

        // Show backup code download form
        context.challenge(createDownloadForm(context, backupCodes));
    }

    protected void removeExistingBackupCodes(RealmModel realm, UserModel user, KeycloakSession session) {

        UserCredentialManager userCredentialManager = session.userCredentialManager();
        log.debugf("Removing existing backup codes. realm=%s user=%s", realm.getId(), user.getId());
        for (CredentialModel credential : userCredentialManager.getStoredCredentialsByTypeStream(realm, user, BackupCodeCredentialModel.TYPE).collect(Collectors.toList())) {
            userCredentialManager.removeStoredCredential(realm, user, credential.getId());
        }
        log.debugf("Removed existing backup codes. realm=%s user=%s", realm.getId(), user.getId());
    }

    protected Response createDownloadForm(RequiredActionContext context, List<BackupCode> backupCodes) {

        // use form from src/main/resources/theme-resources/templates/
        LoginFormsProvider form = context.form();
        form.setAttribute("username", context.getAuthenticationSession().getAuthenticatedUser().getUsername());
        form.setAttribute("createdAt", Time.currentTimeMillis());
        form.setAttribute("realm", new RealmBean(context.getRealm()));
        form.setAttribute("backupCodes", backupCodes);
        return form.createForm("backup-codes-download.ftl");
    }

    @Override
    public void close() {
        // NOOP
    }
}
