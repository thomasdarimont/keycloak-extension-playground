package com.github.thomasdarimont.keycloak.virtualclients;

import com.google.auto.service.AutoService;
import org.keycloak.component.ComponentModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.provider.ProviderConfigurationBuilder;
import org.keycloak.storage.client.ClientStorageProvider;
import org.keycloak.storage.client.ClientStorageProviderFactory;
import org.keycloak.storage.client.ClientStorageProviderModel;

import java.util.List;

@AutoService(ClientStorageProviderFactory.class)
public class VirtualClientStorageProviderFactory implements ClientStorageProviderFactory<ClientStorageProvider> {

    static final String ID = "virtual-client-storage";

    static final String CLIENT_SERVICE_URL_ATTRIBUTE = "client-service-url";

    private static final List<ProviderConfigProperty> CONFIG_PROPERTIES;

    static {
        CONFIG_PROPERTIES = ProviderConfigurationBuilder.create()
                .property().name(CLIENT_SERVICE_URL_ATTRIBUTE)
                .type(ProviderConfigProperty.STRING_TYPE)
                .defaultValue("http://localhost:1234/clients")
                .label("Client Service URL")
                .helpText("URL to fetch the registered clients from.")
                .add()
                .build();
    }

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return CONFIG_PROPERTIES;
    }

    @Override
    public ClientStorageProvider create(KeycloakSession session, ComponentModel config) {
        ClientStorageProviderModel providerModel = createProviderModel(config);
        return new VirtualClientStorageProvider(session, providerModel);
    }

    @Override
    public String getId() {
        return ID;
    }

    private ClientStorageProviderModel createProviderModel(ComponentModel model) {
        return new ClientStorageProviderModel(model);
    }
}
