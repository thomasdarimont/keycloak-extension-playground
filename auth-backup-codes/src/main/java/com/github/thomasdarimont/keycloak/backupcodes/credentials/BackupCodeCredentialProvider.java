package com.github.thomasdarimont.keycloak.backupcodes.credentials;

import com.github.thomasdarimont.keycloak.backupcodes.BackupCode;
import com.github.thomasdarimont.keycloak.backupcodes.action.GenerateBackupCodeAction;
import org.keycloak.credential.CredentialInput;
import org.keycloak.credential.CredentialInputValidator;
import org.keycloak.credential.CredentialModel;
import org.keycloak.credential.CredentialProvider;
import org.keycloak.credential.CredentialTypeMetadata;
import org.keycloak.credential.CredentialTypeMetadata.CredentialTypeMetadataBuilder;
import org.keycloak.credential.CredentialTypeMetadataContext;
import org.keycloak.credential.hash.PasswordHashProvider;
import org.keycloak.credential.hash.Pbkdf2PasswordHashProviderFactory;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserCredentialManager;
import org.keycloak.models.UserModel;
import org.keycloak.models.credential.PasswordCredentialModel;

import java.util.List;
import java.util.stream.Collectors;

public class BackupCodeCredentialProvider implements CredentialProvider<CredentialModel>, CredentialInputValidator {

    private final KeycloakSession session;

    public BackupCodeCredentialProvider(KeycloakSession session) {
        this.session = session;
    }

    @Override
    public boolean supportsCredentialType(String credentialType) {
        return BackupCode.CREDENTIAL_TYPE.equals(credentialType);
    }

    @Override
    public boolean isConfiguredFor(RealmModel realm, UserModel user, String credentialType) {
        // TODO check if user actually has credentials
        return true;
    }

    @Override
    public boolean isValid(RealmModel realm, UserModel user, CredentialInput credentialInput) {

        // TODO validate code input
        String codeInput = credentialInput.getChallengeResponse();

        UserCredentialManager userCredentialManager = session.userCredentialManager();
        List<CredentialModel> backupCodes = userCredentialManager.getStoredCredentialsByTypeStream(realm, user, BackupCode.CREDENTIAL_TYPE).collect(Collectors.toList());
        PasswordHashProvider passwordHashProvider = session.getProvider(PasswordHashProvider.class, Pbkdf2PasswordHashProviderFactory.ID);

        for (CredentialModel backupCode : backupCodes) {
            if (passwordHashProvider.verify(codeInput, PasswordCredentialModel.createFromCredentialModel(backupCode))) {
                // we found matching backup code

                // delete backup code entry
                userCredentialManager.removeStoredCredential(realm, user, backupCode.getId());

                return true;
            }
        }

        // no matching backup code found
        return false;
    }

    @Override
    public String getType() {
        return BackupCode.CREDENTIAL_TYPE;
    }

    @Override
    public CredentialModel createCredential(RealmModel realm, UserModel user, CredentialModel credentialModel) {
        // NOT supported
        return null;
    }

    @Override
    public boolean deleteCredential(RealmModel realm, UserModel user, String credentialId) {

        UserCredentialManager userCredentialManager = session.userCredentialManager();
        return userCredentialManager.removeStoredCredential(realm, user, credentialId);
    }

    @Override
    public CredentialModel getCredentialFromModel(CredentialModel model) {
        // NOOP unused
        return null;
    }

    @Override
    public CredentialTypeMetadata getCredentialTypeMetadata(CredentialTypeMetadataContext metadataContext) {

        CredentialTypeMetadataBuilder builder = CredentialTypeMetadata.builder();
        builder.type(BackupCode.CREDENTIAL_TYPE);
        builder.category(CredentialTypeMetadata.Category.TWO_FACTOR);
        builder.createAction(GenerateBackupCodeAction.ID);
        builder.removeable(true);
        builder.displayName("Backup Codes");
        builder.helpText("Generate Backup Codes");
        // builder.updateAction(GenerateBackupCodeAction.ID);
        builder.iconCssClass("backupCodes");
        CredentialTypeMetadata credentialTypeMetadata = builder.build(session);

        return credentialTypeMetadata;
    }
}
