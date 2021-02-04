<#import "template.ftl" as layout>
<@layout.registrationLayout displayInfo=true; section>
    <#if section = "title">
        Update Custom info form
    <#elseif section = "header">
        Update Custom info form
    <#elseif section = "form">

        <h1>Hello ${username}</h1>
        <p>Please update your mobile phone number</p>
        <form id="kc-passwd-update-form" class="${properties.kcFormClass!}" action="${url.loginAction}" method="post">
            <div class="${properties.kcFormGroupClass!}">
                <div class="${properties.kcLabelWrapperClass!}">
                    <label for="mobile">Mobile</label>
                </div>
                <div class="${properties.kcInputWrapperClass!}">
                    <input id="mobile" type="tel" name="mobile" value="${currentMobile}" required aria-invalid="<#if messagesPerField.existsError('mobile')>true</#if>"/>

                    <#if messagesPerField.existsError('mobile')>
                        <span id="input-error-mobile" class="${properties.kcInputErrorMessageClass!}" aria-live="polite">
                            ${kcSanitize(messagesPerField.get('mobile'))?no_esc}
                        </span>
                    </#if>
                </div>
            </div>

            <div class="${properties.kcFormGroupClass!}">
                <div id="kc-form-buttons" class="${properties.kcFormButtonsClass!}">
                    <input class="${properties.kcButtonClass!} ${properties.kcButtonPrimaryClass!} ${properties.kcButtonLargeClass!}"
                           type="submit" value="${msg("doSubmit")}"/>
                </div>
            </div>
        </form>
    </#if>
</@layout.registrationLayout>