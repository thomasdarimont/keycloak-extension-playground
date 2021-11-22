package com.github.thomasdarimont.keycloak.federation;

import com.github.thomasdarimont.keycloak.federation.client.KeycloakFacadeProvider;
import com.google.auto.service.AutoService;
import lombok.extern.jbosslog.JBossLog;
import org.keycloak.Config;
import org.keycloak.component.ComponentModel;
import org.keycloak.component.ComponentValidationException;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.models.RealmModel;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.provider.ProviderConfigurationBuilder;
import org.keycloak.storage.UserStorageProviderFactory;
import org.keycloak.utils.StringUtil;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@JBossLog
@AutoService(UserStorageProviderFactory.class)
public class KeycloakUserStorageProviderFactory implements UserStorageProviderFactory<KeycloakUserStorageProvider> {

    private final ConcurrentMap<String, KeycloakFacadeProvider> keycloakApis = new ConcurrentHashMap<>();

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

        return new KeycloakUserStorageProvider(session, model, keycloakApis);
    }

    @Override
    public String getId() {
        return "keycloak-remote";
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
                .property()
                .name("defaultRealmRoles")
                .label("Additional Realm Roles")
                .helpText("Realm roles to add")
                .type(ProviderConfigProperty.STRING_TYPE)
                .defaultValue("")
                .add()
                .property()
                .name("defaultClientRoles")
                .label("Additional Client Roles")
                .helpText("Client roles to add. clientId1=role1,role2;cliendId2=role3,role4")
                .type(ProviderConfigProperty.STRING_TYPE)
                .defaultValue("")
                .add()
                .property()
                .name("externalUserIdAttribute")
                .label("External UserId Attribute")
                .helpText("The name of the user attribute to store the external userId.")
                .type(ProviderConfigProperty.STRING_TYPE)
                .defaultValue("extUserId")
                .add()
                .property()
                .name("useEmailAsUsername")
                .label("Use email as username")
                .helpText("The name of the user attribute to store the external userId.")
                .type(ProviderConfigProperty.BOOLEAN_TYPE)
                .defaultValue("false")
                .add()
                .property()
                .name("importEnabled")
                .label("Import User into Keycloak")
                .helpText("If enabled creates a local user for the given user information.")
                .type(ProviderConfigProperty.BOOLEAN_TYPE)
                .defaultValue("false")
                .add()
                // more properties
                // .property()
                // .add()
                .build();

        return config;
    }

    @Override
    public void validateConfiguration(KeycloakSession session, RealmModel realm, ComponentModel config) throws ComponentValidationException {

        String externalUserIdAttribute = config.get(KeycloakUserStorageProvider.EXTERNAL_USER_ID_ATTRIBUTE);
        if (StringUtil.isBlank(externalUserIdAttribute)) {
            throw new ComponentValidationException("externalUserIdAttribute must not be empty!");
        }
    }
}

