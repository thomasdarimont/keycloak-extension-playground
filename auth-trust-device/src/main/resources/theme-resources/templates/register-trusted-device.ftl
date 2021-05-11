<#import "template.ftl" as layout>
<@layout.registrationLayout displayInfo=true; section>
    <#if section = "title">
        Register trusted device form
    <#elseif section = "header">
        Register trusted device form
    <#elseif section = "form">
        <p>Do you trust this device?</p>
        <form action="${url.loginAction}" class="${properties.kcFormClass!}" id="kc-u2f-login-form" method="post">
            <label for="device">Device</label>
            <input id="device" type="text" name="device" value="${(device!'')}"/>
            <input class="${properties.kcButtonClass!} ${properties.kcButtonPrimaryClass!} ${properties.kcButtonLargeClass!}"
                   type="submit" name="trust-device" value="${msg("yes")}"/>
            <input class="${properties.kcButtonClass!} ${properties.kcButtonSecondaryClass!} ${properties.kcButtonLargeClass!}"
                   type="submit" value="${msg("no")}"/>
        </form>
    </#if>
</@layout.registrationLayout>