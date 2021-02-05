<#import "template.ftl" as layout>
<@layout.registrationLayout displayInfo=false displayRequiredFields=false; section>
    <#if section = "title">
        MFA OTP
    <#elseif section = "header">
        MFA OTP
    <#elseif section = "form">
        <div>${customMsg("mfa_otp_enter_code")}</div>

        <style>
            #kc-username {
                display: none;
            }
        </style>

<#--            <ul>-->
<#--                <#list .data_model?keys as key>-->
<#--                    <li>${key}</li>-->
<#--                </#list>-->
<#--            </ul>-->

        <form action="${url.loginAction}" class="${properties.kcFormClass!}" id="kc-u2f-login-form" method="post">
            <div class="${properties.kcFormGroupClass!}">
                <div class="${properties.kcLabelWrapperClass!}">
                    <label for="otp" class="${properties.kcLabelClass!}">${msg("loginOtpOneTime")}</label>
                </div>

                <div class="${properties.kcInputWrapperClass!}">
                    <input id="otp" name="challenge_input" autocomplete="off" inputmode="numeric" type="text"
                           spellcheck="false"
                           placeholder="OTP Code..."
                           class="${properties.kcInputClass!}"
                           autofocus/>
                </div>
            </div>

            <input class="${properties.kcButtonClass!} ${properties.kcButtonPrimaryClass!} ${properties.kcButtonLargeClass!}"
                   type="submit" value="${msg("doSubmit")}"/>

            <input class="${properties.kcButtonClass!} ${properties.kcButtonPrimaryClass!} ${properties.kcButtonLargeClass!}"
                   type="submit" name="cancel" value="${msg("doCancel")}"/>
        </form>
    </#if>
</@layout.registrationLayout>