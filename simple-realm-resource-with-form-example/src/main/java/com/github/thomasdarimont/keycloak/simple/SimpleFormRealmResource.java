package com.github.thomasdarimont.keycloak.simple;

import org.keycloak.forms.login.LoginFormsProvider;
import org.keycloak.models.KeycloakContext;
import org.keycloak.models.KeycloakSession;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * {@code
 * curl -v http://localhost:8081/auth/realms/demo/simple-forms-resource/custom-form
 * }
 */
public class SimpleFormRealmResource {

    private final KeycloakSession session;

    public SimpleFormRealmResource(KeycloakSession session) {
        this.session = session;
    }

    @GET
    @Path("custom-form")
    @Produces(MediaType.APPLICATION_JSON)
    public Response ping() {

        KeycloakContext context = session.getContext();

        LoginFormsProvider forms = session.getProvider(LoginFormsProvider.class);
        forms.setAttribute("realm_name", context.getRealm().getName());

        return forms.createForm("simple-resource-form.ftl");
    }
}
