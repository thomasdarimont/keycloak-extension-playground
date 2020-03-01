package demo.keycloak.smallrye;

import org.keycloak.events.admin.AdminEvent;

public class KeycloakAdminEvent {

    private final AdminEvent adminEvent;

    private final String realmName;

    public KeycloakAdminEvent(AdminEvent adminEvent, String realmName) {
        this.adminEvent = adminEvent;
        this.realmName = realmName;
    }

    public AdminEvent getAdminEvent() {
        return adminEvent;
    }

    public String getRealmName() {
        return realmName;
    }
}
