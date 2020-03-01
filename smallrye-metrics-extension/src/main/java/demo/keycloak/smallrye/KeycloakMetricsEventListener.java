package demo.keycloak.smallrye;

import lombok.extern.jbosslog.JBossLog;
import org.keycloak.events.Event;
import org.keycloak.events.EventListenerProvider;
import org.keycloak.events.admin.AdminEvent;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;

@JBossLog
public class KeycloakMetricsEventListener implements EventListenerProvider {

    private final KeycloakSession session;
    private final KeycloakMetricRecorder metricsRecorder;

    public KeycloakMetricsEventListener(KeycloakSession session, KeycloakMetricRecorder metricsRecorder) {
        this.session = session;
        this.metricsRecorder = metricsRecorder;
    }

    @Override
    public void onEvent(Event event) {
        logEventDetails(event);

        KeycloakEvent keycloakEvent = toKeycloakEvent(event);

        switch (event.getType()) {
            case LOGIN:
                metricsRecorder.recordLogin(keycloakEvent);
                break;
            case REGISTER:
                metricsRecorder.recordRegistration(keycloakEvent);
                break;
            case REGISTER_ERROR:
                metricsRecorder.recordRegistrationError(keycloakEvent);
                break;
            case LOGIN_ERROR:
                metricsRecorder.recordLoginError(keycloakEvent);
                break;
            default:
                metricsRecorder.recordGenericEvent(keycloakEvent);
        }
    }

    @Override
    public void onEvent(AdminEvent event, boolean includeRepresentation) {

        logAdminEventDetails(event);
        KeycloakAdminEvent keycloakAdminEvent = toKeycloakEvent(event);

        metricsRecorder.recordGenericAdminEvent(keycloakAdminEvent);
    }


    private KeycloakEvent toKeycloakEvent(Event event) {
        RealmModel realm = session.realms().getRealm(event.getRealmId());
        return new KeycloakEvent(event, realm.getName());
    }

    private KeycloakAdminEvent toKeycloakEvent(AdminEvent event) {
        RealmModel realm = session.realms().getRealm(event.getRealmId());
        return new KeycloakAdminEvent(event, realm.getName());
    }

    private void logEventDetails(Event event) {
        log.tracef("Received user event of type %s in realm %s",
                event.getType().name(),
                event.getRealmId());
    }

    private void logAdminEventDetails(AdminEvent event) {
        log.tracef("Received admin event of type %s (%s) in realm %s",
                event.getOperationType().name(),
                event.getResourceType().name(),
                event.getRealmId());
    }

    @Override
    public void close() {
        // NOOP
    }
}
