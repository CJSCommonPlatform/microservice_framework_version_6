package uk.gov.justice.services.persistence;

import javax.enterprise.inject.Disposes;
import javax.enterprise.inject.Produces;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;

/**
 * Producer of {:link EntityManager} for use with JPA (Delta-spike).
 */
public class EntityManagerProducer {

    @PersistenceUnit
    EntityManagerFactory entityManagerFactory;

    @Produces
    public EntityManager create() {
        return entityManagerFactory.createEntityManager();
    }

    public void close(@Disposes final EntityManager em) {
        if (em.isOpen()) {
            em.close();
        }
    }
}