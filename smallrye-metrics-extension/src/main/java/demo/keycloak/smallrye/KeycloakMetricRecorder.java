package demo.keycloak.smallrye;

public interface KeycloakMetricRecorder {

    void recordGenericEvent(KeycloakEvent keycloakEvent);

    void recordGenericAdminEvent(KeycloakAdminEvent keycloakAdminEvent);

    void recordLogin(KeycloakEvent keycloakEvent);

    void recordLoginError(KeycloakEvent keycloakEvent);

    void recordRegistration(KeycloakEvent keycloakEvent);

    void recordRegistrationError(KeycloakEvent keycloakEvent);

    void recordTokenRefresh(KeycloakEvent keycloakEvent);

    void recordTokenRefreshError(KeycloakEvent keycloakEvent);
}
