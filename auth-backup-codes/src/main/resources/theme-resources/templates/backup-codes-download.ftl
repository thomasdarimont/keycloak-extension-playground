<#import "template.ftl" as layout>
<@layout.registrationLayout displayInfo=true; section>
    <#if section = "title">
        ${msg("backup-code-generated")}
    <#elseif section = "header">
        ${msg("backup-code-generated")}
    <#elseif section = "form">

        <div id="backup-codes">
            <p>${kcSanitize(msg("backup-code-download-cta", username))?no_esc}</p>
            <div class="alert alert-info" style="margin-top:0 !important;margin-bottom:30px !important">
                <span class="pficon pficon-info"></span>
                <p class="instruction">${kcSanitize(msg("backup-code-download-hint"))}</p>
            </div>
            <ol>
                <#list backupCodes as backupCode>
                    <li>${backupCode.code}</li>
                </#list>
            </ol>
        </div>

        <a id="backup-codes-download" href="#" download="backup-codes.txt">${msg("backup-code-download")}</a>
        <script>
            let backupCodesDownload = document.querySelector("#backup-codes-download");
            backupCodesDownload.href = "data:text/html," + document.getElementById("backup-codes").textContent;
        </script>

        <form id="kc-passwd-update-form" class="${properties.kcFormClass!}" action="${url.loginAction}" method="post">
            <div class="${properties.kcFormGroupClass!}">
                <div id="kc-form-buttons" class="${properties.kcFormButtonsClass!}">
                    <button class="${properties.kcButtonClass!} ${properties.kcButtonDefaultClass!} ${properties.kcButtonLargeClass!}"
                            type="submit" name="cancel-aia" value="true"/>${msg("backToApplication")?no_esc}</button>
                </div>
            </div>
        </form>
    </#if>
</@layout.registrationLayout>