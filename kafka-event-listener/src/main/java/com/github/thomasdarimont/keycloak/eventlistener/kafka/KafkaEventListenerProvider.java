package com.github.thomasdarimont.keycloak.eventlistener.kafka;

import lombok.extern.jbosslog.JBossLog;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.keycloak.events.Event;
import org.keycloak.events.EventListenerProvider;
import org.keycloak.events.EventType;
import org.keycloak.events.admin.AdminEvent;
import org.keycloak.util.JsonSerialization;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@JBossLog
public class KafkaEventListenerProvider implements EventListenerProvider {

    private final String bootstrapServers;
    private final String clientId;
    private final String topicEvents;
    private final Map<String, Object> kafkaProducerConfig;

    private final List<EventType> eventTypes;

    private final String topicAdminEvents;

    private Producer<String, String> producer;

    public KafkaEventListenerProvider(
            String bootstrapServers,
            String clientId,
            String topicEvents,
            String[] eventTypeNames,
            String topicAdminEvents,
            Map<String, Object> kafkaProducerConfig) {
        this.bootstrapServers = bootstrapServers;
        this.clientId = clientId;
        this.topicEvents = topicEvents;
        this.kafkaProducerConfig = kafkaProducerConfig;
        this.eventTypes = new ArrayList<>();
        this.topicAdminEvents = topicAdminEvents;

        for (String eventTypeName : eventTypeNames) {
            try {
                this.eventTypes.add(EventType.valueOf(eventTypeName.toUpperCase()));
            } catch (IllegalArgumentException e) {
                log.debugf("Ignoring event eventType=%s. Event does not exist.", eventTypeName);
            }
        }
    }

    public void init() {
        log.infof("Creating KafkaProducer clientId=%s", clientId);
        producer = KafkaProducerFactory.createProducer(clientId, bootstrapServers, kafkaProducerConfig);
        log.infof("Created KafkaProducer clientId=%s", clientId);
    }

    private void sendEvent(String eventId, String eventAsString, String topic, String realmId, String userId, String eventType)
            throws InterruptedException, ExecutionException, TimeoutException {

        if (producer == null) {
            log.warnf("Discarding event due to missing KafkaProduce to topic=%s eventId=%s realmId=%s userId=%s eventType=%s",
                    topicEvents, eventId, realmId, userId, eventType);
            return;
        }

        log.debugf("Sending to topic=%s eventId=%s realmId=%s userId=%s eventType=%s",
                topicEvents, eventId, realmId, userId, eventType);
        ProducerRecord<String, String> record = new ProducerRecord<>(topic, eventAsString);
        Future<RecordMetadata> metaData = producer.send(record);

        // TODO fix synchronous waiting
        RecordMetadata recordMetadata = metaData.get(30, TimeUnit.SECONDS);
        log.debugf("Sent to topic=%s eventId=%s realmId=%s userId=%s eventType=%s",
                topicEvents, eventId, realmId, userId, eventType);
    }

    @Override
    public void onEvent(Event event) {

        if (!eventTypes.contains(event.getType())) {
            return;
        }

        try {
            String eventAsString = JsonSerialization.writeValueAsString(event);
            sendEvent(event.getId(), eventAsString, topicEvents, event.getRealmId(), event.getUserId(), event.getType().name());
        } catch (ExecutionException | TimeoutException | IOException e) {
            log.error(e.getMessage(), e);
        } catch (InterruptedException e) {
            log.error(e.getMessage(), e);
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public void onEvent(AdminEvent event, boolean includeRepresentation) {

        if (topicAdminEvents == null) {
            return;
        }

        try {
            String eventAsString = JsonSerialization.writeValueAsString(event);
            String adminEventDescription = event.getResourceTypeAsString() + ":" + event.getOperationType() + ":" + event.getResourcePath();
            sendEvent(event.getId(), eventAsString, topicAdminEvents, event.getRealmId(), null, adminEventDescription);
        } catch (ExecutionException | TimeoutException | IOException e) {
            log.errorf(e, e.getMessage());
        } catch (InterruptedException e) {
            log.errorf(e, e.getMessage());
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public void close() {
        // ignore
    }

    public void shutdown(Duration duration) {
        producer.close(duration);
    }
}
