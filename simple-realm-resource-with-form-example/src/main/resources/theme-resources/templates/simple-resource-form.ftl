<#import "template.ftl" as layout>
<@layout.registrationLayout displayInfo=true; section>
    <#if section = "title">
        Simple resource form
    <#elseif section = "header">
        Simple resource form
    <#elseif section = "form">
        <h1>Hello from Realm: ${realm_name}</h1>
    </#if>
</@layout.registrationLayout>