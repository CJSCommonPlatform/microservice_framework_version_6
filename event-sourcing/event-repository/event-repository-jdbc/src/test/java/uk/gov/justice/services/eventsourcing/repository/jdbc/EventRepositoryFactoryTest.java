package uk.gov.justice.services.eventsourcing.repository.jdbc;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static uk.gov.justice.services.test.utils.core.reflection.ReflectionUtil.fieldValue;

import uk.gov.justice.services.eventsourcing.repository.jdbc.event.EventConverter;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.EventJdbcRepository;
import uk.gov.justice.services.eventsourcing.repository.jdbc.eventstream.EventStreamJdbcRepository;

import java.util.Optional;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class EventRepositoryFactoryTest {

    @Mock
    private EventConverter eventConverter;

    @InjectMocks
    private EventRepositoryFactory eventRepositoryFactory;

    @Test
    public void shouldProduceEventStreamManager() throws Exception {
        final EventJdbcRepository eventJdbcRepository = mock(EventJdbcRepository.class);
        final EventStreamJdbcRepository eventStreamJdbcRepository = mock(EventStreamJdbcRepository.class);

        final EventRepository eventRepository = eventRepositoryFactory.eventRepository(eventJdbcRepository, eventStreamJdbcRepository);

        assertThat(eventRepository, is(notNullValue()));

        final Optional<Object> eventJdbcRepositoryField = fieldValue(eventRepository, "eventJdbcRepository");
        assertThat(eventJdbcRepositoryField, is(Optional.of(eventJdbcRepository)));

        final Optional<Object> eventStreamJdbcRepositoryField = fieldValue(eventRepository, "eventStreamJdbcRepository");
        assertThat(eventStreamJdbcRepositoryField, is(Optional.of(eventStreamJdbcRepository)));

        final Optional<Object> eventConverterField = fieldValue(eventRepository, "eventConverter");
        assertThat(eventConverterField, is(Optional.of(eventConverter)));

        final Optional<Object> loggerField = fieldValue(eventRepository, "logger");
        assertThat(loggerField.isPresent(), is(true));
    }
}
