package demo.keycloak.smallrye;

import lombok.extern.jbosslog.JBossLog;
import org.eclipse.microprofile.metrics.Metadata;
import org.eclipse.microprofile.metrics.MetricRegistry;
import org.eclipse.microprofile.metrics.MetricType;
import org.eclipse.microprofile.metrics.Tag;
import org.keycloak.events.Event;
import org.keycloak.events.EventType;
import org.keycloak.events.admin.AdminEvent;
import org.keycloak.events.admin.OperationType;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@JBossLog
public class KeycloakMetrics implements KeycloakMetricRecorder {

    private final static String USER_EVENT_PREFIX = "keycloak_user_event_";

    private final static String ADMIN_EVENT_PREFIX = "keycloak_admin_event_";

    public static final String IDENTITY_PROVIDER = "identity_provider";
    private final static String PROVIDER_KEYCLOAK_OPENID = "keycloak";

    public static final String TAG_REALM = "realm";
    public static final String TAG_CLIENT_ID = "client_id";
    public static final String TAG_PROVIDER = "provider";
    public static final String TAG_ERROR = "error";

    private final Map<String, Metadata> counters;
    private final Metadata totalLogins;
    private final Metadata totalFailedLoginAttempts;
    private final Metadata totalRegistrations;
    private final Metadata totalRegistrationsErrors;

    private final MetricRegistry metricsRegistry;

    public KeycloakMetrics(MetricRegistry metricsRegistry) {

        this.metricsRegistry = metricsRegistry;

        this.totalLogins = Metadata.builder()
                .withName("keycloak_logins")
                .withDescription("Total successful logins")
                .withType(MetricType.COUNTER)
                .build();

        this.totalFailedLoginAttempts = Metadata.builder()
                .withName("keycloak_failed_login_attempts")
                .withDescription("Total failed login attempts")
                .withType(MetricType.COUNTER)
                .build();

        this.totalRegistrations = Metadata.builder()
                .withName("keycloak_registrations")
                .withDescription("Total registered users")
                .withType(MetricType.COUNTER)
                .build();

        this.totalRegistrationsErrors = Metadata.builder()
                .withName("keycloak_registrations_errors")
                .withDescription("Total errors on registrations")
                .withType(MetricType.COUNTER)
                .build();

        this.counters = registerCounters();
    }

    private Map<String, Metadata> registerCounters() {

        Map<String, Metadata> initCounters = new HashMap<>();
        registerUserEventCounters(initCounters);
        registerAdminEventCounters(initCounters);
        return Collections.unmodifiableMap(initCounters);
    }

    /**
     * Counters for all user events
     */
    private void registerUserEventCounters(Map<String, Metadata> counters) {

        for (EventType type : EventType.values()) {
            if (type.equals(EventType.LOGIN) || type.equals(EventType.LOGIN_ERROR) || type.equals(EventType.REGISTER)) {
                continue;
            }
            String counterName = buildCounterName(type);
            counters.put(counterName, createCounter(counterName, false));
        }
    }

    /**
     * Counters for all admin events
     */
    private void registerAdminEventCounters(Map<String, Metadata> counters) {

        for (OperationType type : OperationType.values()) {
            String counterName = buildCounterName(type);
            counters.put(counterName, createCounter(counterName, true));
        }
    }

    /**
     * Creates a counter based on a event name
     */
    private static Metadata createCounter(final String name, boolean isAdmin) {

        String description = isAdmin ? "Generic KeyCloak Admin event" : "Generic KeyCloak User event";

        return Metadata.builder()
                .withName(name)
                .withDescription(description)
                .withType(MetricType.COUNTER).build();
    }

    /**
     * Count generic user event
     *
     * @param keycloakEvent User event
     */
    @Override
    public void recordGenericEvent(KeycloakEvent keycloakEvent) {

        Event event = keycloakEvent.getEvent();

        String counterName = buildCounterName(event.getType());
        if (counters.get(counterName) == null) {
            log.warnf("Counter for event type %s does not exist. Realm: %s", event.getType().name(), keycloakEvent.getRealmName());
            return;
        }

        metricsRegistry.counter(counters.get(counterName), tag("realm", keycloakEvent.getRealmName())).inc();
    }

    /**
     * Count generic admin event
     *
     * @param keycloakAdminEvent Admin event
     */
    @Override
    public void recordGenericAdminEvent(KeycloakAdminEvent keycloakAdminEvent) {

        AdminEvent event = keycloakAdminEvent.getAdminEvent();

        String counterName = buildCounterName(event.getOperationType());
        if (counters.get(counterName) == null) {
            log.warnf("Counter for admin event operation type %s does not exist. Resource type: %s, realm: %s", event.getOperationType().name(), event.getResourceType().name(), keycloakAdminEvent.getRealmName());
            return;
        }

        Tag[] tags = {
                tag("realm", keycloakAdminEvent.getRealmName()),
                tag("resource", event.getResourceType().name())
        };

        metricsRegistry.counter(counters.get(counterName), tags).inc();
    }

    /**
     * Increase the number of currently logged in users
     *
     * @param keycloakEvent Login event
     */
    @Override
    public void recordLogin(KeycloakEvent keycloakEvent) {

        Event event = keycloakEvent.getEvent();
        String provider = getIdentityProvider(event);

        Tag[] tags = {
                tag(TAG_REALM, keycloakEvent.getRealmName()),
                tag(TAG_PROVIDER, provider),
                tag(TAG_CLIENT_ID, event.getClientId())
        };

        metricsRegistry.counter(totalLogins, tags).inc();
    }

    /**
     * Increase the number of failed login attempts
     *
     * @param keycloakEvent LoginError event
     */
    @Override
    public void recordLoginError(KeycloakEvent keycloakEvent) {

        Event event = keycloakEvent.getEvent();
        String provider = getIdentityProvider(event);

        Tag[] tags = {
                tag(TAG_REALM, keycloakEvent.getRealmName()),
                tag(TAG_ERROR, event.getError()),
                tag(TAG_PROVIDER, provider),
                tag(TAG_CLIENT_ID, event.getClientId())
        };

        metricsRegistry.counter(totalFailedLoginAttempts, tags).inc();
    }

    /**
     * Increase the number registered users
     *
     * @param keycloakEvent Register event
     */
    @Override
    public void recordRegistration(KeycloakEvent keycloakEvent) {

        Event event = keycloakEvent.getEvent();
        Tag[] tags = {
                tag(TAG_REALM, keycloakEvent.getRealmName()),
                tag(TAG_CLIENT_ID, event.getClientId())
        };

        metricsRegistry.counter(totalRegistrations, tags).inc();
    }

    /**
     * Increase the number of failed registered users attemps
     *
     * @param keycloakEvent RegisterError event
     */
    @Override
    public void recordRegistrationError(KeycloakEvent keycloakEvent) {

        Event event = keycloakEvent.getEvent();
        String provider = getIdentityProvider(event);

        Tag[] tags = {
                tag(TAG_REALM, keycloakEvent.getRealmName()),
                tag(TAG_PROVIDER, provider),
                tag(TAG_CLIENT_ID, event.getClientId())
        };

        metricsRegistry.counter(totalRegistrationsErrors, tags).inc();
    }

    private String buildCounterName(OperationType type) {
        return ADMIN_EVENT_PREFIX + type.name();
    }

    private String buildCounterName(EventType type) {
        return USER_EVENT_PREFIX + type.name();
    }

    /**
     * Retrieve the identity provider name from event details or
     * default to {@value #PROVIDER_KEYCLOAK_OPENID}.
     *
     * @param event User event
     * @return Identity provider name
     */
    private String getIdentityProvider(Event event) {

        String identityProvider = null;
        if (event.getDetails() != null) {
            identityProvider = event.getDetails().get(IDENTITY_PROVIDER);
        }

        if (identityProvider == null) {
            identityProvider = PROVIDER_KEYCLOAK_OPENID;
        }

        return identityProvider;
    }

    private Tag tag(String name, String value) {
        return new Tag(name, value);
    }
}
