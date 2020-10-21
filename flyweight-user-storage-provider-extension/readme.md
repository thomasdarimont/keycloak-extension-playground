Flyweight Federated User Storage Provider Example
---

This example demonstrates how to integrate user account information managed by an external user-store.

This example demonstrates:
- how to lookup users from an external user-store
- how to check a users password against an external user-store
- how to maintain attributes in an external store
- how to maintain realm and client role mappings in an external store

# Build

```
mvn clean package
```

# Setup

1) Copy the resulting jar into the standalone/deployments folder in the Keycloak distribution home.
2) Start Keycloak
3) Sign into the Keycloak-Admin console
4) Create a new federation provider of type `flyweight-acme-user` in a realm. 
   Note: This federation provider does not support caching, so you need to configure the NO_CACHE caching mode.  

You should now be able to list all the users in provided by the custom federation.

# Demo

You can use the following users to login - all users have the same Password: `secret`:
- Username: `user1`  
- Username: `user2`  
- Username: `user3`
- Username: `user4` (this user is disabled)

To login I'd recommend to open the account client of the current realm with a new private / incognito browser window.
