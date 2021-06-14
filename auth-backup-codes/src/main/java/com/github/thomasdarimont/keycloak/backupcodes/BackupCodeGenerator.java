package com.github.thomasdarimont.keycloak.backupcodes;

import org.keycloak.common.util.RandomString;

public class BackupCodeGenerator {

    // TODO consider using a method like Base32 to reduce potential for typos when a code is entered manually

    public static String generateCode(int length) {
        return RandomString.randomCode(length);
    }
}
