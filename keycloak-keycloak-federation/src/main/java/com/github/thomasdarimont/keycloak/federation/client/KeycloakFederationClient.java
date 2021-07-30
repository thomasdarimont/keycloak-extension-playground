package com.github.thomasdarimont.keycloak.federation.client;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Map;

public interface KeycloakFederationClient {

//    @GET
//    @Consumes(MediaType.APPLICATION_JSON)
//    @Path("/realms/{realm}/custom-resources/ping")
    Response createUser(@PathParam("realm") String realm, @HeaderParam("Authorization") String token, String username);

//    @GET
//    @Consumes(MediaType.APPLICATION_JSON)
//    @Path("/realms/{realm}/custom-resources/ping")
    Response updateUser(@PathParam("realm") String realm, @HeaderParam("Authorization") String token, Map<String, Object> keycloakUser);

//    @GET
//    @Consumes(MediaType.APPLICATION_JSON)
//    @Path("/realms/{realm}/custom-resources/ping")
    Response deleteUser(@PathParam("realm") String realm, @QueryParam("userId") String userId, @HeaderParam("Authorization") String token);
}
