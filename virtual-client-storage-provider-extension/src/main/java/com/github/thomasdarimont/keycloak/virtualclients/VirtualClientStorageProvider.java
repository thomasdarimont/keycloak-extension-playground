package com.github.thomasdarimont.keycloak.virtualclients;

import org.keycloak.component.ComponentModel;
import org.keycloak.models.ClientModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.storage.client.ClientStorageProvider;

public class VirtualClientStorageProvider implements ClientStorageProvider {

    private final KeycloakSession session;
    private final ComponentModel componentModel;
    private final VirtualClientModelGenerator virtualClientModelGenerator;

    public VirtualClientStorageProvider(KeycloakSession session, ComponentModel componentModel) {

        this.session = session;
        this.componentModel = componentModel;
        this.virtualClientModelGenerator = new VirtualClientModelGenerator();
    }

    @Override
    public void close() {
        // NOOP
    }

    @Override
    public ClientModel getClientById(String id, RealmModel realm) {

        if (!"virtual-clients".equals(realm.getName())) {
            return null;
        }

        return this.virtualClientModelGenerator.createVirtualModel(id, realm.getName());
    }

    @Override
    public ClientModel getClientByClientId(String clientId, RealmModel realm) {

        if (!"virtual-clients".equals(realm.getName())) {
            return null;
        }

        if (clientId == null) {
            return null;
        }

        if (!clientId.startsWith("f:" + componentModel.getId() + ":")) {
            return null;
        }

        VirtualClientModel virtualModel = this.virtualClientModelGenerator.createVirtualModel(clientId, realm.getName());
        return virtualModel;
    }
}
