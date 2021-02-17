package demo.keycloak.auth;

import com.google.auto.service.AutoService;
import org.jboss.resteasy.spi.HttpRequest;
import org.keycloak.authentication.FormActionFactory;
import org.keycloak.authentication.FormContext;
import org.keycloak.authentication.ValidationContext;
import org.keycloak.authentication.authenticators.browser.AbstractUsernameFormAuthenticator;
import org.keycloak.authentication.forms.RegistrationUserCreation;
import org.keycloak.forms.login.LoginFormsProvider;
import org.keycloak.models.Constants;
import org.keycloak.services.Urls;
import org.keycloak.services.validation.Validation;

import javax.ws.rs.RedirectionException;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

@AutoService(FormActionFactory.class)
public class DynamicRegistrationUserCreation extends RegistrationUserCreation {

    public static final String PROVIDER_ID = "dynreg-user-creation";

    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    @Override
    public String getDisplayType() {
        return "Dynamic Registration User Creation";
    }

    @Override
    public String getHelpText() {
        return "This action must always be first! Validates the username of the user in validation phase.  In success phase, this will create the user in the database.";
    }

    @Override
    public void buildPage(FormContext context, LoginFormsProvider form) {
        super.buildPage(context, form);

        String attemptedUsername = context.getAuthenticationSession().getAuthNote(AbstractUsernameFormAuthenticator.ATTEMPTED_USERNAME);

        if (attemptedUsername == null) {
            return;
        }

        Map<String, String> formData = new HashMap<>();
        formData.put("username", attemptedUsername);

        if (Validation.isEmailValid(attemptedUsername)) {
            formData.put("email", attemptedUsername);
        }
        form.setFormData(new MultivaluedHashMap<>(formData));
    }

    @Override
    public void validate(ValidationContext context) {

        HttpRequest httpRequest = context.getHttpRequest();
        MultivaluedMap<String, String> formData = httpRequest.getDecodedFormParameters();
        if (formData.containsKey("cancel")) {

            UriBuilder loginUrlBuilder = UriBuilder.fromUri(Urls.realmLoginPage(
                    context.getUriInfo().getBaseUri(),
                    context.getRealm().getName()
            ));

            MultivaluedMap<String, String> queryParameters = httpRequest.getUri().getQueryParameters();
            URI loginUrl = loginUrlBuilder
                    .queryParam(Constants.CLIENT_ID, queryParameters.getFirst(Constants.CLIENT_ID))
                    .queryParam(Constants.TAB_ID, queryParameters.getFirst(Constants.TAB_ID))
                    .build();

            throw new RedirectionException(Response.Status.TEMPORARY_REDIRECT, loginUrl);
        }

        super.validate(context);
    }
}
