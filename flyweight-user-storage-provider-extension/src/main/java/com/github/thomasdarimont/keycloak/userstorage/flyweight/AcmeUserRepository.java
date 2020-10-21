package com.github.thomasdarimont.keycloak.userstorage.flyweight;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

class AcmeUserRepository {

    public static final AcmeRole ACME_ADMIN_ROLE = new AcmeRole("1", "acme-admin", "Acme Admin Role");
    public static final AcmeRole ACME_USER_ROLE = new AcmeRole("2", "acme-user", "Acme User Role");
    public static final AcmeRole ACME_CLIENT_ROLE_TEST_CLIENT_MANAGER = new AcmeRole("1001", "acme-test-client-manager", "Acme Test Client Manager Role");

    private final List<AcmeUser> acmeUsers;

    private final Map<String, Set<AcmeRole>> userRoles;

    public AcmeUserRepository() {
        acmeUsers = List.of(
                new AcmeUser("1", "user1", "secret", "First1", "Last1",
                        Map.of("attribute1", List.of("value1_1")), true),
                new AcmeUser("2", "user2", "secret", "First2", "Last2",
                        Map.of("attribute1", List.of("value1_2")), true),
                new AcmeUser("3", "user3", "secret", "First3", "Last3",
                        Map.of("attribute1", List.of("value1_3")), true),
                new AcmeUser("4", "user4", "secret", "First4", "Last4",
                        Map.of("attribute1", List.of("value1_4")), false)
        );

        userRoles = Map.ofEntries(
                // global user roles
                Map.entry("1", Set.of(ACME_ADMIN_ROLE, ACME_USER_ROLE)),
                Map.entry("2", Set.of(ACME_ADMIN_ROLE, ACME_USER_ROLE)),
                Map.entry("3", Set.of(ACME_USER_ROLE)),
                Map.entry("4", Set.of(ACME_USER_ROLE)),

                // client user roles
                Map.entry("test-client:1", Set.of(ACME_CLIENT_ROLE_TEST_CLIENT_MANAGER))
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

    public Set<AcmeRole> getRoles(String username) {

        AcmeUser user = findUserByUsernameOrEmail(username);
        return getGlobalRolesByUserId(user.getId());
    }


    public Set<AcmeRole> getGlobalRolesByUserId(String userId) {
        return userRoles.get(userId);
    }

    public Set<AcmeRole> getClientRolesByUserId(String clientId, String userId) {
        return userRoles.get(clientId + ":" + userId);
    }
}
