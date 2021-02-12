package com.github.thomasdarimont.keycloak.clientstore;

import com.google.auto.service.AutoService;
import lombok.extern.jbosslog.JBossLog;
import org.keycloak.Config;
import org.keycloak.common.util.MultivaluedHashMap;
import org.keycloak.component.ComponentModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.models.RealmModel;
import org.keycloak.models.utils.KeycloakModelUtils;
import org.keycloak.storage.client.ClientStorageProvider;
import org.keycloak.storage.client.ClientStorageProviderFactory;
import org.keycloak.storage.client.ClientStorageProviderModel;

@JBossLog
@AutoService(ClientStorageProviderFactory.class)
public class ExternalClientStorageProviderFactory implements ClientStorageProviderFactory<ExternalClientStorageProvider> {

    public static final String DEFAULT_CLIENT_SERVICE_URL = "http://localhost:7777/clients";

    private String clientServiceUrl;

    @Override
    public ExternalClientStorageProvider create(KeycloakSession session, ComponentModel config) {
        return new ExternalClientStorageProvider(session, createProviderModel(config));
    }

    @Override
    public void init(Config.Scope config) {

        log.info("init");
        clientServiceUrl = config.get(ExternalClientStorageProvider.CLIENT_SERVICE_URL_KEY, DEFAULT_CLIENT_SERVICE_URL);
    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {
        log.info("postInit");
        KeycloakModelUtils.runJobInTransaction(factory, session -> {
            RealmModel realm = session.realms().getRealm("external-clients");

            if (realm == null) {
                return;
            }

            ComponentModel externalClientStorageModel = new ComponentModel();
            externalClientStorageModel.setName(ExternalClientStorageProvider.ID);
            externalClientStorageModel.setParentId(realm.getId());
            externalClientStorageModel.setProviderId(ExternalClientStorageProvider.ID);
            externalClientStorageModel.setProviderType(ClientStorageProvider.class.getName());

            MultivaluedHashMap<String, String> config = new MultivaluedHashMap<>();
            config.putSingle("enabled", "true");
            config.putSingle(ExternalClientStorageProvider.CLIENT_SERVICE_URL_KEY, clientServiceUrl);
            externalClientStorageModel.setConfig(config);

            realm.addComponentModel(externalClientStorageModel);
        });
    }

    @Override
    public String getId() {
        return ExternalClientStorageProvider.ID;
    }

    private ClientStorageProviderModel createProviderModel(ComponentModel model) {
        return new ClientStorageProviderModel(model);
    }
}