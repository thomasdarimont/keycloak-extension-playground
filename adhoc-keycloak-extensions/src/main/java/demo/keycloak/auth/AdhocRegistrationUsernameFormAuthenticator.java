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
 * that shows a prefilled user registration page if a user with the given username cannot be found.
 *
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

        // show the registration page to the user prefilled with the given email if present
        LoginFormsProvider formProvider = context.form();
        if (context.getRealm().isRegistrationEmailAsUsername()) {
            String maybeCurrentUsername = context.getHttpRequest().getDecodedFormParameters().getFirst(AuthenticationManager.FORM_USERNAME);
            boolean usernameIsValidEmail = maybeCurrentUsername != null && Validation.isEmailValid(maybeCurrentUsername);
            if (usernameIsValidEmail) {
                // prefill the email field in the current registration form with the current username
                MultivaluedHashMap<String, String> newFormData = new MultivaluedHashMap<>();
                newFormData.putSingle("email", maybeCurrentUsername);
                formProvider.setFormData(newFormData);
            }
        }

        // reset previous errors to avoid showing error messages on the registration page
        formProvider.setErrors(Collections.emptyList());

        context.challenge(formProvider.createRegistration());

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

