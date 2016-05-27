package uk.gov.justice.services.persistence;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class EntityManagerProducerTest {

    @Mock
    private EntityManagerFactory entityManagerFactory;

    @Mock
    private EntityManager entityManager;

    private EntityManagerProducer entityManagerProducer;

    @Before
    public void setup() {
        entityManagerProducer = new EntityManagerProducer();
        entityManagerProducer.entityManagerFactory = entityManagerFactory;
        when(entityManagerFactory.createEntityManager()).thenReturn(entityManager);
    }

    @Test
    public void shouldProduceEntityManger() {
        EntityManager em = entityManagerProducer.create();

        assertThat(em, equalTo(entityManager));
    }

    @Test
    public void shouldCloseOpenEntityManger() {
        when(entityManager.isOpen()).thenReturn(true);

        EntityManager em = entityManagerProducer.create();
        entityManagerProducer.close(em);

        verify(em, times(1)).close();
    }

    @Test
    public void shouldNotCloseAClosedEntityManger() {
        when(entityManager.isOpen()).thenReturn(false);

        EntityManager em = entityManagerProducer.create();
        entityManagerProducer.close(em);

        verify(em, never()).close();
    }


}