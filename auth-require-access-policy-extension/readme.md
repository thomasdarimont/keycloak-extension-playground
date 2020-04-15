Access Policy Acuthenticator
---

Simple example for an authenticator which can evaluate custom access-policies written as JSON documents.

# Access Policies

An access-policy can be defined as a JSON document which holds a list of access-policy entries. 
An `access-policy-entry` consists of a client-id regex pattern `app` and a list of allowed realm- or client-role names.
Client roles have the form `clientId.roleName`. 

If a client is not contained in the access-policy the access is always granted. 
If a client is contained in the access-policy but contains an role list with the sole value `NONE`, then access is always denied.

An example access-policy looks like this:
```json

{"p":[
    { "app": "clientIdRegex", "allow": ["role1","role2"] },
    { "app": "clientId2", "allow": ["NONE"] }
]}
```

# Additional Access Policy Examples

## Explicitly allow access to clients for some roles

Users with role `user` or `admin` can access the client `test-client`.
Users with role `admin` can access the client `admin-client`.
```json
{"p":[
    { "app": "test-client", "allow": ["user", "admin"] },
    { "app": "admin-client", "allow": ["admin"] }
]}
```

## Allow access to clients based on client regex pattern for some roles

Users with role `domain-role` can access the clients `app-domain-a`, `app-domain-b` and `app-domain-c`.
```json
{"p":[
    { "app": "app-domain-(a|b|c)", "allow": ["domain-role"] }
]}
```

## Allow access to clients based on client regex pattern for some client roles

Users with an `access` client-role for the respective client can access the client apps denoted by `app-domain-a`, `app-domain-b` and `app-domain-c`.
```json
{"p":[
    { "app": "(app-domain-(a|b|c))", "allow": ["$1.access"] }
]}
```

## Allow access to clients based on client regex pattern for some roles

Users with role `domain-role` can access the clients `app-domain-a`, `app-domain-b` and `app-domain-c`.
```json
{"p":[
    { "app": "app-domain-(a|b|c)", "allow": ["domain-role"] }
]}
```    

## Deny access to the account app for all
```json
{"p":[
    { "app": "account", "allow": ["NONE"] }
]}
``` 