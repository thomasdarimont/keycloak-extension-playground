Virtual Clients Storage Provider
---

# Obtaining an Access Token for a User with a virtual client
```
KC_CLIENT_ID=f:virtual:client99
KC_CLIENT_SECRET=secret
KC_USERNAME=tester
KC_PASSWORD=test
KC_ISSUER=http://localhost:8081/auth/realms/virtual-clients

KC_RESPONSE=$( \
curl \
  -d "client_id=$KC_CLIENT_ID" \
  -d "client_secret=$KC_CLIENT_SECRET" \
  -d "username=$KC_USERNAME" \
  -d "password=$KC_PASSWORD" \
  -d "grant_type=password" \
  "$KC_ISSUER/protocol/openid-connect/token" \
)
echo $KC_RESPONSE | jq -C .

KC_ACCESS_TOKEN=$(echo $KC_RESPONSE | jq -r .access_token)
echo $KC_ACCESS_TOKEN 
```

# Obtaining an Access Token for a service-account with a virtual client
```

KC_CLIENT_ID=f:virtual:client99
KC_CLIENT_SECRET=secret
KC_ISSUER=http://localhost:8081/auth/realms/virtual-clients


KC_RESPONSE=$( \
curl \
  -d "client_id=$KC_CLIENT_ID" \
  -d "client_secret=$KC_CLIENT_SECRET" \
  -d "grant_type=client_credentials" \
  "$KC_ISSUER/protocol/openid-connect/token" \
)

echo $KC_RESPONSE | jq -C .

KC_ACCESS_TOKEN=$(echo $KC_RESPONSE | jq -r .access_token)
echo $KC_ACCESS_TOKEN 
```
