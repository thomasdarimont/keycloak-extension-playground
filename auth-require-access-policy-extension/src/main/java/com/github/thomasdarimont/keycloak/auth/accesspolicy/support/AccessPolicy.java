package com.github.thomasdarimont.keycloak.auth.accesspolicy.support;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.jbosslog.JBossLog;
import org.keycloak.models.ClientModel;
import org.keycloak.models.RealmModel;
import org.keycloak.models.RoleModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.utils.RoleUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Getter
@Setter
@JBossLog
public class AccessPolicy {

    public static AccessPolicy EMPTY = new AccessPolicy();

    private static Set<String> NONE = Collections.singleton("NONE");

    @JsonProperty("v")
    private long version;

    @JsonProperty("p")
    private List<AccessPolicyEntry> entries = new ArrayList<>();

    public Set<String> getAllowedRoleNames(String clientId) {

        Set<AccessPolicyEntry> matchingPolicyEntries = entries.stream()
                .filter(e -> isApplicableTo(e, clientId))
                .collect(Collectors.toSet());

        if (matchingPolicyEntries.isEmpty()) {
            return Collections.emptySet();
        }

        Set<String> allowedRoleNames = matchingPolicyEntries.stream()
                .map(e -> expandAllowedRoles(e, clientId))
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());

        return allowedRoleNames;
    }

    private Collection<String> expandAllowedRoles(AccessPolicyEntry entry, String clientId) {

        String appPattern = entry.getApp();
        if (!appPattern.contains("(")) {
            return entry.getAllowedRoles();
        }

        // { "app": "(app-domain-(a|b|c))", "allow": ["$1.access"] }
        // -> app-domain-a -> allow [app-domain-a.access]
        Pattern pattern = Pattern.compile(appPattern);
        Matcher matcher = pattern.matcher(clientId);
        if (matcher.matches()) {
            Set<String> expandedRequiredClientRoles = entry.getAllowedRoles().stream()
                    .map(matcher::replaceAll)
                    .collect(Collectors.toSet());
            return expandedRequiredClientRoles;
        }

        return entry.getAllowedRoles();
    }

    public boolean isApplicableTo(AccessPolicyEntry entry, String clientId) {
        return entry.isApplicableTo(clientId);
    }

    public boolean hasAccess(RealmModel realm, UserModel user, ClientModel client) {

        Set<String> rolesAllowedForClient = getAllowedRoleNames(client.getClientId());

        // no access policy for client configured -> skip check and grant access
        if (rolesAllowedForClient == null || rolesAllowedForClient.isEmpty()) {
            log.debugf("No explicit access-policy for client configured: allow access. realm=%s client=%s user=%s", realm.getName(), client.getClientId(), user.getId());
            return true;
        }

        log.debugf("Checking access to client. realm=%s client=%s user=%s", realm.getName(), client.getClientId(), user.getId());

        // explicit access policy with NONE roles defined -> always deny access
        if (NONE.equals(rolesAllowedForClient)) {
            log.debugf("Access to client denied since no roles are allowed to access. realm=%s client=%s user=%s", realm.getName(), client.getClientId(), user.getId());
            return false;
        }

        Set<String> clientRolesAllowed = rolesAllowedForClient.stream().filter(s -> s.contains(".")).collect(Collectors.toSet());
        Set<String> realmRolesAllowed = new HashSet<>(rolesAllowedForClient);
        realmRolesAllowed.removeAll(clientRolesAllowed);

        // Check for realmRole
        Set<RoleModel> expandedRealmRoleMappings = RoleUtils.expandCompositeRoles(user.getRealmRoleMappings());

        if (!realmRolesAllowed.isEmpty()) {

            Set<String> realmRolesUser = expandedRealmRoleMappings.stream().map(RoleModel::getName).collect(Collectors.toSet());
            boolean userHasRequiredRealmRole = realmRolesAllowed.stream().anyMatch(realmRolesUser::contains);

            if (userHasRequiredRealmRole) {
                // required realm role present -> grant access
                log.debugf("Allow access to client: required realm role present. realm=%s client=%s user=%s", realm.getName(), client.getClientId(), user.getId());
                return true;
            }
        }

        // Check for clientId.clientRole
        if (!clientRolesAllowed.isEmpty()) {

            Set<RoleModel> expandedClientRoleMappings = expandedRealmRoleMappings.stream()
                    .filter(RoleModel::isClientRole)
                    .filter(r -> r.getContainerId().equals(client.getId()))
                    .collect(Collectors.toSet());

            expandedClientRoleMappings.addAll(user.getClientRoleMappings(client));

            Set<String> clientRolesUser = expandedClientRoleMappings.stream()
                    .map(r -> client.getClientId() + "." + r.getName())
                    .collect(Collectors.toSet());

            boolean userHasRequiredClientRole = clientRolesAllowed.stream().anyMatch(clientRolesUser::contains);

            if (userHasRequiredClientRole) {
                // required client role present -> grant access
                log.debugf("Allow access to client: required client role present. realm=%s client=%s user=%s", realm.getName(), client.getClientId(), user.getId());
                return true;
            }
        }

        log.debugf("Access to client denied due to missing required roles. realm=%s client=%s user=%s", realm.getName(), client.getClientId(), user.getId());

        return false;
    }
}
