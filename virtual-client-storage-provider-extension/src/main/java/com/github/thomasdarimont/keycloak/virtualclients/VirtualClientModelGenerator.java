package com.github.thomasdarimont.keycloak.virtualclients;

import com.github.thomasdarimont.keycloak.virtualclients.mappers.DynamicClaimMapper;
import lombok.RequiredArgsConstructor;
import org.keycloak.common.constants.ServiceAccountConstants;
import org.keycloak.models.ClientScopeModel;
import org.keycloak.models.ProtocolMapperModel;
import org.keycloak.models.RealmModel;
import org.keycloak.models.RoleModel;
import org.keycloak.models.utils.KeycloakModelUtils;
import org.keycloak.protocol.ProtocolMapper;
import org.keycloak.protocol.oidc.OIDCLoginProtocol;
import org.keycloak.protocol.oidc.mappers.OIDCAttributeMapperHelper;
import org.keycloak.protocol.oidc.mappers.UserSessionNoteMapper;

import java.beans.Introspector;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

public class VirtualClientModelGenerator {

    private static final Set<ProtocolMapperModel> DEFAULT_PROTOCOL_MAPPERS = createDefaultProtocolMappers();

    private static Set<ProtocolMapperModel> createDefaultProtocolMappers() {

        Set<ProtocolMapperModel> mappers = new LinkedHashSet<>();

        ProtocolMapperModel clientIdMapper = UserSessionNoteMapper.createClaimMapper(
                ServiceAccountConstants.CLIENT_ID_PROTOCOL_MAPPER,
                ServiceAccountConstants.CLIENT_ID,
                ServiceAccountConstants.CLIENT_ID,
                "String",
                true,
                true);
        clientIdMapper.setId(KeycloakModelUtils.generateId());
        mappers.add(clientIdMapper);

        ProtocolMapperModel dynamicMapperModel = new ProtocolMapperModel();
        dynamicMapperModel.setName(DynamicClaimMapper.PROVIDER_ID);
        dynamicMapperModel.setId(KeycloakModelUtils.generateId());
        dynamicMapperModel.setProtocol(OIDCLoginProtocol.LOGIN_PROTOCOL);
        dynamicMapperModel.setProtocolMapper(DynamicClaimMapper.PROVIDER_ID);
        Map<String, String> config = new HashMap<>();
        config.put(OIDCAttributeMapperHelper.INCLUDE_IN_ACCESS_TOKEN, "true");
        config.put(OIDCAttributeMapperHelper.INCLUDE_IN_ID_TOKEN, "false");
        dynamicMapperModel.setConfig(config);
        mappers.add(dynamicMapperModel);

        return mappers;
    }

    VirtualClientModel createVirtualModel(String id, String clientId, RealmModel realm) {
        return createVirtualModel(id, clientId, realm, modelAttributes -> {
            modelAttributes.put("publicClient", false);
            modelAttributes.put("directAccessGrantsEnabled", true);
            modelAttributes.put("standardFlowEnabled", false);
        });
    }

    VirtualClientModel createVirtualModel(String id, String clientId, RealmModel realm, Consumer<Map<String, Object>> adjuster) {

        Map<String, Object> modelAttributes = new HashMap<>();
        modelAttributes.put("id", id);
        if (clientId == null) {
            clientId = "f:virtual:" + id.substring(id.lastIndexOf(':') + 1);
        }
        modelAttributes.put("clientId", clientId);
        modelAttributes.put("name", "virtual-" + clientId);
        modelAttributes.put("realmName", realm.getName());
        modelAttributes.put("realm", realm);
        modelAttributes.put("enabled", true);
        modelAttributes.put("protocol", OIDCLoginProtocol.LOGIN_PROTOCOL);
        modelAttributes.put("protocolMappers", DEFAULT_PROTOCOL_MAPPERS);
        modelAttributes.put("attributes", new HashMap<String, String>());
        modelAttributes.put("authFlowBindings", new HashMap<String, String>());
        modelAttributes.put("authenticationFlowBindingOverrides", new HashMap<>());
        modelAttributes.put("clientAuthenticatorType", "client-secret");
        modelAttributes.put("redirectUris", Collections.<String>emptySet());
        modelAttributes.put("webOrigins", Collections.<String>emptySet());
        modelAttributes.put("scopeMappings", Collections.<RoleModel>emptySet());
        modelAttributes.put("defaultRoles", Collections.emptyList());
        modelAttributes.put("registeredNodes", Collections.emptyMap());
        modelAttributes.put("serviceAccountsEnabled", true);
        modelAttributes.put("secret", "secret");

        adjuster.accept(modelAttributes);

        ClassLoader cl = VirtualClientModel.class.getClassLoader();
        Class[] ifaces = {VirtualClientModel.class};
        VirtualClientModelStorageHandler handler = new VirtualClientModelStorageHandler(modelAttributes);

        return VirtualClientModel.class.cast(Proxy.newProxyInstance(cl, ifaces, handler));
    }

    @RequiredArgsConstructor
    static class VirtualClientModelStorageHandler implements InvocationHandler {

        private final Map<String, Object> modelAttributes;

        private static final Method OBJECT_EQUALS =
                getObjectMethod("equals", Object.class);

        private static final Method OBJECT_HASHCODE =
                getObjectMethod("hashCode");

        private static final Method OBJECT_TOSTRING =
                getObjectMethod("toString");


        public Map<String, ClientScopeModel> getClientScopes(boolean defaultScope, boolean filterByProtocol) {
            return Collections.emptyMap();
        }

        public ClientScopeModel getDynamicClientScope(String scope) {
            return null;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

            String methodName = method.getName();

            if (OBJECT_EQUALS.equals(method)) {
                return equalsInternal(proxy, args[0]);
            }

            if (OBJECT_HASHCODE.equals(method)) {
                return modelAttributes.hashCode();
            }

            if (OBJECT_TOSTRING.equals(method)) {
                return "VirtualClient(clientId=" + modelAttributes.get("clientId") + ")";
            }

            if (methodName.equals("getClientScopes") && method.getParameterCount() == 2) {
                return getClientScopes((Boolean) args[0], (Boolean) args[1]);
            }

            if (methodName.equals("getDynamicClientScope") && method.getParameterCount() == 1) {
                return getDynamicClientScope((String) args[0]);
            }

            if (methodName.equals("getProtocolMapperByName") && method.getParameterCount() == 2) {
                return getProtocolMapperByName((String) args[0], (String) args[1]);
            }


            if (methodName.startsWith("get") && method.getParameterCount() == 0) {
                String attribute = Introspector.decapitalize(methodName.substring(3));
                if (modelAttributes.containsKey(attribute)) {
                    return modelAttributes.get(attribute);
                }
            }

            if (methodName.startsWith("is") && method.getParameterCount() == 0) {
                String attribute = Introspector.decapitalize(methodName.substring(2));
                if (modelAttributes.containsKey(attribute)) {
                    return modelAttributes.get(attribute);
                }
            }

            if (methodName.startsWith("set") && method.getParameterCount() == 1) {
                return modelAttributes.put(Introspector.decapitalize(methodName.substring(3)), args[0]);
            }

            if (Boolean.class.equals(method.getReturnType()) || boolean.class.equals(method.getReturnType())) {
                return false;
            }

            if (Integer.class.equals(method.getReturnType()) || int.class.equals(method.getReturnType())) {
                return 0;
            }

            return null;
        }

        private ProtocolMapper getProtocolMapperByName(String protocol, String name) {

            if (OIDCLoginProtocol.LOGIN_PROTOCOL.equals(protocol)) {

                if (ServiceAccountConstants.CLIENT_ID_PROTOCOL_MAPPER.equals(name)) {
                    // to indirect fulfill check for service accounts in TokenEndpoint#clientCredentialsGrant
                    return new UserSessionNoteMapper();
                }

                if (DynamicClaimMapper.PROVIDER_ID.equals(name)) {
                    return new DynamicClaimMapper();
                }
            }

            return null;
        }

        private boolean equalsInternal(Object me, Object other) {
            if (other == null) {
                return false;
            }
            if (other.getClass() != me.getClass()) {
                return false;
            }
            InvocationHandler handler = Proxy.getInvocationHandler(other);
            if (!(handler instanceof VirtualClientModelStorageHandler)) {
                return false;
            }
            return ((VirtualClientModelStorageHandler) handler).modelAttributes.equals(modelAttributes);
        }

        private static Method getObjectMethod(String name, Class... types) {
            try {
                return Object.class.getMethod(name, types);
            } catch (NoSuchMethodException e) {
                throw new IllegalArgumentException(e);
            }
        }
    }
}
