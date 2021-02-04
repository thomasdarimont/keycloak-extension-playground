package com.github.thomasdarimont.keycloak.actions;

import com.google.auto.service.AutoService;
import org.keycloak.Config;
import org.keycloak.authentication.DisplayTypeRequiredActionFactory;
import org.keycloak.authentication.RequiredActionFactory;
import org.keycloak.authentication.RequiredActionProvider;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;

@AutoService(RequiredActionFactory.class)
public class CustomRequiredActionFactory implements RequiredActionFactory, DisplayTypeRequiredActionFactory {

    @Override
    public RequiredActionProvider create(KeycloakSession session) {
        return new CustomRequiredAction(session);
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
        return CustomRequiredAction.ID;
    }

    @Override
    public String getDisplayText() {
        return "Update custom info";
    }

    @Override
    public boolean isOneTimeAction() {
        return true;
    }
}
