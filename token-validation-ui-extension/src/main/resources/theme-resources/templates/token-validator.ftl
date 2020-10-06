<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
        "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" class="layout-pf">

<head>
    <meta charset="utf-8">
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="robots" content="noindex, nofollow">

    <#if properties.meta?has_content>
        <#list properties.meta?split(' ') as meta>
            <meta name="${meta?split('==')[0]}" content="${meta?split('==')[1]}"/>
        </#list>
    </#if>
    <title>Token validator</title>
    <link rel="icon" href="${url.resourcesCommonPath}/img/favicon.ico"/>
    <link href="${url.resourcesCommonPath}/node_modules/patternfly/dist/css/patternfly.css" rel="stylesheet"/>
    <link href="${url.resourcesCommonPath}/node_modules/patternfly/dist/css/patternfly-additions.css" rel="stylesheet"/>
    <style>
        textarea#token {
            height: 5em;
        }

        @media (min-width: 1200px) {
            textarea#token {
                height: 40em;
            }
        }
    </style>
</head>

<body class="cards-pf">

<div id="kc-header" class="login-pf-header">
    <h1 id="kc-header-wrapper">JWT Token Validator</h1>
</div>

<div class="container-cards-pf">
    <div class="row row-cards-pf">
        <div class="col-md-12 col-lg-3">
            <div class="card-pf">
                <form method="post">
                    <div class="card-pf-heading">
                        <div class="dropdown card-pf-time-frame-filter">
                            <button class="btn btn-primary">Submit</button>
                        </div>
                        <h2 class="card-pf-title">Encoded Token</h2>
                    </div>
                    <div class="card-pf-body">
                        <div class="form-group">
                            <textarea id="token" name="token" class="form-control">${token!}</textarea>
                        </div>
                        <div class="form-group">
                            <label for="tokenLength">Token Length (bytes)</label>
                            <output id="tokenLength" class="form-control">${tokenLength!}</output>
                        </div>
                    </div>
                </form>
            </div>
        </div>

        <#if valid??>
            <div class="col-md-12 col-lg-9">
                <#if valid>
                    <div class="row card-pf" style="background-color: #e9f4e9;">
                        <div class="card-pf-body">
                            <h2 class="card-pf-title">
                                <span class="pficon pficon-ok" style="color: #3f9c35;"></span>
                                Token is valid - Signed with <#if activeKey>active key<#else>passive key</#if>
                            </h2>
                        </div>
                    </div>
                <#else>
                    <div class="row card-pf" style="background-color: #ffe6e6;">
                        <div class="card-pf-body">
                            <h2 class="card-pf-title">
                                <span class="pficon pficon-error-circle-o"></span>
                                Invalid Token - ${error}
                            </h2>
                        </div>
                    </div>
                </#if>
            </div>
        </#if>

        <#if tokenParsed??>
            <div class="col-md-12 col-lg-9">
                <div class="row card-pf">
                    <div class="card-pf-heading">
                        <h2 class="card-pf-title">Header</h2>
                    </div>
                    <div class="card-pf-body">
                        <pre>${header}</pre>
                    </div>
                </div>

                <div class="row card-pf">
                    <div class="card-pf-heading">
                        <h2 class="card-pf-title">Payload</h2>
                    </div>
                    <div class="card-pf-body">
                        <pre>${tokenParsed}</pre>
                    </div>
                </div>
            </div>
        </#if>
    </div>
</div>

</body>
</html>