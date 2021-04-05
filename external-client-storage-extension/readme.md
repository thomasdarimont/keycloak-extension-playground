# Obtaining an Access Token for a User with a virtual client
```
KC_CLIENT_ID=x:custom-client-1
KC_CLIENT_SECRET=secret
KC_USERNAME=tester
KC_PASSWORD=test
KC_ISSUER=http://localhost:8081/auth/realms/external-clients

KC_RESPONSE=$( \
curl \
  -d "client_id=$KC_CLIENT_ID" \
  -d "client_secret=$KC_CLIENT_SECRET" \
  -d "username=$KC_USERNAME" \
  -d "password=$KC_PASSWORD" \
  -d "grant_type=password" \
  -d "scope=profile" \
  "$KC_ISSUER/protocol/openid-connect/token" \
)
echo $KC_RESPONSE | jq -C .

KC_ACCESS_TOKEN=$(echo $KC_RESPONSE | jq -r .access_token)
echo $KC_ACCESS_TOKEN 
```


```
KC_CLIENT_ID=x:custom-client-1
KC_CLIENT_SECRET=secret
KC_ISSUER=http://localhost:8081/auth/realms/external-clients

KC_RESPONSE=$( \
curl \
  -d "client_id=$KC_CLIENT_ID" \
  -d "client_secret=$KC_CLIENT_SECRET" \
  -d "grant_type=client_credentials" \
  -d "scope=profile" \
  "$KC_ISSUER/protocol/openid-connect/token" \
)
echo $KC_RESPONSE | jq -C .

KC_ACCESS_TOKEN=$(echo $KC_RESPONSE | jq -r .access_token)
echo $KC_ACCESS_TOKEN 
```

```
KC_CLIENT_ID=x:custom-client-2
KC_CLIENT_SECRET=secret
KC_USERNAME=tester
KC_PASSWORD=test
KC_ISSUER=http://localhost:8081/auth/realms/external-clients

KC_RESPONSE=$( \
curl \
  -d "client_id=$KC_CLIENT_ID" \
  -d "client_secret=$KC_CLIENT_SECRET" \
  -d "username=$KC_USERNAME" \
  -d "password=$KC_PASSWORD" \
  -d "grant_type=password" \
  -d "scope=profile" \
  "$KC_ISSUER/protocol/openid-connect/token" \
)
echo $KC_RESPONSE | jq -C .

KC_ACCESS_TOKEN=$(echo $KC_RESPONSE | jq -r .access_token)
echo $KC_ACCESS_TOKEN 
```