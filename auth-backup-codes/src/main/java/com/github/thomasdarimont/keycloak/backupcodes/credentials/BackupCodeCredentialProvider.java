package com.github.thomasdarimont.keycloak.backupcodes.credentials;

import com.github.thomasdarimont.keycloak.backupcodes.BackupCode;
import com.github.thomasdarimont.keycloak.backupcodes.BackupCodeConfig;
import com.github.thomasdarimont.keycloak.backupcodes.BackupCodeCredentialModel;
import com.github.thomasdarimont.keycloak.backupcodes.action.GenerateBackupCodeAction;
import lombok.extern.jbosslog.JBossLog;
import org.keycloak.credential.CredentialInput;
import org.keycloak.credential.CredentialInputValidator;
import org.keycloak.credential.CredentialModel;
import org.keycloak.credential.CredentialProvider;
import org.keycloak.credential.CredentialTypeMetadata;
import org.keycloak.credential.CredentialTypeMetadata.CredentialTypeMetadataBuilder;
import org.keycloak.credential.CredentialTypeMetadataContext;
import org.keycloak.credential.hash.PasswordHashProvider;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserCredentialManager;
import org.keycloak.models.UserModel;
import org.keycloak.models.credential.PasswordCredentialModel;

import java.util.List;
import java.util.stream.Collectors;

@JBossLog
public class BackupCodeCredentialProvider implements CredentialProvider<CredentialModel>, CredentialInputValidator {

    private final KeycloakSession session;

    public BackupCodeCredentialProvider(KeycloakSession session) {
        this.session = session;
    }

    @Override
    public boolean supportsCredentialType(String credentialType) {
        return BackupCodeCredentialModel.TYPE.equals(credentialType);
    }

    @Override
    public boolean isConfiguredFor(RealmModel realm, UserModel user, String credentialType) {
        UserCredentialManager userCredentialManager = session.userCredentialManager();
        return userCredentialManager.getStoredCredentialsByTypeStream(realm, user, BackupCodeCredentialModel.TYPE)
                .findAny().isPresent();
    }

    @Override
    public boolean isValid(RealmModel realm, UserModel user, CredentialInput credentialInput) {

        String codeInput = credentialInput.getChallengeResponse();

        BackupCodeConfig backupCodeConfig = BackupCodeConfig.getConfig(realm);
        PasswordHashProvider passwordHashProvider = session.getProvider(PasswordHashProvider.class,
                backupCodeConfig.getHashingProviderId());

        UserCredentialManager ucm = session.userCredentialManager();
        List<CredentialModel> backupCodes = ucm.getStoredCredentialsByTypeStream(realm, user, BackupCodeCredentialModel.TYPE)
                .collect(Collectors.toList());

        for (CredentialModel backupCode : backupCodes) {

            // check if the given backup code matches
            if (passwordHashProvider.verify(codeInput, PasswordCredentialModel.createFromCredentialModel(backupCode))) {
                // we found matching backup code

                // delete backup code entry
                ucm.removeStoredCredential(realm, user, backupCode.getId());

                return true;
            }
        }

        // no matching backup code found
        return false;
    }

    @Override
    public String getType() {
        return BackupCodeCredentialModel.TYPE;
    }

    @Override
    public CredentialModel createCredential(RealmModel realm, UserModel user, CredentialModel credentialModelInput) {

        if (!(credentialModelInput instanceof BackupCodeCredentialModel)) {
            return null;
        }

        BackupCodeConfig backupCodeConfig = BackupCodeConfig.getConfig(realm);

        BackupCodeCredentialModel backupCodeCredentialModel = (BackupCodeCredentialModel) credentialModelInput;
        BackupCode backupCode = backupCodeCredentialModel.getBackupCode();

        PasswordHashProvider passwordHashProvider = session.getProvider(PasswordHashProvider.class,
                backupCodeConfig.getHashingProviderId());
        if (passwordHashProvider == null) {
            log.errorf("Could not find hashProvider to hash backup codes. realm=%s user=%s providerId=%s",
                    realm.getId(), user.getId(), backupCodeConfig.getHashingProviderId());
            throw new RuntimeException("Cloud not find hashProvider to hash backup codes");
        }

        CredentialModel backupCodeModel = new CredentialModel();
        backupCodeModel.setType(BackupCodeCredentialModel.TYPE);
        backupCodeModel.setCreatedDate(backupCode.getCreatedAt());
        // TODO make userlabel configurable
        backupCodeModel.setUserLabel("Backup-Code: " + backupCode.getId());
        PasswordCredentialModel encodedBackupCode = passwordHashProvider.encodedCredential(backupCode.getCode(),
                backupCodeConfig.getBackupCodeHashIterations());
        backupCodeModel.setSecretData(encodedBackupCode.getSecretData());
        backupCodeModel.setCredentialData(encodedBackupCode.getCredentialData());

        session.userCredentialManager().createCredential(realm, user, backupCodeModel);

        return backupCodeModel;
    }

    @Override
    public boolean deleteCredential(RealmModel realm, UserModel user, String credentialId) {

        UserCredentialManager userCredentialManager = session.userCredentialManager();
        return userCredentialManager.removeStoredCredential(realm, user, credentialId);
    }

    @Override
    public CredentialModel getCredentialFromModel(CredentialModel model) {

        if (!BackupCodeCredentialModel.TYPE.equals(model.getType())) {
            return null;
        }

        return model;
    }

    @Override
    public CredentialTypeMetadata getCredentialTypeMetadata(CredentialTypeMetadataContext metadataContext) {

        CredentialTypeMetadataBuilder builder = CredentialTypeMetadata.builder();
        builder.type(BackupCodeCredentialModel.TYPE);
        builder.category(CredentialTypeMetadata.Category.TWO_FACTOR);
        builder.createAction(GenerateBackupCodeAction.ID);
        builder.removeable(false);
        builder.displayName("backup-codes-display-name");
        builder.helpText("backup-codes-help-text");
        // builder.updateAction(GenerateBackupCodeAction.ID);
        builder.iconCssClass("kcAuthenticatorBackupCodeClass");

        return builder.build(session);
    }
}
