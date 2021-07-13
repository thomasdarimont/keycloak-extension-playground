package demo.keycloak.auth;

import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.authenticators.browser.UsernamePasswordForm;
import org.keycloak.forms.login.LoginFormsProvider;
import org.keycloak.models.Constants;
import org.keycloak.services.Urls;
import org.keycloak.services.messages.Messages;
import org.keycloak.sessions.AuthenticationSessionModel;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

/**
 * Variant of {@link org.keycloak.authentication.authenticators.browser.UsernameForm} authenticator,
 * that shows the user registration page if a user with the given username cannot be found.
 * <p>
 * See: https://security.stackexchange.com/questions/88815/new-gmail-login-system-going-against-conventional-wisdom/88844
 */
public class AdhocRegistrationUsernameFormAuthenticator extends UsernamePasswordForm {

    @Override
    protected boolean validateForm(AuthenticationFlowContext context, MultivaluedMap<String, String> formData) {

        boolean userIsValid = validateUser(context, formData);

        if (userIsValid) {
            return userIsValid;
        }

        // we could not find the user with the given username

        if (!context.getRealm().isRegistrationAllowed()) {
            // only show the registration screen for unknown users if self registration is allowed
            return userIsValid;
        }

        // redirect user to registration page
        AuthenticationSessionModel authSession = context.getAuthenticationSession();
        UriBuilder registerPageUri = UriBuilder.fromUri(Urls.realmRegisterPage(context.getUriInfo().getBaseUri(), context.getRealm().getName()));
        registerPageUri.queryParam(Constants.CLIENT_ID, authSession.getClient().getClientId());
        registerPageUri.queryParam(Constants.TAB_ID, authSession.getTabId());

        context.challenge(Response.temporaryRedirect(registerPageUri.build()).build());

        return userIsValid;
    }

    @Override
    protected Response challenge(AuthenticationFlowContext context, MultivaluedMap<String, String> formData) {
        LoginFormsProvider forms = context.form();

        if (!formData.isEmpty()) {
            forms.setFormData(formData);
        }

        return forms.createLoginUsername();
    }

    @Override
    protected Response createLoginForm(LoginFormsProvider form) {
        return form.createLoginUsername();
    }

    @Override
    protected String getDefaultChallengeMessage(AuthenticationFlowContext context) {
        if (context.getRealm().isLoginWithEmailAllowed()) {
            return Messages.INVALID_USERNAME_OR_EMAIL;
        }
        return Messages.INVALID_USERNAME;
    }
}

