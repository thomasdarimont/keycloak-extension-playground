package demo.keycloak.auth;

import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.authenticators.browser.UsernamePasswordForm;
import org.keycloak.forms.login.LoginFormsProvider;
import org.keycloak.services.managers.AuthenticationManager;
import org.keycloak.services.messages.Messages;
import org.keycloak.services.validation.Validation;

import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import java.util.Collections;

/**
 * Variant of {@link org.keycloak.authentication.authenticators.browser.UsernameForm} authenticator,
 * that shows the prepoulated user registration page if a user with the given username cannot be found.
 *
 * See: https://security.stackexchange.com/questions/88815/new-gmail-login-system-going-against-conventional-wisdom/88844
 */
public class AdhocRegistrationUsernameFormAuthenticator extends UsernamePasswordForm {

    @Override
    protected boolean validateForm(AuthenticationFlowContext context, MultivaluedMap<String, String> formData) {
        boolean userValid = validateUser(context, formData);

        if (userValid) {
            return userValid;
        }

        LoginFormsProvider formProvider = context.form();
        String maybeCurrentUsername = context.getHttpRequest().getDecodedFormParameters().getFirst(AuthenticationManager.FORM_USERNAME);
        if (maybeCurrentUsername != null && Validation.isEmailValid(maybeCurrentUsername)) {

            MultivaluedHashMap<String, String> newFormData = new MultivaluedHashMap<>();
            newFormData.putSingle("email", maybeCurrentUsername);
            formProvider.setFormData(newFormData);

            formProvider.setErrors(Collections.emptyList());
        }

        context.challenge(formProvider.createRegistration());

        return userValid;
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

