package uk.gov.justice.services.eventsourcing.source.core;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.eventsourcing.source.core.annotation.EventSourceName.DEFAULT_EVENT_SOURCE_NAME;


import uk.gov.justice.services.common.configuration.GlobalValueProducer;
import uk.gov.justice.services.common.converter.ObjectToJsonValueConverter;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.justice.services.common.util.Clock;
import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.justice.services.core.cdi.LoggerProducer;
import uk.gov.justice.services.core.enveloper.DefaultEnveloper;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.eventsource.DefaultEventDestinationResolver;
import uk.gov.justice.services.eventsourcing.publisher.jms.EventPublisher;
import uk.gov.justice.services.eventsourcing.repository.jdbc.AnsiSQLEventLogInsertionStrategy;
import uk.gov.justice.services.eventsourcing.repository.jdbc.DefaultEventRepository;
import uk.gov.justice.services.eventsourcing.repository.jdbc.EventInsertionStrategy;
import uk.gov.justice.services.eventsourcing.repository.jdbc.EventRepository;
import uk.gov.justice.services.eventsourcing.repository.jdbc.PostgresSQLEventLogInsertionStrategy;
import uk.gov.justice.services.messaging.DefaultJsonObjectEnvelopeConverter;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.JsonObjectEnvelopeConverter;
import uk.gov.justice.services.messaging.jms.DefaultEnvelopeConverter;
import uk.gov.justice.services.messaging.jms.DefaultJmsEnvelopeSender;
import uk.gov.justice.services.messaging.jms.EnvelopeConverter;
import uk.gov.justice.services.messaging.jms.JmsEnvelopeSender;
import uk.gov.justice.services.messaging.logging.DefaultTraceLogger;
import uk.gov.justice.services.messaging.logging.TraceLogger;
import uk.gov.justice.services.test.utils.common.helper.StoppedClock;
import uk.gov.justice.subscription.ParserProducer;
import uk.gov.justice.subscription.YamlFileFinder;
import uk.gov.justice.subscription.registry.EventSourceRegistryProducer;
import uk.gov.justice.subscription.yaml.parser.YamlParser;
import uk.gov.justice.subscription.yaml.parser.YamlSchemaLoader;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.UnsatisfiedResolutionException;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.inject.Inject;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.jglue.cdiunit.AdditionalClasses;
import org.jglue.cdiunit.CdiRunner;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.slf4j.Logger;

@RunWith(CdiRunner.class)
@AdditionalClasses(value = {
        EventStreamManager.class,
        GlobalValueProducer.class,
        SystemEventService.class,
        Clock.class,
        StoppedClock.class,
        UtcClock.class,
        Enveloper.class,
        DefaultEnveloper.class,
        ObjectToJsonValueConverter.class,
        EventAppender.class,
        PublishingEventAppender.class,
        ObjectMapper.class,
        ObjectMapperProducer.class,
        EventRepository.class,
        DefaultEventRepository.class,
        JsonObjectEnvelopeConverter.class,
        DefaultJsonObjectEnvelopeConverter.class,
        EventSourceProducerTest.DummyJmsEventPublisher.class,
        EventSourceProducerTest.TestEventInsertionStrategyProducer.class,
        PostgresSQLEventLogInsertionStrategy.class,
        Logger.class,
        TraceLogger.class,
        DefaultTraceLogger.class,
        LoggerProducer.class,
        DefaultEventDestinationResolver.class,
        JmsEnvelopeSender.class,
        DefaultJmsEnvelopeSender.class,
        EnvelopeConverter.class,
        DefaultEnvelopeConverter.class,
        EventSourceRegistryProducer.class,
        ParserProducer.class,
        YamlFileFinder.class,
        YamlParser.class,
        YamlSchemaLoader.class
})
public class EventSourceProducerTest {

    @Produces
    @Mock
    private EventSourceNameExtractor eventSourceNameExtractor;

    @Inject
    private EventSourceProducer eventSourceProducer;

    @Test
    public void shouldCreateTheCorrectEventSourceAccordingToTheEventSourceName() throws Exception {

        final InjectionPoint injectionPoint = mock(InjectionPoint.class);

        when(eventSourceNameExtractor.getEventSourceNameFromQualifier(injectionPoint)).thenReturn(DEFAULT_EVENT_SOURCE_NAME);

        assertThat(eventSourceProducer.eventSource(injectionPoint), is(instanceOf(JdbcBasedEventSource.class)));
    }

    @Test
    public void shouldThrowUnsatisfiedResolutionExceptionWhenDefaultEventSourceNameNotProvided() throws Exception {
        try {
            final InjectionPoint injectionPoint = mock(InjectionPoint.class);

            when(eventSourceNameExtractor.getEventSourceNameFromQualifier(injectionPoint)).thenReturn("not-known-name");
            eventSourceProducer.eventSource(injectionPoint);
            fail();
        } catch (final UnsatisfiedResolutionException expected) {
            assertThat(expected.getMessage(), is("Use of non default EventSources not yet implemented"));
        }
    }

    @ApplicationScoped
    public static class TestEventInsertionStrategyProducer {

        @Produces
        public EventInsertionStrategy eventLogInsertionStrategy() {
            return new AnsiSQLEventLogInsertionStrategy();
        }
    }

    public static class DummyJmsEventPublisher implements EventPublisher {

        @Override
        public void publish(final JsonEnvelope envelope) {

        }

    }
}


