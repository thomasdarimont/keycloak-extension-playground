package demo.keycloak.smallrye;

import com.google.auto.service.AutoService;
import io.smallrye.metrics.MetricRegistries;
import lombok.extern.jbosslog.JBossLog;
import org.eclipse.microprofile.metrics.MetricRegistry;
import org.keycloak.Config;
import org.keycloak.events.EventListenerProvider;
import org.keycloak.events.EventListenerProviderFactory;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;

@JBossLog
@AutoService(EventListenerProviderFactory.class)
public class KeycloakMetricsEventListenerFactory implements EventListenerProviderFactory {

    private static final String ID = "keycloak-metrics-extension";

    private KeycloakMetrics keycloakMetrics;

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public EventListenerProvider create(KeycloakSession session) {
        return new KeycloakMetricsEventListener(session, keycloakMetrics);
    }

    @Override
    public void init(Config.Scope config) {

        MetricRegistry metricsRegistry = lookupMetricsRegistry();
        this.keycloakMetrics = new KeycloakMetrics(metricsRegistry);
    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {
        // NOOP
    }

    protected MetricRegistry lookupMetricsRegistry() {
        return MetricRegistries.get(MetricRegistry.Type.APPLICATION);
    }

    @Override
    public void close() {
        // NOOP
    }
}
