package com.github.thomasdarimont.keycloak.backupcodes.action;

import com.github.thomasdarimont.keycloak.backupcodes.BackupCode;
import com.github.thomasdarimont.keycloak.backupcodes.BackupCodeConfig;
import com.github.thomasdarimont.keycloak.backupcodes.BackupCodeCredentialModel;
import com.github.thomasdarimont.keycloak.backupcodes.BackupCodeGenerator;
import lombok.extern.jbosslog.JBossLog;
import org.keycloak.authentication.InitiatedActionSupport;
import org.keycloak.authentication.RequiredActionContext;
import org.keycloak.authentication.RequiredActionProvider;
import org.keycloak.common.util.Time;
import org.keycloak.credential.CredentialModel;
import org.keycloak.forms.login.LoginFormsProvider;
import org.keycloak.forms.login.freemarker.model.RealmBean;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserCredentialManager;
import org.keycloak.models.UserModel;
import org.keycloak.sessions.AuthenticationSessionModel;

import javax.ws.rs.core.Response;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
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
        return createBackupCodesForm(context).createForm("backup-codes.ftl");
    }

    protected LoginFormsProvider createBackupCodesForm(RequiredActionContext context) {
        LoginFormsProvider form = context.form();
        UserModel user = context.getAuthenticationSession().getAuthenticatedUser();
        String username = user.getUsername();
        form.setAttribute("username", username);
        form.setAttribute("backupCodesPresent", backupCodesConfiguredForUser(context, user));
        return form;
    }

    private boolean backupCodesConfiguredForUser(RequiredActionContext context, UserModel user) {

        UserCredentialManager ucm = context.getSession().userCredentialManager();
        return ucm.getStoredCredentialsByTypeStream(context.getRealm(), user, BackupCodeCredentialModel.TYPE)
                .findAny().isPresent();
    }

    protected List<BackupCode> createNewBackupCodes(RealmModel realm, UserModel user, KeycloakSession session) {

        BackupCodeConfig backupCodeConfig = BackupCodeConfig.getConfig(realm);
        UserCredentialManager userCredentialManager = session.userCredentialManager();

        List<BackupCode> backupCodes = new ArrayList<>();
        long now = Time.currentTimeMillis();
        for (int i = 1, count = backupCodeConfig.getBackupCodeCount(); i <= count; i++) {
            String code = BackupCodeGenerator.generateCode(backupCodeConfig.getBackupCodeLength());
            BackupCode backupCode = new BackupCode("" + i, code, now);
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
        context.challenge(createDownloadForm(context, backupCodes).createForm("backup-codes-download.ftl"));
    }

    protected void removeExistingBackupCodes(RealmModel realm, UserModel user, KeycloakSession session) {

        UserCredentialManager userCredentialManager = session.userCredentialManager();
        log.debugf("Removing existing backup codes. realm=%s user=%s", realm.getId(), user.getId());
        List<CredentialModel> credentials = userCredentialManager.getStoredCredentialsByTypeStream(realm, user, BackupCodeCredentialModel.TYPE)
                .collect(Collectors.toList());
        for (CredentialModel credential : credentials) {
            userCredentialManager.removeStoredCredential(realm, user, credential.getId());
        }
        log.debugf("Removed existing backup codes. realm=%s user=%s", realm.getId(), user.getId());
    }

    protected LoginFormsProvider createDownloadForm(RequiredActionContext context, List<BackupCode> backupCodes) {

        AuthenticationSessionModel authSession = context.getAuthenticationSession();
        UserModel user = authSession.getAuthenticatedUser();
        ZonedDateTime createdAt = LocalDateTime.now().atZone(ZoneId.systemDefault());

        LoginFormsProvider form = context.form();
        form.setAttribute("username", user.getUsername());
        form.setAttribute("createdAt", createdAt.toInstant().toEpochMilli());

        Locale locale = context.getSession().getContext().resolveLocale(user);
        String createdAtDate = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM).localizedBy(locale).format(createdAt);
        form.setAttribute("createdAtDate", createdAtDate);

        form.setAttribute("realm", new RealmBean(context.getRealm()));
        form.setAttribute("backupCodes", backupCodes);
        return form;
    }

    @Override
    public void close() {
        // NOOP
    }
}
