package com.github.thomasdarimont.keycloak.trustdevice.actions;

import com.google.auto.service.AutoService;
import org.keycloak.Config;
import org.keycloak.authentication.RequiredActionFactory;
import org.keycloak.authentication.RequiredActionProvider;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import ua_parser.Parser;

import java.io.IOException;

@AutoService(RequiredActionFactory.class)
public class RegisterTrustedDeviceActionFactory implements RequiredActionFactory {

    private static final RegisterTrustedDeviceAction INSTANCE = new RegisterTrustedDeviceAction();

    @Override
    public String getDisplayText() {
        return "Register Trusted Device";
    }

    @Override
    public RequiredActionProvider create(KeycloakSession session) {
        return INSTANCE;
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
        return RegisterTrustedDeviceAction.ID;
    }
}
