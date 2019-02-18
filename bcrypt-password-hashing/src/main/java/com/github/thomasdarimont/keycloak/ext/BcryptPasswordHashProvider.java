package com.github.thomasdarimont.keycloak.ext;

import org.keycloak.credential.CredentialModel;
import org.keycloak.credential.hash.PasswordHashProvider;
import org.keycloak.models.PasswordPolicy;
import org.keycloak.models.UserCredentialModel;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.security.SecureRandom;

public class BcryptPasswordHashProvider implements PasswordHashProvider {

    private final String providerId;
    private final int defaultStrength;

    public BcryptPasswordHashProvider(String providerId, int defaultStrength) {
        this.providerId = providerId;
        this.defaultStrength = defaultStrength;
    }

    @Override
    public boolean policyCheck(PasswordPolicy policy, CredentialModel credential) {

        int strength = policy.getHashIterations();
        if (strength == -1) {
            strength = defaultStrength;
        }

        return credential.getHashIterations() == strength && providerId.equals(credential.getAlgorithm());
    }

    @Override
    public void encode(String rawPassword, int iterations, CredentialModel credential) {

        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(iterations, new SecureRandom());
        String encodedPassword = encoder.encode(rawPassword);

        credential.setAlgorithm(providerId);
        credential.setType(UserCredentialModel.PASSWORD);
        credential.setHashIterations(iterations);
        credential.setValue(encodedPassword);
    }

    @Override
    public boolean verify(String rawPassword, CredentialModel credential) {

        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(credential.getHashIterations(), new SecureRandom());
        return encoder.matches(rawPassword, credential.getValue());
    }

    @Override
    public void close() {
        // NOOP
    }
}
