package com.github.thomasdarimont.keycloak.userstorage.flyweight;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.keycloak.models.ClientModel;
import org.keycloak.models.RoleContainerModel;
import org.keycloak.models.RoleModel;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
public class AcmeRoleModel implements RoleModel {

    private String id;

    private String name;

    private String description;

    private boolean composite;

    private RoleContainerModel roleContainer;

    public AcmeRoleModel(String id, String name, String description, boolean composite, RoleContainerModel roleContainer) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.composite = composite;
        this.roleContainer = roleContainer;
    }

    @Override
    public void addCompositeRole(RoleModel role) {
        throw new UnsupportedOperationException("addCompositeRole");
    }

    @Override
    public void removeCompositeRole(RoleModel role) {
        throw new UnsupportedOperationException("removeCompositeRole");
    }

    @Override
    public Set<RoleModel> getComposites() {
        return Collections.emptySet();
    }

    @Override
    public boolean isClientRole() {
        return roleContainer instanceof ClientModel;
    }

    @Override
    public String getContainerId() {
        return roleContainer.getId();
    }

    @Override
    public RoleContainerModel getContainer() {
        return roleContainer;
    }

    @Override
    public boolean hasRole(RoleModel role) {
        return false;
    }

    @Override
    public void setSingleAttribute(String name, String value) {
        throw new UnsupportedOperationException("setSingleAttribute");
    }

    @Override
    public void setAttribute(String name, Collection<String> values) {
        throw new UnsupportedOperationException("setAttribute");
    }

    @Override
    public void removeAttribute(String name) {
        throw new UnsupportedOperationException("removeAttribute");
    }

    @Override
    public String getFirstAttribute(String name) {
        return null;
    }

    @Override
    public List<String> getAttribute(String name) {
        return null;
    }

    @Override
    public Map<String, List<String>> getAttributes() {
        return Collections.emptyMap();
    }
}
