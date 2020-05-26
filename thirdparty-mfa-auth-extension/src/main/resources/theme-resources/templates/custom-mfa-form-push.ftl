<#import "template.ftl" as layout>
<@layout.registrationLayout displayInfo=false displayRequiredFields=false; section>
    <#if section = "title">
        MFA PUSH
    <#elseif section = "header">
        MFA PUSH
    <#elseif section = "form">
        <div>${customMsg("mfa_push_sent")}</div>
        <div>${customMsg(hint)}</div>

        <style>
            #kc-username {
                display: none;
            }
        </style>

    <#--        <ul>-->
    <#--            <#list .data_model?keys as key>-->
    <#--                <li>${key}</li>-->
    <#--            </#list>-->
    <#--        </ul>-->

        <form action="${url.loginAction}" class="${properties.kcFormClass!}" id="kc-u2f-login-form" method="post">
            <input class="${properties.kcButtonClass!} ${properties.kcButtonPrimaryClass!} ${properties.kcButtonLargeClass!}"
                   type="submit" name="useOtp" value="${customMsg("use_otp")}"/>
            <input class="${properties.kcButtonClass!} ${properties.kcButtonPrimaryClass!} ${properties.kcButtonLargeClass!}"
                   type="submit" name="cancel" value="${msg("doCancel")}"/>
        </form>

        <script>
            function autoSubmitForm() {
                document.forms[0].submit()
            }
            window.setTimeout(autoSubmitForm, 2500);
        </script>
    </#if>
</@layout.registrationLayout>