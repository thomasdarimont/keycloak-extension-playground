<#import "template.ftl" as layout>
<@layout.registrationLayout displayInfo=true; section>
    <#if section = "title">
        Generated Backup Codes
    <#elseif section = "header">
        Generated Backup Codes
    <#elseif section = "form">

        <div id="backup-codes">
            <p>Here are the new Backup Codes for <strong>${username}</strong></p>
            <ol>
                <#list backupCodes as backupCode>
                    <li>${backupCode.code}</li>
                </#list>
            </ol>
        </div>

        <a id="backup-codes-download" href="#" download="backup-codes.txt">Download Backup Codes</a>
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