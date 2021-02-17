package demo.keycloak.auth;

import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.authenticators.browser.UsernamePasswordForm;
import org.keycloak.forms.login.LoginFormsProvider;
import org.keycloak.models.Constants;
import org.keycloak.services.Urls;
import org.keycloak.services.messages.Messages;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;

public class DynamicRegistrationUsernameForm extends UsernamePasswordForm {

    @Override
    protected boolean validateForm(AuthenticationFlowContext context, MultivaluedMap<String, String> formData) {
        return validateUser(context, formData);
    }

    @Override
    public void action(AuthenticationFlowContext context) {
        MultivaluedMap<String, String> formData = context.getHttpRequest().getDecodedFormParameters();
        if (formData.containsKey("cancel")) {
            context.cancelLogin();
            return;
        }
        if (!validateForm(context, formData)) {
            context.resetFlow();

            MultivaluedMap<String, String> queryParameters = context.getUriInfo().getQueryParameters();

            UriBuilder registerUriBuilder = UriBuilder.fromUri(Urls.realmRegisterPage(
                    context.getUriInfo().getBaseUri(),
                    context.getRealm().getName()
            ));

            URI registerLink = registerUriBuilder
                    .queryParam(Constants.CLIENT_ID, queryParameters.getFirst(Constants.CLIENT_ID))
                    .queryParam(Constants.TAB_ID, queryParameters.getFirst(Constants.TAB_ID))
                    .build();

            context.challenge(Response.temporaryRedirect(registerLink).build());

            return;
        }
        context.success();
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
        if (context.getRealm().isLoginWithEmailAllowed())
            return Messages.INVALID_USERNAME_OR_EMAIL;
        return Messages.INVALID_USERNAME;
    }
}
