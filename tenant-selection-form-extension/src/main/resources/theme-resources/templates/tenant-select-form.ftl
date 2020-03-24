<#import "template.ftl" as layout>
<@layout.registrationLayout displayInfo=true; section>
    <#if section = "title">
        Tenant selection
    <#elseif section = "header">
        Tenant selection
    <#elseif section = "form">
        <p>Please select a Tenant:</p>
        <form action="${url.loginAction}" class="${properties.kcFormClass!}" id="kc-u2f-login-form" method="post">

            <datalist id="tenantlist">
                <option value="0010">
                <option value="0020">
                <option value="0030">
                <option value="0040">
            </datalist>


            <datalist id="grouplist">
                <option value="1234">
                <option value="5678">
                <option value="9780">
                <option value="3323">
            </datalist>

            <div>
                <label for="tenant">Mandant</label>
                <input id="tenant" name="tenant" list="tenantlist"/>
            </div>

            <div>
                <label for="group">Group</label>
                <input id="group" name="group" list="grouplist"/>
            </div>

            <input class="${properties.kcButtonClass!} ${properties.kcButtonPrimaryClass!} ${properties.kcButtonLargeClass!}"
                   type="submit" value="${msg("doSubmit")}"/>
        </form>
    </#if>
</@layout.registrationLayout>