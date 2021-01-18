<#import "template.ftl" as layout>
<@layout.registrationLayout displayInfo=true; section>
    <#if section = "title">
        Access Code Form
    <#elseif section = "header">
        Access Code Form
    <#elseif section = "form">
        <p>Enter access code</p>
        <form action="${url.loginAction}" class="${properties.kcFormClass!}" id="kc-u2f-login-form" method="post">
            <div class="${properties.kcFormGroupClass!}">
                <label for="emailCode">Access Code</label>
                <input id="emailCode" name="emailCode" type="text" inputmode="numeric" pattern="[0-9]*"/>
            </div>

            <input class="${properties.kcButtonClass!} ${properties.kcButtonPrimaryClass!} ${properties.kcButtonLargeClass!}"
                   type="submit" value="${msg("doSubmit")}"/>

            <input name="resend"
                   class="${properties.kcButtonClass!} ${properties.kcButtonPrimaryClass!} ${properties.kcButtonLargeClass!}"
                   type="submit" value="${msg("resendCode")}"/>

            <input name="cancel"
                   class="${properties.kcButtonClass!} ${properties.kcButtonPrimaryClass!} ${properties.kcButtonLargeClass!}"
                   type="submit" value="${msg("doCancel")}"/>
        </form>
    </#if>
</@layout.registrationLayout>