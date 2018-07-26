package uk.gov.justice.services.eventsourcing.publishing;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static uk.gov.justice.services.core.postgres.OpenEjbConfigurationBuilder.createOpenEjbConfigurationBuilder;

import uk.gov.justice.services.common.converter.StringToJsonObjectConverter;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.justice.services.core.cdi.LoggerProducer;
import uk.gov.justice.services.eventsource.DefaultEventDestinationResolver;
import uk.gov.justice.services.eventsourcing.publisher.jms.EventDestinationResolver;
import uk.gov.justice.services.eventsourcing.publisher.jms.EventPublisher;
import uk.gov.justice.services.eventsourcing.publishing.helpers.DummyEventPublisher;
import uk.gov.justice.services.eventsourcing.publishing.helpers.EventFactory;
import uk.gov.justice.services.eventsourcing.publishing.helpers.EventStoreInitializer;
import uk.gov.justice.services.eventsourcing.publishing.helpers.TestEventInserter;
import uk.gov.justice.services.eventsourcing.publishing.helpers.TestGlobalValueProducer;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.Event;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.EventConverter;
import uk.gov.justice.services.jdbc.persistence.JdbcDataSourceProvider;
import uk.gov.justice.services.messaging.DefaultJsonObjectEnvelopeConverter;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.JsonObjectEnvelopeConverter;
import uk.gov.justice.services.messaging.jms.DefaultEnvelopeConverter;
import uk.gov.justice.services.messaging.jms.DefaultJmsEnvelopeSender;
import uk.gov.justice.services.messaging.jms.EnvelopeConverter;
import uk.gov.justice.services.messaging.jms.JmsEnvelopeSender;
import uk.gov.justice.services.messaging.logging.DefaultTraceLogger;
import uk.gov.justice.services.messaging.logging.TraceLogger;
import uk.gov.justice.services.test.utils.core.messaging.Poller;
import uk.gov.justice.services.test.utils.persistence.TestJdbcDataSourceProvider;
import uk.gov.justice.subscription.EventSourcesParser;
import uk.gov.justice.subscription.ParserProducer;
import uk.gov.justice.subscription.YamlFileFinder;
import uk.gov.justice.subscription.registry.EventSourceDefinitionRegistry;
import uk.gov.justice.subscription.registry.EventSourceDefinitionRegistryProducer;
import uk.gov.justice.subscription.registry.SubscriptionDataSourceProvider;
import uk.gov.justice.subscription.yaml.parser.YamlParser;
import uk.gov.justice.subscription.yaml.parser.YamlSchemaLoader;

import java.util.List;
import java.util.Optional;
import java.util.Properties;

import javax.annotation.Resource;
import javax.inject.Inject;
import javax.sql.DataSource;

import org.apache.openejb.jee.WebApp;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.testing.Application;
import org.apache.openejb.testing.Classes;
import org.apache.openejb.testing.Configuration;
import org.apache.openejb.testing.Module;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;

@RunWith(ApplicationComposer.class)
public class EventDeQueuerTimerBeanIT {

    @Resource(name = "openejb/Resource/frameworkeventstore")
    private DataSource dataSource;

    @Inject
    private TestJdbcDataSourceProvider testJdbcDataSourceProvider;

    @Inject
    private DummyEventPublisher dummyEventPublisher;

    private final EventFactory eventFactory = new EventFactory();
    private final TestEventInserter testEventInserter = new TestEventInserter();
    private final Poller poller = new Poller(10, 3_000L);
    private final EventStoreInitializer eventStoreInitializer = new EventStoreInitializer();

    @Before
    public void initializeDatabase() throws Exception {

        eventStoreInitializer.initializeEventStore(dataSource);
        testJdbcDataSourceProvider.setDataSource(dataSource);
    }

    @Module
    @Classes(cdi = true, value = {
            EventDeQueuerTimerBean.class,
            EventDeQueuerAndPublisher.class,
            EventDeQueuer.class,
            EventPublisher.class,
            DummyEventPublisher.class,
            EventConverter.class,
            JmsEnvelopeSender.class,
            DefaultJmsEnvelopeSender.class,
            Logger.class,
            EventDestinationResolver.class,
            StringToJsonObjectConverter.class,
            JdbcDataSourceProvider.class,
            TestJdbcDataSourceProvider.class,
            EventSourceDefinitionRegistry.class,
            EventDestinationResolver.class,
            DefaultEventDestinationResolver.class,
            JsonObjectEnvelopeConverter.class,
            EventSourceDefinitionRegistryProducer.class,
            EnvelopeConverter.class,
            DefaultEnvelopeConverter.class,
            LoggerProducer.class,
            JsonObjectEnvelopeConverter.class,
            DefaultJsonObjectEnvelopeConverter.class,
            TraceLogger.class,
            DefaultTraceLogger.class,
            ObjectMapperProducer.class,
            EventSourcesParser.class,
            ParserProducer.class,
            YamlFileFinder.class,
            YamlSchemaLoader.class,
            YamlParser.class,
            SubscriptionDataSourceProvider.class,
            TimerConfigFactory.class,
            TestGlobalValueProducer.class
    })
    public WebApp war() {
        return new WebApp()
                .contextRoot("EventDeQueuerExecutorIT")
                .addServlet("App", Application.class.getName());
    }

    @Configuration
    public Properties configuration() {
        return createOpenEjbConfigurationBuilder()
                .addInitialContext()
                .addPostgresqlEventStore()
                .build();
    }

    @Test
    public void shouldPublishEventsInTheEventLogTable() throws Exception {

        final Event event_1 = eventFactory.createEvent("event_1", 1);
        final Event event_2 = eventFactory.createEvent("event_2", 2);
        final Event event_3 = eventFactory.createEvent("event_3", 3);

        testEventInserter.insertIntoEventLog(event_1);
        testEventInserter.insertIntoEventLog(event_2);
        testEventInserter.insertIntoEventLog(event_3);

        final Optional<List<JsonEnvelope>> jsonEnvelopeOptional = poller.pollUntilFound(() -> {
            final List<JsonEnvelope> jsonEnvelopes = dummyEventPublisher.getJsonEnvelopes();
            if (jsonEnvelopes.size() > 2) {
                return of(jsonEnvelopes);
            }

            return empty();
        });

        if(jsonEnvelopeOptional.isPresent()) {
            final List<JsonEnvelope> envelopes = jsonEnvelopeOptional.get();

            assertThat(envelopes.size(), is(3));

            assertThat(envelopes.get(0).metadata().name(), is("event_1"));
            assertThat(envelopes.get(1).metadata().name(), is("event_2"));
            assertThat(envelopes.get(2).metadata().name(), is("event_3"));
        } else {
            fail();
        }
    }
}
