package com.github.thomasdarimont.keycloak.saml.idp;

import com.google.auto.service.AutoService;
import lombok.extern.jbosslog.JBossLog;
import org.keycloak.broker.provider.AbstractIdentityProviderMapper;
import org.keycloak.broker.provider.BrokeredIdentityContext;
import org.keycloak.broker.provider.IdentityProviderMapper;
import org.keycloak.broker.saml.SAMLEndpoint;
import org.keycloak.broker.saml.SAMLIdentityProviderFactory;
import org.keycloak.dom.saml.v2.assertion.AssertionType;
import org.keycloak.dom.saml.v2.assertion.AttributeStatementType;
import org.keycloak.dom.saml.v2.assertion.AttributeType;
import org.keycloak.models.GroupModel;
import org.keycloak.models.IdentityProviderMapperModel;
import org.keycloak.models.IdentityProviderSyncMode;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.provider.ProviderConfigProperty;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@JBossLog
@AutoService(IdentityProviderMapper.class)
public class DynamicIdpGroupMapper extends AbstractIdentityProviderMapper {

    public static final String[] COMPATIBLE_PROVIDERS = {SAMLIdentityProviderFactory.PROVIDER_ID};

    public static final String GROUP_ATTRIBUTE_NAME = "groupAttributeName";

    public static final String DEFAULT_GROUP_ATTRIBUTE = "http://schemas.microsoft.com/ws/2008/06/identity/claims/groups";

    private static final List<ProviderConfigProperty> CONFIG_PROPERTIES;

//    public static final String ATTRIBUTE_NAME = "attribute.name";
//    public static final String ATTRIBUTE_FRIENDLY_NAME = "attribute.friendly.name";
//    public static final String ATTRIBUTE_VALUE = "attribute.value";

    private static final Set<IdentityProviderSyncMode> IDENTITY_PROVIDER_SYNC_MODES = new HashSet<>(Arrays.asList(IdentityProviderSyncMode.values()));

    static {

        List<ProviderConfigProperty> configProperties = new ArrayList<>();

        ProviderConfigProperty property;
        property = new ProviderConfigProperty();
        property.setName(GROUP_ATTRIBUTE_NAME);
        property.setLabel("Group Attribute Name");
        property.setHelpText("Name of group attribute to search for in assertion.  You can leave this blank and specify a friendly name instead.");
        property.setType(ProviderConfigProperty.STRING_TYPE);
        property.setDefaultValue(DEFAULT_GROUP_ATTRIBUTE);
        configProperties.add(property);

//        property = new ProviderConfigProperty();
//        property.setName(ATTRIBUTE_FRIENDLY_NAME);
//        property.setLabel("Friendly Name");
//        property.setHelpText("Friendly name of attribute to search for in assertion.  You can leave this blank and specify a name instead.");
//        property.setType(ProviderConfigProperty.STRING_TYPE);
//        configProperties.add(property);
//        property = new ProviderConfigProperty();
//        property.setName(ATTRIBUTE_VALUE);
//        property.setLabel("Attribute Value");
//        property.setHelpText("Value the attribute must have.  If the attribute is a list, then the value must be contained in the list.");
//        property.setType(ProviderConfigProperty.STRING_TYPE);
//        configProperties.add(property);
////        property = new ProviderConfigProperty();
////        property.setName(ConfigConstants.ROLE);
////        property.setLabel("Role");
////        property.setHelpText("Role to grant to user.  Click 'Select Role' button to browse roles, or just type it in the textbox.  To reference a client role the syntax is clientname.clientrole, i.e. myclient.myrole");
////        property.setType(ProviderConfigProperty.ROLE_TYPE);
////        configProperties.add(property);


        CONFIG_PROPERTIES = Collections.unmodifiableList(configProperties);
    }

    public static final String PROVIDER_ID = "demo-saml-group-idp-mapper";

    @Override
    public boolean supportsSyncMode(IdentityProviderSyncMode syncMode) {
        return IDENTITY_PROVIDER_SYNC_MODES.contains(syncMode);
    }

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return CONFIG_PROPERTIES;
    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    @Override
    public String[] getCompatibleProviders() {
        return COMPATIBLE_PROVIDERS;
    }

    @Override
    public String getDisplayCategory() {
        return "Demo Group Mapper";
    }

    @Override
    public String getDisplayType() {
        return "Demo SAML Attribute to Group";
    }


    @Override
    public String getHelpText() {
        return "Dynamically joins the groups provided by the IdentityProvider. Non-existing groups will be created.";
    }

//    protected boolean isAttributePresent(IdentityProviderMapperModel mapperModel, BrokeredIdentityContext context) {
//        String name = mapperModel.getConfig().get(ATTRIBUTE_NAME);
//        if (name != null && name.trim().equals("")) {
//            name = null;
//        }
//        String friendly = mapperModel.getConfig().get(ATTRIBUTE_FRIENDLY_NAME);
//        if (friendly != null && friendly.trim().equals("")) {
//            friendly = null;
//        }
//        String desiredValue = Optional.ofNullable(mapperModel.getConfig().get(ATTRIBUTE_VALUE)).orElse("");
//        AssertionType assertion = (AssertionType)context.getContextData().get(SAMLEndpoint.SAML_ASSERTION);
//        for (AttributeStatementType statement : assertion.getAttributeStatements()) {
//            for (AttributeStatementType.ASTChoiceType choice : statement.getAttributes()) {
//                AttributeType attr = choice.getAttribute();
//                if (name != null && !name.equals(attr.getName())) {
//                    continue;
//                }
//                if (friendly != null && !friendly.equals(attr.getFriendlyName())) {
//                    continue;
//                }
//                for (Object val : attr.getAttributeValue()) {
//                    val = Optional.ofNullable(val).orElse("");
//                    if (val.equals(desiredValue)) {
//                        return true;
//                    }
//                }
//            }
//        }
//        return false;
//    }

    private Predicate<AttributeStatementType.ASTChoiceType> elementWith(String attributeName) {
        return attributeType -> {
            AttributeType attribute = attributeType.getAttribute();
            return Objects.equals(attribute.getName(), attributeName)
                    || Objects.equals(attribute.getFriendlyName(), attributeName);
        };
    }

    private List<String> findAttributeValuesInContext(String attributeName, BrokeredIdentityContext context) {
        AssertionType assertion = (AssertionType) context.getContextData().get(SAMLEndpoint.SAML_ASSERTION);

        return assertion.getAttributeStatements().stream()
                .flatMap(statement -> statement.getAttributes().stream())
                .filter(elementWith(attributeName))
                .flatMap(attributeType -> attributeType.getAttribute().getAttributeValue().stream())
                .filter(Objects::nonNull)
                .map(Object::toString)
                .collect(Collectors.toList());
    }

    @Override
    public void importNewUser(KeycloakSession session, RealmModel realm, UserModel user, IdentityProviderMapperModel mapperModel, BrokeredIdentityContext context) {
        createOrJoinGroups(realm, user, mapperModel, context);
    }

    @Override
    public void updateBrokeredUser(KeycloakSession session, RealmModel realm, UserModel user, IdentityProviderMapperModel mapperModel, BrokeredIdentityContext context) {
        createOrJoinGroups(realm, user, mapperModel, context);
    }


    protected void createOrJoinGroups(RealmModel realm, UserModel user, IdentityProviderMapperModel mapperModel, BrokeredIdentityContext context) {

        String groupAttributeName = mapperModel.getConfig().getOrDefault(GROUP_ATTRIBUTE_NAME, DEFAULT_GROUP_ATTRIBUTE);

        List<String> groupNames = findAttributeValuesInContext(groupAttributeName, context);

        if (groupNames == null || groupNames.isEmpty()) {
            return;
        }

        Map<String, GroupModel> realmGroupMap = new HashMap<>();
        realm.getGroupsStream().forEach(g -> realmGroupMap.put(g.getName(), g));

//        Set<String> tokenGroups = new HashSet<>();

        for (String groupName : groupNames) {
//            tokenGroups.add(groupName);

            if (realmGroupMap.containsKey(groupName)) {
                log.infof("Found group for IdP brokered user. group=%s userId=%s user=%s", groupName, user.getId(), user.getUsername());
                GroupModel group = realmGroupMap.get(groupName);
                if (!user.isMemberOf(group)) {
                    user.joinGroup(group);
                }
            } else {
//                // create new group dynamically
//                LOG.infof("Create new group for IdP brokered user. group=%s userId=%s user=%s", groupName, user.getId(), user.getUsername());
//                try {
//                    GroupModel newGroup = realm.createGroup(groupName);
//                    user.joinGroup(newGroup);
//                } catch (Exception e) {
//                    LOG.infof("Create new group throws exception. group=%s userId=%s user=%s", groupName, user.getId(), user.getUsername());
//                }

                log.infof("Failed to assign group to user. realm=%s group=%s userId=%s", realm.getId(), groupName, user.getId());
            }
        }

        // remove user from groups not provided by the IdP
//        for (GroupModel g : user.getGroups()) {
//            String gName = g.getName();
//            if (!tokenGroups.contains(gName)) {
//                user.leaveGroup(g);
//            }
//        }
    }

}
