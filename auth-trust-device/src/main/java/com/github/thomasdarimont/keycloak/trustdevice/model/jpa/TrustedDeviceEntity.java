package com.github.thomasdarimont.keycloak.trustdevice.model.jpa;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.Objects;

@Entity
@IdClass(TrustedDeviceEntity.Key.class)
@NamedQuery(name = "findTrustedDevice", query = "" +
        "select tde " +
        "  from TrustedDeviceEntity tde " +
        " where tde.realmId = :realmId " +
        "   and tde.userId = :userId " +
        "   and tde.deviceId = :deviceId")
@NamedQuery(name = "findTrustedDevicesForUser", query = "" +
        "select tde " +
        "  from TrustedDeviceEntity tde " +
        " where tde.realmId = :realmId " +
        "   and tde.userId = :userId")
@NamedQuery(name = "deleteTrustedDevicesByUser", query = "" +
        "delete " +
        "  from TrustedDeviceEntity tde " +
        " where tde.realmId = :realmId " +
        "   and tde.userId = :userId")
@NamedQuery(name = "deleteTrustedDevicesByUserOlderThan", query = "" +
        "delete " +
        "  from TrustedDeviceEntity tde " +
        " where tde.realmId = :realmId " +
        "   and tde.userId = :userId " +
        "   and tde.createdAt < :minCreatedAt "
)
@NamedQuery(name = "deleteTrustedDeviceForUser", query = "" +
        "delete " +
        "  from TrustedDeviceEntity tde " +
        " where tde.realmId = :realmId " +
        "   and tde.userId = :userId " +
        "   and tde.deviceId = :deviceId "
)
@Table(name = "custom_trusted_device")
public class TrustedDeviceEntity {

    @Id
    @Column(name = "user_id")
    private String userId;

    @Id
    @Column(name = "realm_id")
    private String realmId;

    @Id
    @Column(name = "device_id")
    private String deviceId;

    @Column(name = "device_name")
    private String deviceName;

    @Column(name = "created_date")
    private long createdAt;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getRealmId() {
        return realmId;
    }

    public void setRealmId(String realmId) {
        this.realmId = realmId;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    @Embeddable
    public static class Key implements Serializable {

        private String userId;

        private String realmId;

        private String deviceId;

        public Key() {
            // for JPA
        }

        public Key(String realmId, String userId, String deviceId) {
            this.realmId = realmId;
            this.userId = userId;
            this.deviceId = deviceId;
        }

        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }

        public String getRealmId() {
            return realmId;
        }

        public void setRealmId(String realmId) {
            this.realmId = realmId;
        }

        public String getDeviceId() {
            return deviceId;
        }

        public void setDeviceId(String deviceId) {
            this.deviceId = deviceId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Key)) return false;
            Key key = (Key) o;
            return Objects.equals(userId, key.userId) && Objects.equals(realmId, key.realmId) && Objects.equals(deviceId, key.deviceId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(userId, realmId, deviceId);
        }

        @Override
        public String toString() {
            return "Key{" +
                    "userId='" + userId + '\'' +
                    ", realmId='" + realmId + '\'' +
                    ", deviceId='" + deviceId + '\'' +
                    '}';
        }
    }
}
