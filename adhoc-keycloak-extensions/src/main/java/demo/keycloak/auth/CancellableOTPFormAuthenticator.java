package demo.keycloak.auth;

import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.authenticators.browser.OTPFormAuthenticator;

import javax.ws.rs.core.MultivaluedMap;

public class CancellableOTPFormAuthenticator extends OTPFormAuthenticator {

    public void validateOTP(AuthenticationFlowContext context) {

        MultivaluedMap<String, String> formData = context.getHttpRequest().getDecodedFormParameters();
        if (formData.containsKey("cancel")) {
            context.resetFlow();
            return;
        }

        super.validateOTP(context);
    }
}
