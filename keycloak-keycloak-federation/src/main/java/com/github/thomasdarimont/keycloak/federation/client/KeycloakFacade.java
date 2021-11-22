package com.github.thomasdarimont.keycloak.federation.client;

import org.keycloak.OAuth2Constants;
import org.keycloak.jose.jwk.JSONWebKeySet;
import org.keycloak.representations.AccessTokenResponse;
import org.keycloak.representations.idm.UserRepresentation;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import java.util.List;

public interface KeycloakFacade {

    @GET
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/realms/{realm}/protocol/openid-connect/certs")
    JSONWebKeySet getJwks(@PathParam("realm") String realm);

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/realms/{realm}/protocol/openid-connect/token")
    AccessTokenResponse getToken(@PathParam("realm") String realm, //
                                 @FormParam(OAuth2Constants.CLIENT_ID) String clientId, //
                                 @FormParam(OAuth2Constants.CLIENT_SECRET) String clientSecret, //
                                 @FormParam(OAuth2Constants.SCOPE) String scope, //
                                 @FormParam(OAuth2Constants.GRANT_TYPE) String grantType //
    );


    @POST
    @Produces(MediaType.APPLICATION_FORM_URLENCODED)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/realms/{realm}/protocol/openid-connect/token")
    AccessTokenResponse validatePassword(@PathParam("realm") String realm, //
                                         @FormParam(OAuth2Constants.CLIENT_ID) String clientId, //
                                         @FormParam(OAuth2Constants.CLIENT_SECRET) String clientSecret, //
                                         @FormParam(OAuth2Constants.USERNAME) String username, //
                                         @FormParam(OAuth2Constants.PASSWORD) String password, //
                                         @FormParam(OAuth2Constants.GRANT_TYPE) String grantType, //
                                         @FormParam(OAuth2Constants.SCOPE) String scope //
    );

    @GET
    @Consumes(MediaType.APPLICATION_JSON)
    // /users?briefRepresentation=true&first=0&max=20
    @Path("/admin/realms/{realm}/users")
    List<UserRepresentation> getUserByUsername(@PathParam("realm") String realm, //
                                               @QueryParam("username") String username, //
                                               @QueryParam("briefRepresentation") boolean briefRepresentation //
    );

    @GET
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/admin/realms/{realm}/users")
    List<UserRepresentation> getUserByEmail(@PathParam("realm") String realm, //
                                            @QueryParam("email") String email, //
                                            @QueryParam("briefRepresentation") boolean briefRepresentation //
    );

    @GET
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/admin/realms/{realm}/users/{id}")
    UserRepresentation getUserById(@PathParam("realm") String realm, //
                                   @PathParam("id") String id //
    );
}
