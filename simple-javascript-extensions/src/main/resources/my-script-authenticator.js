AuthenticationFlowError = Java.type("org.keycloak.authentication.AuthenticationFlowError");

function authenticate(context) {

    LOG.info(script.name + " --> trace auth for: " + user.username);

    /*
    if (   user.username === "tester"
        && user.getAttribute("someAttribute")
        && user.getAttribute("someAttribute").contains("someValue")) {

        context.failure(AuthenticationFlowError.INVALID_USER);
        return;
    }
    */

    context.success();
}