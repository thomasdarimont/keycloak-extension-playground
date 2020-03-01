package com.github.thomasdarimont.keycloak.server;

import com.github.thomasdarimont.keycloak.server.smallrye.MetricsServlet;
import io.undertow.servlet.Servlets;
import io.undertow.servlet.api.DeploymentInfo;
import org.keycloak.common.Version;
import org.keycloak.testsuite.KeycloakServer;

import java.text.SimpleDateFormat;
import java.util.Date;

public class KeycloakPlaygroundServer {

    public static void main(String[] args) throws Throwable {

        System.out.println("Starting KeycloakPlaygroundServer");
        Version.BUILD_TIME = new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date());

        KeycloakServer keycloakServer = KeycloakServer.bootstrapKeycloakServer(args);

        deployMetricsServlet(keycloakServer);
    }

    private static void deployMetricsServlet(KeycloakServer keycloakServer) {

        DeploymentInfo di = Servlets.deployment()
                .setClassLoader(KeycloakServer.class.getClassLoader())
                .setContextPath("/")
                .setDeploymentName("Metrics")
                .addServlets(Servlets.servlet("smallrye-metrics-servlet", MetricsServlet.class)
                        .addMapping("/metrics"));

        keycloakServer.getServer().deploy(di);
    }
}
