package demo.keycloak.auth.forgotusername;

import demo.keycloak.auth.forgotusername.UsernameLookupService.UsernameLookupRequest;
import demo.keycloak.auth.forgotusername.UsernameLookupService.UsernameLookupResponse;
import org.jboss.logging.Logger;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.Authenticator;
import org.keycloak.models.Constants;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.services.Urls;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

public class ForgotUsernameAuthenticator implements Authenticator {

    private static final Logger LOG = Logger.getLogger(ForgotUsernameAuthenticator.class);
    private final KeycloakSession session;

    private final UsernameLookupService usernameLookupService;

    public ForgotUsernameAuthenticator(KeycloakSession session, UsernameLookupService usernameLookupService) {
        this.session = session;
        this.usernameLookupService = usernameLookupService;
    }

    @Override
    public void authenticate(AuthenticationFlowContext context) {

        String forgotUsername = context.getHttpRequest().getUri().getQueryParameters().getFirst("fu");
        if (Boolean.parseBoolean(forgotUsername)) {
            Response response = context.form()
                    .createForm("forgot-username-form.ftl");

            context.challenge(response);
            return;
        }

        context.success();
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

    @Override
    public void action(AuthenticationFlowContext context) {

        MultivaluedMap<String, String> formParams = context.getHttpRequest().getDecodedFormParameters();

        UsernameLookupRequest lookupRequest = new UsernameLookupRequest();
        lookupRequest.setReferenceNumber(formParams.getFirst("referenceNumber"));

        UsernameLookupResponse lookupResponse = usernameLookupService.lookupUsername(lookupRequest);

        String positiveMessage = "Falls ein Benutzer mit den angegeben Informationen existiert erhalten Sie in Kürze ihrem Benutzernamen an die bei ihrem Konto hinterlegte E-Mail Adresse.";

        String message = lookupResponse.getErrorCode() == null ? positiveMessage : lookupResponse.getErrorCode();
        context.resetFlow();

        UriBuilder loginUriBuilder = UriBuilder.fromUri(Urls.realmLoginPage(context.getUriInfo().getBaseUri(), context.getRealm().getName()))
                .queryParam(Constants.CLIENT_ID, context.getAuthenticationSession().getClient().getClientId())
                .queryParam(Constants.TAB_ID, context.getAuthenticationSession().getTabId());
        Response response = context.form()
                .setAttribute("lookupResultMessage", message)
                .setAttribute("loginUrl", loginUriBuilder.build())
                .createForm("forgot-username-result.ftl");

        context.challenge(response);
    }

    @Override
    public void close() {
        // NOOP
    }
    /*
     var forgotPassword = document.querySelector("[href*=reset-credentials]");
     let forgotPasswordUrl = new URL(forgotPassword.href);

     var forgotUsername = forgotPassword.cloneNode();
     forgotUsername.href=forgotPasswordUrl.pathname + forgotPasswordUrl.search + "&fu=true";
     forgotUsername.text=" Forgot Username? ";
     forgotUsername.tabIndex=6;
     forgotPassword.parentElement.append(forgotUsername);

     */
}
