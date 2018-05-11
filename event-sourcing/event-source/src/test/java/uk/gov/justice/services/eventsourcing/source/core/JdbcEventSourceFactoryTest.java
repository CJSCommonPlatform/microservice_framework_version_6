package uk.gov.justice.services.eventsourcing.source.core;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertThat;

import org.mockito.InjectMocks;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.eventsourcing.repository.jdbc.EventRepository;
import uk.gov.justice.services.eventsourcing.repository.jdbc.EventRepositoryFactory;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.EventJdbcRepository;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.EventJdbcRepositoryFactory;
import uk.gov.justice.services.eventsourcing.repository.jdbc.eventstream.EventStreamJdbcRepository;
import uk.gov.justice.services.eventsourcing.repository.jdbc.eventstream.EventStreamJdbcRepositoryFactory;
import uk.gov.justice.services.test.utils.core.reflection.ReflectionUtil;

import java.util.Optional;


@RunWith(MockitoJUnitRunner.class)
public class JdbcEventSourceFactoryTest {

    @Mock
    EventStreamManagerFactory eventStreamManagerFactory;

    @Mock
    EventRepositoryFactory eventRepositoryFactory;

    @Mock
    EventJdbcRepositoryFactory eventJdbcRepositoryFactory;

    @Mock
    EventStreamJdbcRepositoryFactory eventStreamJdbcRepositoryFactory;

    @InjectMocks
    private JdbcEventSourceFactory jdbcEventSourceFactory;

    @Test
    public void shouldCreateJdbcBasedEventSource() throws Exception {

        final String jndiDatasource = "jndiDatasource";

        final EventJdbcRepository eventJdbcRepository = mock(EventJdbcRepository.class);
        final EventStreamJdbcRepository eventStreamJdbcRepository = mock(EventStreamJdbcRepository.class);
        final EventRepository eventRepository = mock(EventRepository.class);
        final EventStreamManager eventStreamManager = mock(EventStreamManager.class);

        when(eventJdbcRepositoryFactory.eventJdbcRepository(jndiDatasource)).thenReturn(eventJdbcRepository);
        when(eventStreamJdbcRepositoryFactory.eventStreamJdbcRepository(jndiDatasource)).thenReturn(eventStreamJdbcRepository);

        when(eventRepositoryFactory.eventRepository(
                eventJdbcRepository,
                eventStreamJdbcRepository)).thenReturn(eventRepository);

        when(eventStreamManagerFactory.eventStreamManager(eventRepository)).thenReturn(eventStreamManager);

        final JdbcBasedEventSource jdbcBasedEventSource = jdbcEventSourceFactory.create(jndiDatasource);


        assertThat(fieldValueAs(jdbcBasedEventSource, "eventStreamManager", EventStreamManager.class), is(eventStreamManager));
        assertThat(fieldValueAs(jdbcBasedEventSource, "eventRepository", EventRepository.class), is(eventRepository));
    }

    private <T> T fieldValueAs(final Object object, final String fieldName, final Class<T> type) throws Exception {
        final Optional<T> optional = ReflectionUtil.fieldValueAs(object, fieldName, type);

        return optional.get();
    }
}
