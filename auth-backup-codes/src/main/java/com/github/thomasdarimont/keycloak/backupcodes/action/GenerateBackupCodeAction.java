package com.github.thomasdarimont.keycloak.backupcodes.action;

import com.github.thomasdarimont.keycloak.backupcodes.BackupCode;
import com.github.thomasdarimont.keycloak.backupcodes.BackupCodeConfig;
import lombok.extern.jbosslog.JBossLog;
import org.keycloak.authentication.InitiatedActionSupport;
import org.keycloak.authentication.RequiredActionContext;
import org.keycloak.authentication.RequiredActionProvider;
import org.keycloak.common.util.RandomString;
import org.keycloak.common.util.Time;
import org.keycloak.credential.CredentialModel;
import org.keycloak.credential.hash.PasswordHashProvider;
import org.keycloak.forms.login.LoginFormsProvider;
import org.keycloak.forms.login.freemarker.model.RealmBean;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserCredentialManager;
import org.keycloak.models.UserModel;
import org.keycloak.models.credential.PasswordCredentialModel;

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

    private static final BackupCodeConfig BACKUP_CODE_CONFIG = new BackupCodeConfig();

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
        // use form from src/main/resources/theme-resources/templates/
        LoginFormsProvider form = context.form();
        form.setAttribute("username", context.getAuthenticationSession().getAuthenticatedUser().getUsername());
        return form.createForm("backup-codes.ftl");
    }

    protected List<BackupCode> createNewBackupCodes(BackupCodeConfig backupCodeConfig, RealmModel realm, UserModel user, KeycloakSession session) {

        PasswordHashProvider passwordHashProvider = session.getProvider(PasswordHashProvider.class, backupCodeConfig.getHashingProviderId());
        if (passwordHashProvider == null) {
            log.errorf("Cloud not find hashProvider to hash backup codes. realm=%s user=%s providerId=%s",
                    realm.getId(), user.getId(), backupCodeConfig.getHashingProviderId());
            throw new RuntimeException("Cloud not find hashProvider to hash backup codes");
        }
        UserCredentialManager userCredentialManager = session.userCredentialManager();

        List<BackupCode> backupCodes = new ArrayList<>();
        long now = Time.currentTimeMillis();
        for (int i = 1, count = backupCodeConfig.getBackupCodeCount(); i <= count; i++) {
            String codeId = Integer.toString(i);
            String code = RandomString.randomCode(backupCodeConfig.getBackupCodeLength());
            BackupCode backupCode = new BackupCode(codeId, code, now);
            try {
                CredentialModel backupCodeModel = createBackupCodeCredentialModel(backupCodeConfig, passwordHashProvider, backupCode);
                userCredentialManager.createCredential(realm, user, backupCodeModel);
                backupCodes.add(backupCode);
            } catch (Exception ex) {
                log.warnf(ex, "Cloud not create backup code for user. realm=%s user=%s", realm.getId(), user.getId());
            }
        }
        return backupCodes;
    }

    private CredentialModel createBackupCodeCredentialModel(BackupCodeConfig backupCodeConfig, PasswordHashProvider passwordHashProvider, BackupCode backupCode) {

        CredentialModel backupCodeModel = new CredentialModel();
        backupCodeModel.setType(BackupCode.CREDENTIAL_TYPE);
        backupCodeModel.setCreatedDate(backupCode.getCreatedAt());
        backupCodeModel.setUserLabel("Backup-Code: " + backupCode.getId());
        PasswordCredentialModel encodedBackupCode = passwordHashProvider.encodedCredential(backupCode.getCode(), backupCodeConfig.getBackupCodeHashIterations());
        backupCodeModel.setSecretData(encodedBackupCode.getSecretData());
        backupCodeModel.setCredentialData(encodedBackupCode.getCredentialData());

        return backupCodeModel;
    }

    protected void removeExistingBackupCodes(RealmModel realm, UserModel user, KeycloakSession session) {

        UserCredentialManager userCredentialManager = session.userCredentialManager();
        log.debugf("Removing existing backup codes. realm=%s user=%s", realm.getId(), user.getId());
        for (CredentialModel credential : userCredentialManager.getStoredCredentialsByTypeStream(realm, user, BackupCode.CREDENTIAL_TYPE).collect(Collectors.toList())) {
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
    public void processAction(RequiredActionContext context) {

        KeycloakSession session = context.getSession();
        RealmModel realm = context.getRealm();
        UserModel user = context.getUser();

        removeExistingBackupCodes(realm, user, session);

        List<BackupCode> backupCodes = createNewBackupCodes(BACKUP_CODE_CONFIG, realm, user, session);

        // remove required action
        context.getUser().removeRequiredAction(ID);

        context.success();

        // Show backup code download form
        context.challenge(createDownloadForm(context, backupCodes));
    }

    @Override
    public void close() {
        // NOOP
    }
}
