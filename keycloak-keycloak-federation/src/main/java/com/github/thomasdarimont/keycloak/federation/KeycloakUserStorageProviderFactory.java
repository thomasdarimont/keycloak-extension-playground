package com.github.thomasdarimont.keycloak.federation;

import com.google.auto.service.AutoService;
import lombok.extern.jbosslog.JBossLog;
import org.keycloak.Config;
import org.keycloak.component.ComponentModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.provider.ProviderConfigurationBuilder;
import org.keycloak.storage.UserStorageProviderFactory;

import java.util.List;

@JBossLog
@AutoService(UserStorageProviderFactory.class)
public class KeycloakUserStorageProviderFactory implements UserStorageProviderFactory<KeycloakUserStorageProvider> {

    @Override
    public void init(Config.Scope config) {

        // this configuration is pulled from the SPI configuration of this provider in the standalone[-ha] / domain.xml
        // see setup.cli

//        String someProperty = config.get("someProperty");
//        log.infov("Configured {0} with someProperty: {1}", this, someProperty);
    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {

    }

    @Override
    public KeycloakUserStorageProvider create(KeycloakSession session, ComponentModel model) {
        // here you can setup the user storage provider, initiate some connections, etc.

//        log.infov("CreateProvider {0}", List.of());

        return new KeycloakUserStorageProvider(session, model);
    }

    @Override
    public String getId() {
        return "keycloak";
    }

    @Override
    public String getHelpText() {
        return "Remote Keycloak User Storage";
    }

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {

        // this configuration is configurable in the admin-console
        List<ProviderConfigProperty> config = ProviderConfigurationBuilder.create()
                .property()
                .name("authServerUrl")
                .label("Auth Server URL")
                .helpText("URL of the Keycloak Auth Server")
                .type(ProviderConfigProperty.STRING_TYPE)
                .defaultValue("http://localhost:8080/auth")
                .add()
                .property()
                .name("clientId")
                .label("Client ID")
                .helpText("Client ID")
                .type(ProviderConfigProperty.STRING_TYPE)
                .defaultValue("admin-cli")
                .add()
                .property()
                .name("clientSecret")
                .label("Client Secret")
                .helpText("Client Secret")
                .type(ProviderConfigProperty.PASSWORD)
                .defaultValue("")
                .add()
                .property()
                .name("username")
                .label("Username")
                .helpText("Username")
                .type(ProviderConfigProperty.STRING_TYPE)
                .defaultValue("")
                .add()
                .property()
                .name("password")
                .label("Password")
                .helpText("Password")
                .type(ProviderConfigProperty.PASSWORD)
                .defaultValue("")
                .add()
                .property()
                .name("realm")
                .label("Realm")
                .helpText("Realm name")
                .type(ProviderConfigProperty.STRING_TYPE)
                .defaultValue("")
                .add()
                // more properties
                // .property()
                // .add()
                .build();

//        config.addAll(getCommonProviderConfigProperties());

        return config;
    }
}

