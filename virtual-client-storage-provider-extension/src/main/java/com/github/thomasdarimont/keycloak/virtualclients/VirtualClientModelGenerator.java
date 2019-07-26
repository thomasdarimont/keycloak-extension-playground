package com.github.thomasdarimont.keycloak.virtualclients;

import lombok.RequiredArgsConstructor;
import org.keycloak.models.ClientScopeModel;
import org.keycloak.models.ProtocolMapperModel;
import org.keycloak.models.RoleModel;
import org.keycloak.models.utils.KeycloakModelUtils;
import org.keycloak.protocol.oidc.OIDCLoginProtocol;
import org.keycloak.protocol.oidc.mappers.OIDCAttributeMapperHelper;
import org.keycloak.protocol.oidc.mappers.UserPropertyMapper;

import java.beans.Introspector;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

public class VirtualClientModelGenerator {

    private static final Set<ProtocolMapperModel> DEFAULT_PROTOCOL_MAPPERS = createDefaultProtocolMappers();

    private static Set<ProtocolMapperModel> createDefaultProtocolMappers() {

        Set<ProtocolMapperModel> mappers = new HashSet<>();

        ProtocolMapperModel usernameMapper = OIDCAttributeMapperHelper.createClaimMapper("username", "username", "preferred_username", "string", true, true, UserPropertyMapper.PROVIDER_ID);
        usernameMapper.setId(KeycloakModelUtils.generateId());

        mappers.add(usernameMapper);
        return mappers;
    }

    VirtualClientModel createVirtualModel(String clientId, String realmName) {
        return createVirtualModel(clientId, realmName, modelAttributes -> {
            modelAttributes.put("publicClient", false);
            modelAttributes.put("directAccessGrantsEnabled", true);
            modelAttributes.put("standardFlowEnabled", false);
        });
    }

    VirtualClientModel createVirtualModel(String clientId, String realmName, Consumer<Map<String, Object>> adjuster) {

        Map<String, Object> modelAttributes = new HashMap<>();
        modelAttributes.put("id", clientId);
        modelAttributes.put("clientId", clientId);
        modelAttributes.put("name", "virtual-" + clientId);
        modelAttributes.put("realmName", realmName);
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
        modelAttributes.put("secret", clientId);

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
