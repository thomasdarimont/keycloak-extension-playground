package demo.keycloak.auth.notify;

import com.google.auto.service.AutoService;
import org.keycloak.Config;
import org.keycloak.authentication.Authenticator;
import org.keycloak.authentication.AuthenticatorFactory;
import org.keycloak.models.AuthenticationExecutionModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.provider.ProviderConfigurationBuilder;

import java.util.List;

@AutoService(AuthenticatorFactory.class)
public class LoginNotifyEmailAuthenticatorFactory implements AuthenticatorFactory {

    private static final String PROVIDER_ID = "auth-login-email-notification";


    @Override
    public String getDisplayType() {
        return "Login Email Notification";
    }

    @Override
    public String getReferenceCategory() {
        return null;
    }

    @Override
    public boolean isConfigurable() {
        return true;
    }

    public static final AuthenticationExecutionModel.Requirement[] REQUIREMENT_CHOICES = {
            AuthenticationExecutionModel.Requirement.REQUIRED, AuthenticationExecutionModel.Requirement.DISABLED
    };

    @Override
    public AuthenticationExecutionModel.Requirement[] getRequirementChoices() {
        return REQUIREMENT_CHOICES;
    }

    @Override
    public boolean isUserSetupAllowed() {
        return false;
    }

    @Override
    public String getHelpText() {
        return "Sends a login notification email if the last login is older than the specified amount";
    }

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return ProviderConfigurationBuilder
                .create()
                .property().name(LoginNotifyEmailAuthenticator.EMAIL_TEMPLATE_NAME)
                .type(ProviderConfigProperty.STRING_TYPE)
                .label("Email Template")
                .defaultValue("loginNotifyEmail")
                .helpText("Email Template name for Login Notification E-Mail")
                .add()
                .property().name(LoginNotifyEmailAuthenticator.TIME_SINCE_LAST_LOGIN)
                .type(ProviderConfigProperty.STRING_TYPE)
                .label("Time since last login")
                .defaultValue("PT720H")
                .helpText("Duration between last login and current login. If the last login is older than the given duration, the login notification email is sent. Defaults to 30 days (PT720H).")
                .add().build();
    }

    @Override
    public void close() {
        // NOOP
    }

    @Override
    public Authenticator create(KeycloakSession session) {
        return new LoginNotifyEmailAuthenticator(session);
    }

    @Override
    public void init(Config.Scope config) {
        // NOOP
    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {
        // NOOP
    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }
}
