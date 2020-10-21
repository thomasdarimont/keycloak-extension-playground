package com.github.thomasdarimont.keycloak.userstorage.flyweight;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

class AcmeUserRepository {

    public static final String ACME_ADMIN_ROLE = "acme-admin";
    public static final String ACME_USER_ROLE = "acme-user";
    private final List<AcmeUser> acmeUsers;

    private final Map<String, Set<String>> userRoles;

    public AcmeUserRepository() {
        acmeUsers = Arrays.asList(
                new AcmeUser("1", "user1", "secret", "First1", "Last1", Map.of("attribute1", List.of("value1_1")), true),
                new AcmeUser("2", "user2", "secret", "First2", "Last2", Map.of("attribute1", List.of("value1_2")), true),
                new AcmeUser("3", "user3", "secret", "First3", "Last3", Map.of("attribute1", List.of("value1_3")), true),
                new AcmeUser("4", "user4", "secret", "First4", "Last4", Map.of("attribute1", List.of("value1_4")), false)
        );

        userRoles = Map.ofEntries(
                // user roles

                Map.entry("1", Set.of(ACME_ADMIN_ROLE, ACME_USER_ROLE)),
                Map.entry("2", Set.of(ACME_ADMIN_ROLE, ACME_USER_ROLE)),
                Map.entry("3", Set.of(ACME_USER_ROLE)),
                Map.entry("4", Set.of(ACME_USER_ROLE)),

                // user client roles
                Map.entry("test-client:1", Set.of("acme-test-client-manager"))
        );
    }

    public List<AcmeUser> getAllUsers() {
        return acmeUsers;
    }

    public int getUsersCount() {
        return acmeUsers.size();
    }

    public AcmeUser findUserById(String id) {
        return acmeUsers.stream().filter(acmeUser -> acmeUser.getId().equals(id)).findFirst().get();
    }

    public AcmeUser findUserByUsernameOrEmail(String username) {
        return acmeUsers.stream()
                .filter(acmeUser -> acmeUser.getUsername().equalsIgnoreCase(username) || acmeUser.getEmail().equalsIgnoreCase(username))
                .findFirst().get();
    }

    public List<AcmeUser> findUsers(String query) {
        return acmeUsers.stream()
                .filter(acmeUser -> acmeUser.getUsername().contains(query) || acmeUser.getEmail().contains(query))
                .collect(Collectors.toList());
    }

    public boolean validateCredentials(String username, String password) {
        return findUserByUsernameOrEmail(username).getPassword().equals(password);
    }

    public boolean updateCredentials(String username, String password) {
        findUserByUsernameOrEmail(username).setPassword(password);
        return true;
    }

    public Set<String> getRoles(String username) {

        AcmeUser user = findUserByUsernameOrEmail(username);
        return getRolesByUserId(user.getId());
    }


    public Set<String> getRolesByUserId(String userId) {
        return userRoles.get(userId);
    }

    public Set<String> getClientRolesByUserId(String clientId, String userId) {
        return userRoles.get(clientId + ":" + userId);
    }
}
