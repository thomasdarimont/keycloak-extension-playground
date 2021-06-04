package com.github.thomasdarimont.keycloak.trustdevice.model;

public interface TrustedDeviceModel {
    String getUserId();

    void setUserId(String userId);

    String getRealmId();

    void setRealmId(String realmId);

    String getDeviceId();

    void setDeviceId(String deviceId);

    String getDeviceName();

    void setDeviceName(String deviceName);

    long getCreatedAt();

    void setCreatedAt(long createdAt);
}
