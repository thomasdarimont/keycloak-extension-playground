package com.github.thomasdarimont.keycloak.trustdevice.model;

import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;

public interface TrustedDeviceManager {

    TrustedDeviceModel lookupTrustedDevice(KeycloakSession session, RealmModel realm, UserModel user, String deviceId);

    TrustedDeviceModel registerTrustedDevice(KeycloakSession session, RealmModel realm, UserModel user, String deviceId, String deviceName);

    boolean removeAllTrustedDevices(KeycloakSession session, RealmModel realm, UserModel user);

    boolean removeTrustedDevice(KeycloakSession session, RealmModel realm, UserModel user, String deviceId);
}
