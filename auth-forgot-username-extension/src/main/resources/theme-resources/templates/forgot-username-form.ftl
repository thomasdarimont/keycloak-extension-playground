<#import "template.ftl" as layout>
<@layout.registrationLayout displayInfo=true; section>
    <#if section = "title">
        Forgot username
    <#elseif section = "header">
        Forgot username
    <#elseif section = "form">
        <p>Try to recover username</p>
        <form action="${url.loginAction}" class="${properties.kcFormClass!}" id="kc-u2f-login-form" method="post">

            <div class="${properties.kcFormGroupClass!}">
                <label for="referenceNumber" class="${properties.kcLabelClass!}">Reference Number</label>
                <input id="referenceNumber" type="text" name="referenceNumber" class="${properties.kcInputClass!}" />
            </div>

            <div class="${properties.kcFormGroupClass!}">
                <label for="firstname" class="${properties.kcLabelClass!}">Firstname</label>
                <input id="firstname" type="text" name="firstname" class="${properties.kcInputClass!}" />
            </div>

            <div class="${properties.kcFormGroupClass!}">
                <label for="lastname" class="${properties.kcLabelClass!}">lastname</label>
                <input id="lastname" type="text" name="lastname" class="${properties.kcInputClass!}" />
            </div>

            <div class="${properties.kcFormGroupClass!}">
                <label for="dateOfBirth" class="${properties.kcLabelClass!}">Date of Birth</label>
                <input id="dateOfBirth" type="date" name="dateOfBirth" class="${properties.kcInputClass!}" />
            </div>

            <div class="${properties.kcFormGroupClass!}">
                <label for="zipCode" class="${properties.kcLabelClass!}">Zip</label>
                <input id="zipCode" type="text" name="zipCode" class="${properties.kcInputClass!}" />
            </div>

            <input class="${properties.kcButtonClass!} ${properties.kcButtonPrimaryClass!} ${properties.kcButtonLargeClass!}"
                   type="submit" value="${msg("doSubmit")}"/>

        </form>
    </#if>
</@layout.registrationLayout>