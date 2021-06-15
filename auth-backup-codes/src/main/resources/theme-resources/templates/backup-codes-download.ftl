<#import "template.ftl" as layout>
<@layout.registrationLayout displayInfo=true; section>
    <#if section = "title">
        ${msg("backup-code-generated")}
    <#elseif section = "header">
        ${msg("backup-code-generated")}
    <#elseif section = "form">

        <div class="alert alert-info" style="margin-top:0 !important;margin-bottom:30px !important">
            <span class="pficon pficon-info"></span>
            <p class="instruction">${kcSanitize(msg("backup-code-download-hint"))}</p>
        </div>

        <div id="backup-codes">
            <p>${kcSanitize(msg("backup-code-download-cta", realm.displayName, createdAtDate))?no_esc}</p>
            <ol>
                <#list backupCodes as backupCode>
                    <li>${backupCode.code}</li>
                </#list>
            </ol>
        </div>

        <a id="backup-codes-download" href="#" download="${kcSanitize(realm.name)}-backup-codes.txt"
           class="${properties.kcButtonClass!} ${properties.kcButtonPrimaryClass!} ${properties.kcButtonLargeClass!}">
            ${msg("backup-code-download")}</a>
        <script>
            let lnkBackupCodesDownload = document.getElementById("backup-codes-download");
            lnkBackupCodesDownload.href = "data:text/html," + document.getElementById("backup-codes").textContent;
            lnkBackupCodesDownload.addEventListener("click", function () {
                let btnBackToApp = document.getElementById("back-to-application");
                btnBackToApp.disabled = false;
                btnBackToApp.classList.remove("hidden");
                return true;
            });
        </script>

        <form id="kc-backup-codes-download-form" class="${properties.kcFormClass!}" action="${url.loginAction}"
              method="post">
            <div class="${properties.kcFormGroupClass!}">
                <div id="kc-form-buttons" class="${properties.kcFormButtonsClass!}">
                    <button id="back-to-application" class="${properties.kcButtonClass!} ${properties.kcButtonDefaultClass!} ${properties.kcButtonLargeClass!} hidden" type="submit" name="cancel-aia" value="true" />
                    ${msg("doSubmit")}
                    </button>
                </div>
            </div>
        </form>
    </#if>
</@layout.registrationLayout>