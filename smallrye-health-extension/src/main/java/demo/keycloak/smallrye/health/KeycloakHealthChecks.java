package demo.keycloak.smallrye.health;

import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.HealthCheckResponseBuilder;
import org.eclipse.microprofile.health.Liveness;
import org.eclipse.microprofile.health.Readiness;
import org.keycloak.common.Version;

import javax.annotation.Resource;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.sql.DataSource;
import java.lang.management.ManagementFactory;
import java.sql.Connection;

@ApplicationScoped
public class KeycloakHealthChecks {

    public static final int DB_CONNECTION_VALID_TIMEOUT_MILLIS = 1000;

    public static final HealthCheckResponseBuilder KEYCLOAK_SERVER_HEALTH_CHECK = HealthCheckResponse.named("keycloak:server")
            .withData("version", Version.VERSION_KEYCLOAK)
            .withData("startupTimestamp", ManagementFactory.getRuntimeMXBean().getStartTime());

    @Resource(name = "KeycloakDS")
    private DataSource keycloakDatasource;

    @Produces
    @Liveness
    HealthCheck serverCheck() {
        return () -> KEYCLOAK_SERVER_HEALTH_CHECK.up().build();
    }

    @Produces
    @Readiness
    HealthCheck databaseCheck() {

        HealthCheckResponseBuilder databaseHealth = HealthCheckResponse.named("keycloak:database");

        boolean databaseReady = isDatabaseReady();

        return () -> (databaseReady ? databaseHealth.up() : databaseHealth.down()).build();
    }

    private boolean isDatabaseReady() {

        try (Connection connection = keycloakDatasource.getConnection()) {
            return connection.isValid(DB_CONNECTION_VALID_TIMEOUT_MILLIS);
        } catch (Exception ex) {
            return false;
        }
    }
}
