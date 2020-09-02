<#import "template.ftl" as layout>
<@layout.registrationLayout displayInfo=true; section>
    <#if section = "title">
        Forgot username
    <#elseif section = "header">
        Forgot username
    <#elseif section = "form">
        <p>Username Lookup Result</p>
        <div>${lookupResultMessage}</div>

        <div class="${properties.kcFormGroupClass!} ${properties.kcFormSettingClass!}">
            <div id="kc-form-options" class="${properties.kcFormOptionsClass!}">
                <div class="${properties.kcFormOptionsWrapperClass!}">
                    <span><a href="${loginUrl}">${kcSanitize(msg("backToLogin"))?no_esc}</a></span>
                </div>
            </div>
        </div>
    </#if>
</@layout.registrationLayout>