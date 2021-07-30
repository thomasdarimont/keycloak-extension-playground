package com.github.thomasdarimont.keycloak.federation;

import com.github.thomasdarimont.keycloak.federation.client.KeycloakFederationClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;
import org.keycloak.component.ComponentModel;
import org.keycloak.models.GroupModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.storage.UserStorageProvider;
import org.keycloak.storage.user.UserLookupProvider;
import org.keycloak.storage.user.UserQueryProvider;
import org.keycloak.storage.user.UserRegistrationProvider;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.util.List;
import java.util.Map;

public class KeycloakUserStorageProvider
        implements
        UserStorageProvider
        , UserLookupProvider
        , UserQueryProvider
        , UserRegistrationProvider {

    private final KeycloakSession session;
    private final ComponentModel model;

    public KeycloakUserStorageProvider(KeycloakSession session, ComponentModel model) {

        this.session = session;
        this.model = model;
    }

    @Override
    public void close() {
        // NOOP
    }

    @Override
    public UserModel getUserById(String id, RealmModel realm) {

        // call remote keycloak
        // convert keycloak response to UserModel

        return null;
    }

    @Override
    public UserModel getUserByUsername(String username, RealmModel realm) {
        return null;
    }

    @Override
    public UserModel getUserByEmail(String email, RealmModel realm) {
        return null;
    }

    @Override
    public List<UserModel> getUsers(RealmModel realm) {
        return null;
    }

    @Override
    public List<UserModel> getUsers(RealmModel realm, int firstResult, int maxResults) {
        return null;
    }

    @Override
    public List<UserModel> searchForUser(String search, RealmModel realm) {
        return null;
    }

    @Override
    public List<UserModel> searchForUser(String search, RealmModel realm, int firstResult, int maxResults) {
        return null;
    }

    @Override
    public List<UserModel> searchForUser(Map<String, String> params, RealmModel realm) {
        return null;
    }

    @Override
    public List<UserModel> searchForUser(Map<String, String> params, RealmModel realm, int firstResult, int maxResults) {
        return null;
    }

    @Override
    public List<UserModel> getGroupMembers(RealmModel realm, GroupModel group) {
        return null;
    }

    @Override
    public List<UserModel> getGroupMembers(RealmModel realm, GroupModel group, int firstResult, int maxResults) {
        return null;
    }

    @Override
    public List<UserModel> searchForUserByUserAttribute(String attrName, String attrValue, RealmModel realm) {
        return null;
    }

    @Override
    public UserModel addUser(RealmModel realm, String username) {
        String targetAuthServerUrl = "http://localhost:8080/auth";
        String targetRealm = "acme";
        String targetKeycloakToken = "accessToken";
        KeycloakFederationClient client = getResteasyWebTarget(targetAuthServerUrl).proxy(KeycloakFederationClient.class);

        Response response = client.createUser(targetRealm, targetKeycloakToken, username);

        // convert response to UserModel

        return null;
    }

    @Override
    public boolean removeUser(RealmModel realm, UserModel user) {
        return false;
    }

    private static ResteasyWebTarget getResteasyWebTarget(String authServerUrl) {
        ResteasyClient client = new ResteasyClientBuilder().build();
        return client.target(UriBuilder.fromPath(authServerUrl));
    }
}
