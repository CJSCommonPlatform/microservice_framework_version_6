package uk.gov.justice.services.eventsourcing.source.core;

import static com.google.common.collect.ImmutableSet.of;
import static java.util.Optional.empty;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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
import uk.gov.justice.services.eventsourcing.source.core.annotation.EventSourceName;
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

import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.inject.Inject;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.dbcp2.BasicDataSource;
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
public class DataSourceProducerTest {

    @Produces
    @Mock
    private InitialContextProducer initialContextProducer;

    @Inject
    private DataSourceProducer dataSourceProducer;

    @Test
    public void shouldCreateDataSourceForEventSourceWithDataSourceURL() throws Exception {
        final InjectionPoint injectionPoint = mock(InjectionPoint.class);
        final EventSourceName eventSourceName = DummyEventSource3.class.getDeclaredAnnotation(EventSourceName.class);

        when(injectionPoint.getQualifiers()).thenReturn(of(eventSourceName));

        final InitialContext initialContext = new InitialContext();
        initialContext.bind("java:/app/test/DS.eventstore", new BasicDataSource());

        when(initialContextProducer.getInitialContext()).thenReturn(initialContext);

        final Optional<DataSource> actual = dataSourceProducer.dataSource(injectionPoint);

        assertThat(actual.get(), is(instanceOf(DataSource.class)));
    }

    @Test
    public void shouldNotCreateDataSourceForEventSourceWithNoDataSourceURL() throws Exception {
        final InjectionPoint injectionPoint = mock(InjectionPoint.class);
        final EventSourceName eventSourceName = DummyEventSource.class.getDeclaredAnnotation(EventSourceName.class);

        when(injectionPoint.getQualifiers()).thenReturn(of(eventSourceName));

        final Optional<DataSource> actual = dataSourceProducer.dataSource(injectionPoint);

        assertThat(actual, is(empty()));
    }

    @Test
    public void shouldNotCreateDataSourceForEventSourceWithIncorrectDataSourceURL() throws Exception {
        try {
            final InjectionPoint injectionPoint = mock(InjectionPoint.class);
            final InitialContext initialContextMock = mock(InitialContext.class);
            final EventSourceName eventSourceName = DummyEventSource2.class.getDeclaredAnnotation(EventSourceName.class);

            when(injectionPoint.getQualifiers()).thenReturn(of(eventSourceName));
            when(initialContextProducer.getInitialContext()).thenReturn(initialContextMock);
            when(initialContextMock.lookup("jdbc:h2:mem:test1;MV_STORE=FALSE;MVCC=FALSE")).thenThrow(new NamingException());

            dataSourceProducer.dataSource(injectionPoint);
            fail();
        } catch (DataSourceProducerException expected) {
            assertThat(expected.getMessage(), is("Data Source not found for the provided event source name: incorrectdatasourcename"));
        }
    }

    @Test
    public void shouldNotCreateDataSourceForIncorrectEventSourceName() throws Exception {
        try {

            final InjectionPoint injectionPoint = mock(InjectionPoint.class);
            final EventSourceName eventSourceName = DummyEventSource1.class.getDeclaredAnnotation(EventSourceName.class);

            when(injectionPoint.getQualifiers()).thenReturn(of(eventSourceName));

            dataSourceProducer.dataSource(injectionPoint);
            fail();
        } catch (DataSourceProducerException e) {
            assertThat(e.getMessage(), is("Expected Event Source of name: nonexitenteventsourcename not found, please check event-sources.yaml"));
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

@EventSourceName("nodatasource")
class DummyEventSource {
}

@EventSourceName("nonexitenteventsourcename")
class DummyEventSource1 {
}

@EventSourceName("incorrectdatasourcename")
class DummyEventSource2 {
}

@EventSourceName("correctdatasource")
class DummyEventSource3 {
}






