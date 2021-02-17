package demo.keycloak.auth;

import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.authenticators.browser.PasswordForm;

import javax.ws.rs.core.MultivaluedMap;

public class CancellablePasswordForm extends PasswordForm {

    @Override
    public void action(AuthenticationFlowContext context) {
        MultivaluedMap<String, String> formData = context.getHttpRequest().getDecodedFormParameters();
        if (formData.containsKey("cancel")) {
            context.resetFlow();
            return;
        }
        if (!validateForm(context, formData)) {
            return;
        }
        context.success();
    }
}
