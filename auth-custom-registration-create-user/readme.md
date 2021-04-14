Example for requiring Terms Acceptance before Registration
----

This example shows how to customize the registration process so that a user must agree to the terms and conditions
before registering. To do this we extend `org.keycloak.authentication.forms.RegistrationUserCreation`
via `com.github.thomasdarimont.keycloak.auth.CustomRegistrationUserCreation`
and add the handling for terms acceptance validation.

We also store the timestamp of the acceptance in the user attribute `terms_accepted`.

# Customize the Registration Flow

In the admin-console, copy the `registration` flow to `registration-custom` and replace the
`Registration User Creation` `FormAction` with `Demo: Registration User Creation With Terms`. To do this, you can click
on the `Actions` menu of the `Custom-registration Registration Form` and select
`Add execution`.

Next goto the `Bindings` tab and set the `Registration Flow` to our new `registration-custom`.

# Adjust the Login Theme

In order to let the user accept our terms and conditions, we need to adjust the register page of the standard Keycloak
login theme. To do that, we create a new custom login theme, e.g. `login-custom` with the following `theme.properties`

```
parent=keycloak
import=common/keycloak

kcCheckboxWrapperClass=pf-c-check
kcCheckboxInputClass=pf-c-check__input
```

Note that an [example login theme can be found in here](../simple-theme/src/main/themes/keycloak-revised)

## Create Message ResourceBundle

Create a file called `messages_en.properties` in the `messages` folder within the new login theme with the following
contents:

```
acceptTerms=Accept Terms
termsText=The terms and conditions Terms can be found <a href="http://example.com/terms" id="termsLink">here</a>
termsRequired=You must agree to our terms and conditions to register.
```

## Adjust the Register Page

Copy the `register.ftl` into the new login theme folder and insert the following HTML snippet right before
`<#if recaptchaRequired??>`.

```html
<#if acceptTermsRequired??>

<div class="${properties.kcFormGroupClass!}">

    <div id="kc-terms-text">
        ${msg("termsTitle")}
        <div>
            ${kcSanitize(msg("termsText"))?no_esc}
        </div>
    </div>

    <script defer>
        document.getElementById("termsLink").setAttribute("target", "_blank");
    </script>
    <div class="${properties.kcLabelWrapperClass!}">
        <label for="acceptTerms" class="${properties.kcLabelClass!}">${msg("acceptTerms")}</label>

        <input type="checkbox" id="acceptTerms" name="terms" class="${properties.kcCheckboxInputClass!}"
               value="${(register.formData.acceptTerms!'')}"
               aria-invalid="<#if messagesPerField.existsError('terms')>true</#if>"
        />
    </div>
</div>
</#if>
```

