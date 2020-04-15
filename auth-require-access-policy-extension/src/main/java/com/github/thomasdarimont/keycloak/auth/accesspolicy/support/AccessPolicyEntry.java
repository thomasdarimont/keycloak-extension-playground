package com.github.thomasdarimont.keycloak.auth.accesspolicy.support;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
class AccessPolicyEntry {

    @JsonProperty("app")
    private String app;

    @JsonProperty("allow")
    private List<String> allowedRoles;

    public boolean isApplicableTo(String clientId) {

        if (app.equals(clientId)) {
            return true;
        }

        return clientId.matches(app);
    }
}
