package uk.gov.justice.services.eventsourcing.source.core;

import static com.google.common.collect.ImmutableSet.of;
import static java.util.Collections.emptySet;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.eventsourcing.source.core.annotation.EventSourceName.DEFAULT_EVENT_SOURCE_NAME;

import uk.gov.justice.services.eventsourcing.source.core.annotation.EventSourceName;
import uk.gov.justice.services.eventsourcing.source.core.dummies.DummyCommandHandlerWithDefaultEventSource;
import uk.gov.justice.services.eventsourcing.source.core.dummies.DummyCommandHandlerWithNamedEventSource;

import javax.enterprise.inject.spi.InjectionPoint;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class EventSourceNameExtractorTest {

    @InjectMocks
    private EventSourceNameExtractor eventSourceNameExtractor;

    @Test
    public void shouldExtractTheEventSourceNameFromANamedEventSourceAnnotation() throws Exception {

        final EventSourceName eventSourceName = DummyCommandHandlerWithNamedEventSource.class.getDeclaredAnnotation(EventSourceName.class);

        final InjectionPoint injectionPoint = mock(InjectionPoint.class);

        when(injectionPoint.getQualifiers()).thenReturn(of(eventSourceName));

        assertThat(eventSourceNameExtractor.getEventSourceNameFromQualifier(injectionPoint), is("my-event-source"));
    }

    @Test
    public void shouldExtractTheEventSourceNameFromAnEventSourceAnnotationWithDefautName() throws Exception {

        final EventSourceName eventSourceName = DummyCommandHandlerWithDefaultEventSource.class.getDeclaredAnnotation(EventSourceName.class);

        final InjectionPoint injectionPoint = mock(InjectionPoint.class);

        when(injectionPoint.getQualifiers()).thenReturn(of(eventSourceName));

        assertThat(eventSourceNameExtractor.getEventSourceNameFromQualifier(injectionPoint), is(DEFAULT_EVENT_SOURCE_NAME));
    }

    @Test
    public void shouldReturnTheDefaultEventSourceNameFromAnClassWithNoEventSourceAnnotation() throws Exception {

        final InjectionPoint injectionPoint = mock(InjectionPoint.class);

        when(injectionPoint.getQualifiers()).thenReturn(emptySet());

        assertThat(eventSourceNameExtractor.getEventSourceNameFromQualifier(injectionPoint), is(DEFAULT_EVENT_SOURCE_NAME));
    }
}
