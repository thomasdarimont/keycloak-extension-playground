package demo.keycloak.smallrye;
import lombok.extern.jbosslog.JBossLog;
import org.eclipse.microprofile.metrics.Gauge;
import org.eclipse.microprofile.metrics.Metadata;
import org.eclipse.microprofile.metrics.MetricRegistry;
import org.eclipse.microprofile.metrics.MetricType;
import org.eclipse.microprofile.metrics.Tag;
import org.keycloak.common.Version;
import org.keycloak.events.Event;
import org.keycloak.events.EventType;
import org.keycloak.events.admin.AdminEvent;
import org.keycloak.events.admin.OperationType;
import org.keycloak.events.admin.ResourceType;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Keycloak Metrics are exposed via the Wildfly {@code /metrics} endpoint via the management interface.
 * Note, that you need to register the metrics listener in your target realm first, e.g. via admin-console -> events -> config.
 * <p>
 * See: http://localhost:9990/metrics
 */
@JBossLog
public class KeycloakMetrics implements KeycloakMetricRecorder {

    private static final String USER_EVENT_PREFIX = "keycloak_user_event_";

    private static final String ADMIN_EVENT_PREFIX = "keycloak_admin_event_";

    private static final String IDENTITY_PROVIDER = "identity_provider";

    private static final String PROVIDER_KEYCLOAK_OPENID = "keycloak";

    private static final String TAG_REALM = "realm";

    private static final String TAG_CLIENT_ID = "client_id";

    private static final String TAG_PROVIDER = "provider";

    private static final String TAG_ERROR = "error";

    private final Map<String, Metadata> genericCounters;

    private final Metadata totalLogins;

    private final Metadata totalFailedLoginAttempts;

    private final Metadata totalRegistrations;

    private final Metadata totalRegistrationsErrors;

    private final Metadata totalTokenRefreshes;

    private final Metadata totalTokenRefreshErrors;

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

        this.totalTokenRefreshes = Metadata.builder()
                .withName("keycloak_user_event_REFRESH_TOKEN")
                .withDescription("Total successful token refreshes")
                .withType(MetricType.COUNTER)
                .build();

        this.totalTokenRefreshErrors = Metadata.builder()
                .withName("keycloak_user_event_REFRESH_TOKEN_ERROR")
                .withDescription("Total failed token refreshes")
                .withType(MetricType.COUNTER)
                .build();

        registerServerVersionGauge(metricsRegistry);

        this.genericCounters = registerGenericCounters();
    }

    /**
     * Registers a dummy gauge with value 0 that piggy-backs the current Keycloak version as a label.
     * @param metricsRegistry
     */
    private void registerServerVersionGauge(MetricRegistry metricsRegistry) {

        Metadata keycloakServerVersion = Metadata.builder()
                .withName("keycloak_server_version")
                .withDescription("Current Keycloak Server Version")
                .withType(MetricType.GAUGE)
                .build();

        Tag[] tags = {
                tag("version", Version.VERSION),
        };

        metricsRegistry.register(keycloakServerVersion, (Gauge<Double>) () -> 0.0, tags);
    }

    private Map<String, Metadata> registerGenericCounters() {

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
            if (KeycloakMetricsEventListener.CUSTOM_HANDLED_EVENT_TYPES.contains(type)) {
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

        return Metadata.builder().withName(name).withDescription(description).withType(MetricType.COUNTER).build();
    }

    /**
     * Count generic user event
     *
     * @param keycloakEvent User event
     */
    @Override
    public void recordGenericEvent(KeycloakEvent keycloakEvent) {

        Event event = keycloakEvent.getEvent();

        EventType eventType = event.getType();
        String counterName = buildCounterName(eventType);
        Metadata counterMetadata = genericCounters.get(counterName);
        String realmName = keycloakEvent.getRealmName();

        if (counterMetadata == null) {
            log.warnf("Counter %s for event type %s does not exist. Realm: %s", counterName, eventType.name(), realmName);
            return;
        }

        metricsRegistry.counter(counterMetadata, tag("realm", realmName)).inc();
    }

    /**
     * Count generic admin event
     *
     * @param keycloakAdminEvent Admin event
     */
    @Override
    public void recordGenericAdminEvent(KeycloakAdminEvent keycloakAdminEvent) {

        AdminEvent event = keycloakAdminEvent.getAdminEvent();

        OperationType operationType = event.getOperationType();
        String counterName = buildCounterName(operationType);
        Metadata counterMetadata = genericCounters.get(counterName);
        ResourceType resourceType = event.getResourceType();
        String realmName = keycloakAdminEvent.getRealmName();

        if (counterMetadata == null) {
            log.warnf("Counter %s for admin event operation type %s does not exist. Resource type: %s, realm: %s", counterName, operationType.name(), resourceType.name(), realmName);
            return;
        }

        Tag[] tags = {
                tag("realm", realmName),
                tag("resource", resourceType.name())
        };

        metricsRegistry.counter(counterMetadata, tags).inc();
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
                tag(TAG_CLIENT_ID, getClientId(event))
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
                tag(TAG_CLIENT_ID, getClientId(event))
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
                tag(TAG_CLIENT_ID, getClientId(event))
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
                tag(TAG_CLIENT_ID, getClientId(event))
        };

        metricsRegistry.counter(totalRegistrationsErrors, tags).inc();
    }

    @Override
    public void recordTokenRefresh(KeycloakEvent keycloakEvent) {

        Event event = keycloakEvent.getEvent();

        Tag[] tags = {
                tag(TAG_REALM, keycloakEvent.getRealmName()),
                tag(TAG_CLIENT_ID, event.getClientId())
        };

        metricsRegistry.counter(totalTokenRefreshes, tags).inc();
    }

    @Override
    public void recordTokenRefreshError(KeycloakEvent keycloakEvent) {

        Event event = keycloakEvent.getEvent();

        Tag[] tags = {
                tag(TAG_REALM, keycloakEvent.getRealmName()),
                tag(TAG_CLIENT_ID, event.getClientId())
        };

        metricsRegistry.counter(totalTokenRefreshErrors, tags).inc();
    }

    private String buildCounterName(OperationType type) {
        return ADMIN_EVENT_PREFIX + type.name();
    }

    private String buildCounterName(EventType type) {
        return USER_EVENT_PREFIX + type.name();
    }

    /**
     * Retrieve the identity provider name from event details or
     * <p>
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

    private String getClientId(Event event) {
        return coalesce(event.getClientId(), "unknown");
    }

    /**
     * Helper method to return a fallback value if the given value was null.
     * <p>
     * Used to avoid exceptions in case of malformed requests.
     *
     * @param value
     * @param fallback
     * @param <T>
     * @return
     */
    private <T> T coalesce(T value, T fallback) {
        if (value != null) {
            return value;
        }
        return fallback;
    }
}