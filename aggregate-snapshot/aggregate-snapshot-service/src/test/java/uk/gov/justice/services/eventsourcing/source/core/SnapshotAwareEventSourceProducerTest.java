package uk.gov.justice.services.eventsourcing.source.core;

import static java.util.Optional.of;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.eventsourcing.source.core.annotation.EventSourceName.DEFAULT_EVENT_SOURCE_NAME;
import static uk.gov.justice.services.test.utils.core.reflection.ReflectionUtil.fieldValue;

import uk.gov.justice.services.eventsourcing.repository.jdbc.EventRepository;
import uk.gov.justice.services.eventsourcing.repository.jdbc.EventRepositoryFactory;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.EventJdbcRepository;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.EventJdbcRepositoryFactory;
import uk.gov.justice.services.eventsourcing.repository.jdbc.eventstream.EventStreamJdbcRepository;
import uk.gov.justice.services.eventsourcing.repository.jdbc.eventstream.EventStreamJdbcRepositoryFactory;
import uk.gov.justice.services.eventsourcing.source.core.snapshot.SnapshotService;
import uk.gov.justice.services.jdbc.persistence.DataSourceJndiNameProvider;
import uk.gov.justice.subscription.registry.EventSourceRegistry;

import java.util.Optional;

import javax.enterprise.inject.UnsatisfiedResolutionException;
import javax.enterprise.inject.spi.InjectionPoint;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class SnapshotAwareEventSourceProducerTest {

    @Mock
    private EventSourceNameExtractor eventSourceNameExtractor;

    @Mock
    private EventSourceRegistry eventSourceRegistry;

    @Mock
    private EventStreamManagerFactory eventStreamManagerFactory;

    @Mock
    private EventRepositoryFactory eventRepositoryFactory;

    @Mock
    private EventJdbcRepositoryFactory eventJdbcRepositoryFactory;

    @Mock
    private EventStreamJdbcRepositoryFactory eventStreamJdbcRepositoryFactory;

    @Mock
    private DataSourceJndiNameProvider dataSourceJndiNameProvider;

    @Mock
    private SnapshotService snapshotService;

    @InjectMocks
    private SnapshotAwareEventSourceProducer snapshotAwareEventSourceProducer;

    @Test
    public void shouldCreateTheCorrectEventSourceAccordingToTheEventSourceName() throws Exception {

        final InjectionPoint injectionPoint = mock(InjectionPoint.class);
        final EventStreamManager eventStreamManager = mock(EventStreamManager.class);
        final EventRepository eventRepository = mock(EventRepository.class);

        when(eventSourceNameExtractor.getEventSourceNameFromQualifier(injectionPoint)).thenReturn(DEFAULT_EVENT_SOURCE_NAME);
        when(eventRepositoryFactory.eventRepository(any(EventJdbcRepository.class), any(EventStreamJdbcRepository.class))).thenReturn(eventRepository);
        when(eventStreamManagerFactory.eventStreamManager(eventRepository)).thenReturn(eventStreamManager);

        final EventSource eventSource = snapshotAwareEventSourceProducer.eventSource(injectionPoint);

        assertThat(eventSource, is(instanceOf(SnapshotAwareEventSource.class)));

        final SnapshotAwareEventSource snapshotAwareEventSource = (SnapshotAwareEventSource) eventSource;

        final Optional<Object> eventStreamManagerField = fieldValue(snapshotAwareEventSource, "eventStreamManager");
        assertThat(eventStreamManagerField, is(of(eventStreamManager)));

        final Optional<Object> snapshotServiceField = fieldValue(snapshotAwareEventSource, "snapshotService");
        assertThat(snapshotServiceField, is(of(snapshotService)));

        final Optional<Object> eventRepositoryField = fieldValue(snapshotAwareEventSource, "eventRepository");
        assertThat(eventRepositoryField, is(of(eventRepository)));
    }

    @Test
    public void shouldThrowUnsatisfiedResolutionExceptionWhenDefaultEventSourceNameNotProvided() throws Exception {

        final InjectionPoint injectionPoint = mock(InjectionPoint.class);

        when(eventSourceNameExtractor.getEventSourceNameFromQualifier(injectionPoint)).thenReturn("not-known-name");
        when(eventSourceRegistry.getEventSourceFor("not-known-name")).thenReturn(Optional.empty());

        try {
            snapshotAwareEventSourceProducer.eventSource(injectionPoint);
            fail();
        } catch (final UnsatisfiedResolutionException expected) {
            assertThat(expected.getMessage(), is("Use of non default EventSources not yet implemented"));
        }
    }
}