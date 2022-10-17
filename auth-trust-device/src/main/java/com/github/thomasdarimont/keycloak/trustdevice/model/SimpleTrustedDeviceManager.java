package com.github.thomasdarimont.keycloak.trustdevice.model;

import com.github.thomasdarimont.keycloak.trustdevice.model.jpa.TrustedDeviceEntity;
import com.github.thomasdarimont.keycloak.trustdevice.model.jpa.TrustedDeviceRepository;
import lombok.extern.jbosslog.JBossLog;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;

@JBossLog
public class SimpleTrustedDeviceManager implements TrustedDeviceManager {

    @Override
    public TrustedDeviceModel lookupTrustedDevice(KeycloakSession session, RealmModel realm, UserModel user, String deviceId) {

        TrustedDeviceRepository repo = new TrustedDeviceRepository(session);
        TrustedDeviceEntity entity = repo.lookupTrustedDevice(realm.getId(), user.getId(), deviceId);
        if (entity == null) {
            return null;
        }

        return toModel(entity);
    }

    @Override
    public TrustedDeviceModel registerTrustedDevice(KeycloakSession session, RealmModel realm, UserModel user, String deviceId, String deviceName) {

        TrustedDeviceRepository repo = new TrustedDeviceRepository(session);
        TrustedDeviceEntity entity = repo.registerTrustedDevice(realm.getId(), user.getId(), deviceId, deviceName);

        return toModel(entity);
    }

    @Override
    public boolean removeAllTrustedDevices(KeycloakSession session, RealmModel realm, UserModel user) {

        TrustedDeviceRepository repo = new TrustedDeviceRepository(session);
        int deletedCount = repo.deleteTrustedDevicesForUser(realm.getId(), user.getId());
        boolean deleted = deletedCount > 0;
        if (deleted) {
            log.infof("Deleted trusted devices for user. realm=%s userId=%s devices=%s", realm.getId(), user.getId(), deletedCount);
        }
        return deleted;
    }

    @Override
    public boolean removeTrustedDevice(KeycloakSession session, RealmModel realm, UserModel user, String deviceId) {

        TrustedDeviceRepository repo = new TrustedDeviceRepository(session);
        int deletedCount = repo.deleteTrustedDeviceForUser(realm.getId(), user.getId(), deviceId);
        boolean deleted = deletedCount > 0;
        if (deleted) {
            log.infof("Deleted trusted device for user. realm=%s userId=%s device=%s", realm.getId(), user.getId(), deviceId);
        }
        return deleted;
    }

    protected SimpleTrustedDeviceModel toModel(TrustedDeviceEntity entity) {
        SimpleTrustedDeviceModel model = new SimpleTrustedDeviceModel();
        model.setRealmId(entity.getRealmId());
        model.setUserId(entity.getUserId());
        model.setDeviceId(entity.getDeviceId());
        model.setCreatedAt(entity.getCreatedAt());
        model.setDeviceName(entity.getDeviceName());
        return model;
    }
}
