package com.github.thomasdarimont.keycloak.global;

import lombok.extern.jbosslog.JBossLog;
import org.keycloak.common.util.Resteasy;
import org.keycloak.models.KeycloakContext;
import org.keycloak.models.KeycloakSession;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import java.io.IOException;

@JBossLog
public class GlobalRequestResponseFilter implements ContainerRequestFilter, ContainerResponseFilter {

    protected static final GlobalRequestResponseFilter INSTANCE = new GlobalRequestResponseFilter();

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        log.infof("Before request: request=%s", requestContext);

        KeycloakSession keycloakSession = Resteasy.getContextData(KeycloakSession.class);
        KeycloakContext context = keycloakSession.getContext();


//        String traceId = requestContext.getHeaderString("X-TraceID");
//        MDC.put("traceId", traceId);

    }

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {
        log.infof("After request: request=%s response=%s", requestContext, responseContext);
//        MDC.remove("traceId");
    }
}
