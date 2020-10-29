package com.github.thomasdarimont.keycloak.userstorage.flyweight;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
public class AcmeUser {

    private boolean enabled;
    private String id;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private String password;

    private long createdTimestamp;

    private Map<String, List<String>> attributes;

    public AcmeUser(String id, String username, String password, String firstName, String lastName, Map<String, List<String>> attributes, boolean enabled) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.attributes = attributes;
        this.username = username;
        this.password = password;
        this.email = this.username + "@example.com";
        this.enabled = enabled;
        // TODO pull from database
        this.createdTimestamp = System.currentTimeMillis();
    }

    public List<String> getAttribute(String name) {
        return attributes.getOrDefault(name, Collections.emptyList());
    }
}
