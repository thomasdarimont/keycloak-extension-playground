package com.github.thomasdarimont.keycloak.login;


import com.google.auto.service.AutoService;
import org.keycloak.Config;
import org.keycloak.forms.login.LoginFormsProvider;
import org.keycloak.forms.login.LoginFormsProviderFactory;
import org.keycloak.forms.login.freemarker.FreeMarkerLoginFormsProviderFactory;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.theme.FreeMarkerUtil;

@AutoService(LoginFormsProviderFactory.class)
public class CustomLoginFormsProviderFactory extends FreeMarkerLoginFormsProviderFactory {

    private FreeMarkerUtil freeMarker;

    @Override
    public String getId() {
        return "acme-custom-login-forms";
    }

    @Override
    public LoginFormsProvider create(KeycloakSession session) {
        return new CustomLoginFormsProvider(session, freeMarker);
    }

    @Override
    public void init(Config.Scope config) {
        freeMarker = new FreeMarkerUtil();
    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {
        // NOOP
    }

    @Override
    public void close() {
        freeMarker = null;
    }
}

