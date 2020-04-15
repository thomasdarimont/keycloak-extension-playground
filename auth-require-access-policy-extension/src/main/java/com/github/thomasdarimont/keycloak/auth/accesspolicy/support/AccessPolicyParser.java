package com.github.thomasdarimont.keycloak.auth.accesspolicy.support;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.jbosslog.JBossLog;

import java.io.IOException;

@JBossLog
public class AccessPolicyParser {

    private final ObjectMapper OM = new ObjectMapper();


    public AccessPolicy parse(String policyJson) {

        try {
            return OM.readValue(policyJson, AccessPolicy.class);
        } catch (IOException e) {
            log.error("Failed to parse AccessPolicy from JSON", e);
        }

        return AccessPolicy.EMPTY;
    }
}
