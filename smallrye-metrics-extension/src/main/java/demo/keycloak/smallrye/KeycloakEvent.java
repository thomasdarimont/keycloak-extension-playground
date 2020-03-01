package demo.keycloak.smallrye;

import org.keycloak.events.Event;

public class KeycloakEvent {

    private final Event event;

    private final String realmName;

    public KeycloakEvent(Event event, String realmName) {
        this.event = event;
        this.realmName = realmName;
    }

    public Event getEvent() {
        return event;
    }

    public String getRealmName() {
        return realmName;
    }
}
