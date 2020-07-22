package demo.keycloak.auth;

import org.jboss.logging.Logger;
import org.jboss.resteasy.spi.HttpRequest;
import org.jboss.resteasy.spi.ResteasyProviderFactory;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.AuthenticationFlowError;
import org.keycloak.authentication.Authenticator;
import org.keycloak.forms.login.LoginFormsProvider;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.utils.FormMessage;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.Response;
import java.util.Map;

public class AdHocAuthenticator implements Authenticator {

    private static final Logger LOG = Logger.getLogger(AdHocAuthenticator.class);
    private final KeycloakSession session;

    public AdHocAuthenticator(KeycloakSession session) {
        this.session = session;
    }

    @Override
    public void authenticate(AuthenticationFlowContext context) {

        UserModel user = context.getUser();

        if (user != null) {
            LOG.infof("Pass through: %s%n", user.getUsername());
        } else {
            LOG.infof("Pass through: %s%n", "anonymous");
        }

//        context.form().setError("custom_error");
//        context.failure(AuthenticationFlowError.INTERNAL_ERROR);

//        HttpRequest httpRequest = context.getHttpRequest();
//
//        boolean shouldWait = false;
//        if (httpRequest.getUri().getQueryParameters().containsKey("wait")) {
//            HttpServletResponse httpResponse = ResteasyProviderFactory.getContextData(HttpServletResponse.class);
//            javax.servlet.http.Cookie cookie = new javax.servlet.http.Cookie("keycloak-pacer", "1");
//            cookie.setMaxAge((int)(System.currentTimeMillis() / 1000) + 5); // 5 second wait time
//            httpResponse.addCookie(cookie);
//            shouldWait = true;
//        }
//
//        Map<String, Cookie> cookies = httpRequest.getHttpHeaders().getCookies();
//        Cookie cookie = cookies.get("keycloak-pacer");
//        if (cookie != null || shouldWait) {
//            LoginFormsProvider form = context.form().setExecution(context.getExecution().getId());
//            form.setError("please-wait");
//            Response response = form.createLoginUsernamePassword();
//            context.failureChallenge(AuthenticationFlowError.USER_TEMPORARILY_DISABLED, response);
//        }

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
    }

    @Override
    public void action(AuthenticationFlowContext context) {
    }

    @Override
    public void close() {
        // NOOP
    }
}
