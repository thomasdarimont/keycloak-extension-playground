package com.github.thomasdarimont.keycloak.userstorage.flyweight;

import lombok.Data;

@Data
public class AcmeRole {

    private final String id;

    private final String name;

    private final String description;
}
