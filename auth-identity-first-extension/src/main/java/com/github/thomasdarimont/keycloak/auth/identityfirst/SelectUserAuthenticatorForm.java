package com.github.thomasdarimont.keycloak.auth.identityfirst;

import org.jboss.logging.Logger;
import org.jboss.resteasy.specimpl.MultivaluedMapImpl;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.authenticators.browser.AbstractUsernameFormAuthenticator;
import org.keycloak.events.Details;
import org.keycloak.forms.login.LoginFormsProvider;
import org.keycloak.forms.login.freemarker.model.LoginBean;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.protocol.oidc.OIDCLoginProtocol;
import org.keycloak.services.managers.AuthenticationManager;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

public class SelectUserAuthenticatorForm extends AbstractIdentityFirstAbstractUsernameFormAuthenticator {

    private static final Logger LOG = Logger.getLogger(SelectUserAuthenticatorForm.class);
    private final KeycloakSession session;

    public SelectUserAuthenticatorForm(KeycloakSession session) {
        this.session = session;
    }


    @Override
    public void authenticate(AuthenticationFlowContext context) {

        Response response = challenge(context, null);

        context.challenge(response);
    }

    @Override
    protected Response challenge(AuthenticationFlowContext context, String error) {
        return createSelectUserForm(context, error)
                .createForm("select-user-form.ftl");
    }

    private LoginFormsProvider createSelectUserForm(AuthenticationFlowContext context, String error) {

        MultivaluedMap<String, String> formData = createLoginFormData(context);

        LoginFormsProvider form = context.form();
        if (formData.size() > 0) {
            form.setFormData(formData);
        }
        form.setAttribute("login", new LoginBean(formData));

        if (error != null) {
            form.setError(error);
        }

        return form;
    }

    private MultivaluedMap<String, String> createLoginFormData(AuthenticationFlowContext context) {

        MultivaluedMap<String, String> formData = new MultivaluedMapImpl<>();
        String loginHint = context.getAuthenticationSession().getClientNote(OIDCLoginProtocol.LOGIN_HINT_PARAM);
        String rememberMeUsername = AuthenticationManager.getRememberMeUsername(context.getRealm(), context.getHttpRequest().getHttpHeaders());

        if (loginHint != null || rememberMeUsername != null) {
            if (loginHint != null) {
                formData.add(AuthenticationManager.FORM_USERNAME, loginHint);
            } else {
                formData.add(AuthenticationManager.FORM_USERNAME, rememberMeUsername);
                formData.add("rememberMe", "on");
            }
        }
        return formData;
    }

    @Override
    public void action(AuthenticationFlowContext context) {
        MultivaluedMap<String, String> formData = context.getHttpRequest().getDecodedFormParameters();
        if (formData.containsKey("cancel")) {
            context.cancelLogin();
            return;
        }
        if (!validateUsernameForm(context, formData)) {
            return;
        }
        context.success();
    }

    private boolean validateUsernameForm(AuthenticationFlowContext context, MultivaluedMap<String, String> inputData) {

        String username = inputData.getFirst(AuthenticationManager.FORM_USERNAME);
        if (username == null) {
            failWithUserNotFound(context);
            return false;
        }

        // remove leading and trailing whitespace
        username = username.trim();

        context.getEvent().detail(Details.USERNAME, username);
        context.getAuthenticationSession().setAuthNote(AbstractUsernameFormAuthenticator.ATTEMPTED_USERNAME, username);

        UserModel user = lookupUser(context, username);

        if (invalidUser(context, user)) {
            return false;
        }

        if (!enabledUser(context, user)) {
            return false;
        }

        String rememberMe = inputData.getFirst("rememberMe");
        boolean remember = rememberMe != null && rememberMe.equalsIgnoreCase("on");
        if (remember) {
            context.getAuthenticationSession().setAuthNote(Details.REMEMBER_ME, "true");
            context.getEvent().detail(Details.REMEMBER_ME, "true");
        } else {
            context.getAuthenticationSession().removeAuthNote(Details.REMEMBER_ME);
        }
        context.setUser(user);
        return true;
    }

    @Override
    public boolean requiresUser() {
        return false;
    }

    @Override
    public boolean configuredFor(KeycloakSession session, RealmModel realm, UserModel user) {
        return true;
    }

    @Override
    public void setRequiredActions(KeycloakSession session, RealmModel realm, UserModel user) {
        // NOOP
    }
}


