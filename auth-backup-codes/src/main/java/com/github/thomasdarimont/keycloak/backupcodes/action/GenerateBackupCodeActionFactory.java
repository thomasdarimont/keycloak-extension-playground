package com.github.thomasdarimont.keycloak.backupcodes.action;

import com.google.auto.service.AutoService;
import org.keycloak.Config;
import org.keycloak.authentication.DisplayTypeRequiredActionFactory;
import org.keycloak.authentication.RequiredActionFactory;
import org.keycloak.authentication.RequiredActionProvider;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;

@AutoService(RequiredActionFactory.class)
public class GenerateBackupCodeActionFactory implements RequiredActionFactory, DisplayTypeRequiredActionFactory {

    public static final GenerateBackupCodeAction INSTANCE = new GenerateBackupCodeAction();

    @Override
    public RequiredActionProvider create(KeycloakSession session) {
        return INSTANCE;
    }

    @Override
    public RequiredActionProvider createDisplay(KeycloakSession session, String displayType) {
        return create(session);
    }

    @Override
    public void init(Config.Scope config) {
        // NOOP
    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {
        // NOOP
    }

    @Override
    public void close() {
        // NOOP
    }

    @Override
    public String getId() {
        return GenerateBackupCodeAction.ID;
    }

    @Override
    public String getDisplayText() {
        return "Generate Backup Codes";
    }

    @Override
    public boolean isOneTimeAction() {
        return true;
    }


}
