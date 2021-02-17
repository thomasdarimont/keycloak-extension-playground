package demo.keycloak.auth;

import com.google.auto.service.AutoService;
import org.keycloak.authentication.Authenticator;
import org.keycloak.authentication.AuthenticatorFactory;
import org.keycloak.authentication.authenticators.browser.OTPFormAuthenticatorFactory;
import org.keycloak.models.KeycloakSession;

@AutoService(AuthenticatorFactory.class)
public class CancellableOTPFormAuthenticatorFactory extends OTPFormAuthenticatorFactory {

    public static final String PROVIDER_ID = "auth-cancel-otp-form";

    private static final CancellableOTPFormAuthenticator INSTANCE = new CancellableOTPFormAuthenticator();

    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    @Override
    public Authenticator create(KeycloakSession session) {
        return INSTANCE;
    }

    @Override
    public String getDisplayType() {
        return "Cancellable OTP Form";
    }

    @Override
    public String getHelpText() {
        return "Validates a OTP on a separate OTP form.";
    }

}
