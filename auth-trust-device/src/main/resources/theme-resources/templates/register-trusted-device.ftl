<#import "template.ftl" as layout>
<@layout.registrationLayout displayInfo=true; section>
    <#if section = "title">
        Register Trusted Device
    <#elseif section = "header">
        Register Trusted Device
    <#elseif section = "form">
        <p>Do you trust this device?</p>
        <form action="${url.loginAction}" class="${properties.kcFormClass!}" id="kc-u2f-login-form" method="post">
            <label for="device">Device</label>
            <input id="device" type="text" name="device" value="${(device!'')}"/>
            <input class="${properties.kcButtonClass!} ${properties.kcButtonPrimaryClass!} ${properties.kcButtonLargeClass!}"
                   type="submit" name="trust-device" value="${msg("yes")}"/>
            <input class="${properties.kcButtonClass!} ${properties.kcButtonSecondaryClass!} ${properties.kcButtonLargeClass!}"
                   type="submit" name="dont-trust-device" value="${msg("no")}"/>
            <div class="checkbox">
                <label for="removeOtherTrustedDevices" class="${properties.kcLabelClass!}">
                    <input type="checkbox" id="removeOtherTrustedDevices" name="remove-other-trusted-devices" class="${properties.kcCheckboxInputClass!}"
                           value=""/>
                    Remove all Trusted devices
                </label>
            </div>
        </form>
    </#if>
</@layout.registrationLayout>