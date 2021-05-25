package com.github.thomasdarimont.keycloak.backupcodes;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@RequiredArgsConstructor
@ToString
@Getter
public class BackupCode {

    private final String id;

    private final String code;

    private final long createdAt;

}
