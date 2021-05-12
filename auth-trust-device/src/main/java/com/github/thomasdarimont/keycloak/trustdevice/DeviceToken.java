package com.github.thomasdarimont.keycloak.trustdevice;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.keycloak.TokenCategory;
import org.keycloak.representations.JsonWebToken;

public class DeviceToken extends JsonWebToken {

    @Override
    public TokenCategory getCategory() {
        return TokenCategory.INTERNAL;
    }

    @JsonProperty("device_id")
    private String deviceId;

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }
}