package com.github.thomasdarimont.keycloak.motd;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class MotdMessage {

    private final String header;

    private final String details;
}
