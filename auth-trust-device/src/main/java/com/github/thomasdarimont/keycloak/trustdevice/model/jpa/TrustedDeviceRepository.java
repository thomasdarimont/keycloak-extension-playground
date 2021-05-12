package com.github.thomasdarimont.keycloak.trustdevice.model.jpa;

import org.keycloak.common.util.Time;
import org.keycloak.connections.jpa.JpaConnectionProvider;
import org.keycloak.models.KeycloakSession;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Query;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class TrustedDeviceRepository {

    private final KeycloakSession session;

    public TrustedDeviceRepository(KeycloakSession session) {
        this.session = session;
    }

    public void registerTrustedDevice(String realmId, String userId, String deviceId, String deviceName) {

        Objects.requireNonNull(realmId, "realmId");
        Objects.requireNonNull(deviceId, "deviceId");
        Objects.requireNonNull(deviceId, "deviceId");
        Objects.requireNonNull(deviceName, "deviceName");

        JpaConnectionProvider jpa = session.getProvider(JpaConnectionProvider.class);

        EntityManagerFactory emf = jpa.getEntityManager().getEntityManagerFactory();

        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            TrustedDeviceEntity tde = new TrustedDeviceEntity();
            tde.setRealmId(realmId);
            tde.setUserId(userId);
            tde.setDeviceId(deviceId);
            tde.setDeviceName(deviceName);
            tde.setCreatedAt(Time.currentTime());

            em.persist(tde);
            tx.commit();
        } finally {
            em.close();
        }
    }

    public List<TrustedDeviceEntity> findByUser(String realmId, String userId) {

        if (realmId == null || userId == null) {
            return Collections.emptyList();
        }

        JpaConnectionProvider jpa = session.getProvider(JpaConnectionProvider.class);

        EntityManager em = jpa.getEntityManager();
        Query query = em.createNamedQuery("findTrustedDevicesForUser", TrustedDeviceEntity.class);
        query.setParameter("realmId", realmId);
        query.setParameter("userId", userId);
        List<TrustedDeviceEntity> result = (List<TrustedDeviceEntity>) query.getResultList().stream().collect(Collectors.toList());
        return result;
    }

    public int deleteTrustedDevicesForUser(String realmId, String userId) {

        if (realmId == null || userId == null) {
            return 0;
        }

        JpaConnectionProvider jpa = session.getProvider(JpaConnectionProvider.class);
        EntityManagerFactory emf = jpa.getEntityManager().getEntityManagerFactory();
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        int result = -1;
        try {
            tx.begin();
            Query query = em.createNamedQuery("deleteTrustedDevicesByUser");
            query.setParameter("realmId", realmId);
            query.setParameter("userId", userId);
            result = query.executeUpdate();
            tx.commit();
        } finally {
            em.close();
        }

        return result;
    }

    public int deleteTrustedDeviceForUser(String realmId, String userId, String deviceId) {

        if (realmId == null || userId == null || deviceId == null) {
            return 0;
        }

        JpaConnectionProvider jpa = session.getProvider(JpaConnectionProvider.class);
        EntityManagerFactory emf = jpa.getEntityManager().getEntityManagerFactory();
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        int result = -1;
        try {
            tx.begin();
            Query query = em.createNamedQuery("deleteTrustedDeviceForUser");
            query.setParameter("realmId", realmId);
            query.setParameter("userId", userId);
            query.setParameter("deviceId", deviceId);

            result = query.executeUpdate();
            tx.commit();
        } finally {
            em.close();
        }

        return result;
    }

    public int deleteTrustedDevicesForUser(String realmId, String userId, int minCreatedAt) {

        if (realmId == null || userId == null) {
            return 0;
        }

        JpaConnectionProvider jpa = session.getProvider(JpaConnectionProvider.class);
        EntityManagerFactory emf = jpa.getEntityManager().getEntityManagerFactory();
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        int result = -1;
        try {
            tx.begin();
            Query query = em.createNamedQuery("deleteTrustedDevicesByUserOlderThan");
            query.setParameter("realmId", realmId);
            query.setParameter("userId", userId);
            query.setParameter("minCreatedAt", minCreatedAt);
            result = query.executeUpdate();
            tx.commit();
        } finally {
            em.close();
        }

        return result;
    }

    public TrustedDeviceEntity lookupTrustedDevice(String realmId, String userId, String deviceId) {

        if (realmId == null || userId == null || deviceId == null) {
            return null;
        }

        JpaConnectionProvider jpa = session.getProvider(JpaConnectionProvider.class);

        EntityManager em = jpa.getEntityManager();
        Query query = em.createNamedQuery("findTrustedDevice", TrustedDeviceEntity.class);
        query.setParameter("realmId", realmId);
        query.setParameter("userId", userId);
        query.setParameter("deviceId", deviceId);

        query.setMaxResults(1);
        List resultList = query.getResultList();
        if (resultList.isEmpty()) {
            return null;
        }
        return (TrustedDeviceEntity) resultList.get(0);
    }
}
