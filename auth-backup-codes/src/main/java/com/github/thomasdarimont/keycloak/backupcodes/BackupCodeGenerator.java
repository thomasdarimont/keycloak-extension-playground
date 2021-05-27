package com.github.thomasdarimont.keycloak.backupcodes;

import org.keycloak.common.util.RandomString;

public class BackupCodeGenerator {

    public static String generateCode(int length) {
        return RandomString.randomCode(length);
    }
}
