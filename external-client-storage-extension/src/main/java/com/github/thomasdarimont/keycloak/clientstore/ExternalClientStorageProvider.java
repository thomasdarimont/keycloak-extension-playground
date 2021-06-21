package com.github.thomasdarimont.keycloak.clientstore;

import org.keycloak.broker.provider.util.SimpleHttp;
import org.keycloak.component.ComponentModel;
import org.keycloak.models.ClientModel;
import org.keycloak.models.ClientScopeModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.storage.client.ClientStorageProvider;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Stream;

public class ExternalClientStorageProvider implements ClientStorageProvider {

    static final VirtualClientModelGenerator CLIENT_MODEL_GENERATOR = new VirtualClientModelGenerator();

    static final String ID = "external-client-storage";

    static final String CLIENT_SERVICE_URL_KEY = "client-service-url";

    private final KeycloakSession session;
    private final ComponentModel componentModel;
    private final String externalClientIdPrefix;

    public ExternalClientStorageProvider(KeycloakSession session, ComponentModel componentModel) {

        this.session = session;
        this.componentModel = componentModel;
        this.externalClientIdPrefix = "f:" + componentModel.getId() + ":";
    }

    @Override
    public void close() {
        // NOOP
    }

    @Override
    public ClientModel getClientById(RealmModel realm, String id) {

        if (id == null) {
            return null;
        }

        if (!id.startsWith(externalClientIdPrefix)) {
            return null;
        }

        try {
            SimpleHttp.Response response = SimpleHttp.doGet(createUrl("/" + id.substring(externalClientIdPrefix.length())), session).asResponse();
            if (response.getStatus() != 200) {
                return null;
            }
            Map<String, Object> clientData = response.asJson(Map.class);
            clientData.put("id", externalClientIdPrefix + clientData.get("id")); // ensure that we can lookup this client via this provider
            return CLIENT_MODEL_GENERATOR.createVirtualModel(realm, clientData);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // return this.virtualClientModelGenerator.createVirtualModel(id, null, realm);
        return null;
    }

    private String createUrl(String path) {
        String baseUrl = componentModel.getConfig().getFirst(CLIENT_SERVICE_URL_KEY);
        return baseUrl + path;
    }

    @Override
    public ClientModel getClientByClientId(RealmModel realm, String clientId) {
        // Fetch clients from remote location...

        if (clientId == null) {
            return null;
        }

        if (!clientId.startsWith("x:")) {
            return null;
        }

        try {
            SimpleHttp.Response response = SimpleHttp.doGet(createUrl("/search/by-client-id/" + clientId.substring(2)), session).asResponse();
            if (response.getStatus() != 200) {
                return null;
            }
            Map<String, Object> clientData = response.asJson(Map.class);
            clientData.put("id", externalClientIdPrefix + clientData.get("id")); // ensure that we can lookup this client via this provider
            return CLIENT_MODEL_GENERATOR.createVirtualModel(realm, clientData);
        } catch (IOException e) {
            e.printStackTrace();
        }

//        String internalClientId = "f:" + componentModel.getId() + ":";
//        if (!(clientId.startsWith("f:virtual:") || clientId.startsWith(internalClientId))) {
//            return null;
//        }
//
//        String generatedId = internalClientId + clientId.substring(clientId.lastIndexOf(':') + 1);
//
//        // dynamically generate dummy clients for testing...
//        VirtualClientModel virtualModel = this.virtualClientModelGenerator.createVirtualModel(generatedId, generatedId, realm);
//
//        if (virtualModel.isServiceAccountsEnabled()) {
//            UserModel serviceAccount = session.userLocalStorage().getServiceAccount(virtualModel);
//            if (serviceAccount == null) {
//
//                UserModel newServiceAccount = createServiceAccountUser(realm, virtualModel);
//                // TODO find a way to delete the dangling service account users...
////                RoleModel serviceRole = realm.getRole("service");
////                newServiceAccount.grantRole(serviceRole);
//            }
//        }
//
//        return virtualModel;

        return null;
    }

    @Override
    public Stream<ClientModel> searchClientsByClientIdStream(RealmModel realm, String clientId, Integer firstResult, Integer maxResults) {
        // TODO implement search for clients by clientId
        return Stream.empty();
    }

    @Override
    public Stream<ClientModel> searchClientsByAttributes(RealmModel realm, Map<String, String> attributes, Integer firstResult, Integer maxResults) {
        return Stream.empty();
    }

    @Override
    public Map<String, ClientScopeModel> getClientScopes(RealmModel realm, ClientModel client, boolean defaultScopes) {
        // TODO fix dynamic client scope resolution
        return Collections.emptyMap();
    }

    private UserModel createServiceAccountUser(RealmModel realm, ClientModel clientModel) {

        UserModel newServiceAccount = session.userLocalStorage().addUser(realm, "service-account-" + clientModel.getClientId());
        newServiceAccount.setEnabled(true);
        newServiceAccount.setServiceAccountClientLink(clientModel.getId());

        return newServiceAccount;
    }
}
