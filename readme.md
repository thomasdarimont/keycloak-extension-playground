# Keycloak Extension Playground

Simple project environment for developing custom [Keycloak](https://keycloak.org) extensions.  
This example uses the in-process `KeycloakServer` from the `keycloak-testsuite-utils` project to ease development.

Note that you might need to build the [keycloak project](https://github.com/keycloak/keycloak) locally via `mvn clean install -DskipTests` since the
`keycloak-testsuite-utils` is not distributed via maven central. 

## IDE Setup

Create a new launch configuration and configure `com.github.thomasdarimont.keycloak.server.KeycloakPlaygroundServer` as the main class.
Additionally configure `keycloak-playground-server` as the working directory.

Note, that you can configure Keycloak to use an h2 or PostgreSQL database, depending on your needs.

The following set of JVM options configure Keycloak with:
* h2 or PostgreSQL as database
* listen on all local interfaces on port `8081`
* Keycloak Admin User `admin` with password `admin`
* `preview` Profile features enabled

You can access the local Keycloak instance via the URL: `http://localhost:8081/auth` 

### Developing with a local h2 database

```
-Dkeycloak.bind.address=127.0.0.1
-Djava.net.preferIPv4Stack=true
-Dkeycloak.connectionsJpa.url=jdbc:h2:file:./data/keycloak_4_x_master;DB_CLOSE_ON_EXIT=FALSE
-Dkeycloak.connectionsJpa.driver=org.h2.Driver
-Dkeycloak.connectionsJpa.driverDialect=org.hibernate.dialect.H2Dialect
-Dkeycloak.connectionsJpa.user=sa
-Dkeycloak.connectionsJpa.password=
-Dkeycloak.connectionsJpa.showSql=true
-Dkeycloak.connectionsJpa.formatSql=true
-Dprofile=COMMUNITY
-Dproduct.default-profile=COMMUNITY
-Dkeycloak.password.blacklists.path=./data/blacklists/
-Dcom.sun.net.ssl.checkRevocation=false
-Dkeycloak.truststore.disabled=true
-Dkeycloak.profile=COMMUNITY
-Dkeycloak.product.name=keycloak
-Dproduct.name=keycloak
-Dproduct.version=4.8.x
-Dkeycloak.profile=preview
-Dkeycloak.profile.feature.account2=enabled
-Dkeycloak.profile.feature.scripts=enabled
-Dkeycloak.theme.welcomeTheme=keycloak
-Dkeycloak.theme.dir=../simple-theme/target/theme/
```

### Developing with a PostgreSQL database

```
-Dkeycloak.bind.address=127.0.0.1
-Djava.net.preferIPv4Stack=true
-Dkeycloak.connectionsJpa.url=jdbc:postgresql://localhost:5432/keycloak_4_x_master
-Dkeycloak.connectionsJpa.driver=org.postgresql.Driver
-Dkeycloak.connectionsJpa.driverDialect=org.hibernate.dialect.PostgreSQLDialect
-Dkeycloak.connectionsJpa.user=keycloak
-Dkeycloak.connectionsJpa.password=keycloak
-Dkeycloak.connectionsJpa.showSql=true
-Dkeycloak.connectionsJpa.formatSql=true
-Dprofile=COMMUNITY
-Dproduct.default-profile=COMMUNITY
-Dkeycloak.password.blacklists.path=/home/tom/dev/tmp/blacklists/
-Dcom.sun.net.ssl.checkRevocation=false
-Dkeycloak.truststore.disabled=true
-Dkeycloak.profile=COMMUNITY
-Dkeycloak.product.name=keycloak
-Dproduct.name=keycloak
-Dproduct.version=4.8.x
-Dkeycloak.profile=preview
-Dkeycloak.profile.feature.account2=enabled
-Dkeycloak.profile.feature.scripts=enabled
-Dkeycloak.theme.welcomeTheme=keycloak
-Dkeycloak.theme.dir=../simple-theme/target/theme/
```

## Developing a Keycloak extension

To develop your extension, simply create a new maven module, e.g. `simple-auth-extension` in the `keycloak-extension-playground`.
Then add the module as a dependency to the `keycloak-playground-server` project.

The [Server Development part of the Keycloak reference documentation](https://www.keycloak.org/docs/latest/server_development/index.html) contains additional resources and examples for developing custom Keycloak extensions.

## Examples

The [Keycloak](https://github.com/keycloak/keycloak) project on github provides a useful set of [examples for Keycloak extensions](https://github.com/keycloak/keycloak/tree/master/examples).

### Simple Auth Extension
The `simple-auth-extension` project provides an example project that can be used as a starting point.

### Simple Themes
The `simple-themes` example project demonstrates how to use custom themes with keycloak. 