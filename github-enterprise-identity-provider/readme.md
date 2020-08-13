Github Enterprise Identity Provider for Keycloak
---

This is a simple custom IdentityProvider that extends the existing github identity provider to allow customization
of various URLs.

# Build

In order to build this example you need to follow the general building instructions from [in the project readme.md](../readme.md).

```
mvn clean package
cp target/*.jar /path/to/keycloak/standalone/deployments 
```

# Setup

To setup an oauth client in Github Enterprise for the Keycloak login, take a look at:
- [Miniorange Guide to oauth-openid-login-using-github-enterprise](https://plugins.miniorange.com/oauth-openid-login-using-github-enterprise)
- [Github authorizing-oauth-apps](https://docs.github.com/en/enterprise/2.21/user/developers/apps/authorizing-oauth-apps)


To setup the Github Enterprise IdP in Keycloak goto:
1) Identity Providers
2) Select Github Enterprise
3) Configure the URLs & settings (clientId, secret) according to your environment

If now open the realm login page, you should now see a button `Github Enterprise` listed as Identity provider.
When you click on `Github Enterprise` you should get redirected to the github enterprise login.