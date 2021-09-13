package com.github.thomasdarimont.keycloak.eventlistener.kafka;

import com.google.auto.service.AutoService;
import lombok.extern.jbosslog.JBossLog;
import org.keycloak.Config.Scope;
import org.keycloak.events.EventListenerProvider;
import org.keycloak.events.EventListenerProviderFactory;
import org.keycloak.events.EventType;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;

import java.time.Duration;
import java.util.Map;
import java.util.Objects;

@JBossLog
@AutoService(EventListenerProviderFactory.class)
public class KafkaEventListenerProviderFactory implements EventListenerProviderFactory {

    private static final String ID = "kafka";

    private static final String[] DEFAULT_EVENT_TYPE_NAMES = {EventType.REGISTER.name()};

    public static final String DEFAULT_USER_EVENTS_TOPIC = "keycloakUserEvents";
    public static final String DEFAULT_ADMIN_EVENTS_TOPIC = "keycloakAdminEvents";

    private volatile KafkaEventListenerProvider instance;

    private String bootstrapServers;
    private String topicEvents;
    private String topicAdminEvents;
    private String clientId;
    private String[] eventTypeNames;
    private Map<String, Object> kafkaProducerConfig;
    private int shutdownTimeoutSeconds;

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public EventListenerProvider create(KeycloakSession session) {
        return createLazily();
    }

    private KafkaEventListenerProvider createLazily() {

        KafkaEventListenerProvider provider = instance;
        if (provider != null) {
            return provider;
        }

        provider = new KafkaEventListenerProvider(bootstrapServers, clientId, topicEvents, eventTypeNames, topicAdminEvents, kafkaProducerConfig);
        provider.init();
        instance = provider;
        return provider;
    }

    @Override
    public void init(Scope config) {
        log.info("Init kafka module ...");
        topicEvents = config.get("topicEvents", DEFAULT_USER_EVENTS_TOPIC);
        clientId = config.get("clientId", "keycloak");
        bootstrapServers = config.get("bootstrapServers", "localhost:29092");
        topicAdminEvents = config.get("topicAdminEvents", DEFAULT_ADMIN_EVENTS_TOPIC);
        shutdownTimeoutSeconds = config.getInt("shutdownTimeoutSeconds", 30);

        Objects.requireNonNull(topicEvents, "topic must not be null.");
        Objects.requireNonNull(clientId, "clientId must not be null.");
        Objects.requireNonNull(bootstrapServers, "bootstrapServers must not be null");

        eventTypeNames = resolveEventTypeNames(config);

        kafkaProducerConfig = KafkaProducerConfig.createConfig(config);
    }

    private String[] resolveEventTypeNames(Scope config) {

        String eventsString = config.get("events");

        String[] typeNames = null;
        if (eventsString != null) {
            typeNames = eventsString.split(",");
        }

        if (typeNames == null || typeNames.length == 0) {
            typeNames = DEFAULT_EVENT_TYPE_NAMES;
        }

        return typeNames;
    }

    @Override
    public void postInit(KeycloakSessionFactory arg0) {
        // ignore
    }

    @Override
    public void close() {
        log.info("Shutting down KafkaProducer");
        try {
            instance.shutdown(Duration.ofSeconds(shutdownTimeoutSeconds));
            log.info("Shutdown KafkaProducer succeeded");
        } catch (Exception ex) {
            log.errorf(ex, "Error during shutdown of KafkaProducer");
        }
    }
}
