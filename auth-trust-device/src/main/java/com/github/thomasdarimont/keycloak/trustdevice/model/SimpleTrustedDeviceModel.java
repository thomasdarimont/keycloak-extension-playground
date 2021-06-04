package com.github.thomasdarimont.keycloak.trustdevice.model;

import javax.persistence.Column;
import javax.persistence.Id;

public class SimpleTrustedDeviceModel implements TrustedDeviceModel {

    private String userId;

    private String realmId;

    private String deviceId;

    private String deviceName;

    private long createdAt;

    @Override
    public String getUserId() {
        return userId;
    }

    @Override
    public void setUserId(String userId) {
        this.userId = userId;
    }

    @Override
    public String getRealmId() {
        return realmId;
    }

    @Override
    public void setRealmId(String realmId) {
        this.realmId = realmId;
    }

    @Override
    public String getDeviceId() {
        return deviceId;
    }

    @Override
    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    @Override
    public String getDeviceName() {
        return deviceName;
    }

    @Override
    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    @Override
    public long getCreatedAt() {
        return createdAt;
    }

    @Override
    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }
}
