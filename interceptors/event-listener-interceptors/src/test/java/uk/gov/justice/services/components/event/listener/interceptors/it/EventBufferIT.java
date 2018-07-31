package uk.gov.justice.services.components.event.listener.interceptors.it;

import static co.unruly.matchers.OptionalMatchers.contains;
import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.collection.IsEmptyCollection.empty;
import static org.junit.Assert.assertThat;
import static uk.gov.justice.services.core.annotation.Component.EVENT_LISTENER;
import static uk.gov.justice.services.core.interceptor.InterceptorContext.interceptorContextWithInput;
import static uk.gov.justice.services.test.utils.core.messaging.JsonEnvelopeBuilder.envelope;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataOf;

import uk.gov.justice.schema.service.CatalogProducer;
import uk.gov.justice.schema.service.SchemaCatalogResolverProducer;
import uk.gov.justice.schema.service.SchemaCatalogService;
import uk.gov.justice.services.common.configuration.GlobalValueProducer;
import uk.gov.justice.services.common.converter.ObjectToJsonValueConverter;
import uk.gov.justice.services.common.converter.StringToJsonObjectConverter;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.justice.services.components.event.listener.interceptors.EventBufferInterceptor;
import uk.gov.justice.services.core.accesscontrol.AccessControlFailureMessageGenerator;
import uk.gov.justice.services.core.accesscontrol.AllowAllPolicyEvaluator;
import uk.gov.justice.services.core.accesscontrol.DefaultAccessControlService;
import uk.gov.justice.services.core.accesscontrol.PolicyEvaluator;
import uk.gov.justice.services.core.annotation.Adapter;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.cdi.LoggerProducer;
import uk.gov.justice.services.core.dispatcher.DispatcherCache;
import uk.gov.justice.services.core.dispatcher.DispatcherFactory;
import uk.gov.justice.services.core.dispatcher.EmptySystemUserProvider;
import uk.gov.justice.services.core.dispatcher.EnvelopePayloadTypeConverter;
import uk.gov.justice.services.core.dispatcher.JsonEnvelopeRepacker;
import uk.gov.justice.services.core.dispatcher.ServiceComponentObserver;
import uk.gov.justice.services.core.dispatcher.SystemUserUtil;
import uk.gov.justice.services.core.envelope.EnvelopeInspector;
import uk.gov.justice.services.core.envelope.EnvelopeValidationExceptionHandlerProducer;
import uk.gov.justice.services.core.envelope.EnvelopeValidator;
import uk.gov.justice.services.core.envelope.MediaTypeProvider;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.core.extension.BeanInstantiater;
import uk.gov.justice.services.core.extension.ServiceComponentScanner;
import uk.gov.justice.services.core.interceptor.InterceptorCache;
import uk.gov.justice.services.core.interceptor.InterceptorChainEntry;
import uk.gov.justice.services.core.interceptor.InterceptorChainEntryProvider;
import uk.gov.justice.services.core.interceptor.InterceptorChainObserver;
import uk.gov.justice.services.core.interceptor.InterceptorChainProcessor;
import uk.gov.justice.services.core.interceptor.InterceptorChainProcessorProducer;
import uk.gov.justice.services.core.json.BackwardsCompatibleJsonSchemaValidator;
import uk.gov.justice.services.core.json.FileBasedJsonSchemaValidator;
import uk.gov.justice.services.core.json.JsonSchemaLoader;
import uk.gov.justice.services.core.json.PayloadExtractor;
import uk.gov.justice.services.core.json.SchemaCatalogAwareJsonSchemaValidator;
import uk.gov.justice.services.core.mapping.ActionNameToMediaTypesMappingObserver;
import uk.gov.justice.services.core.mapping.DefaultMediaTypesMappingCache;
import uk.gov.justice.services.core.mapping.DefaultNameToMediaTypeConverter;
import uk.gov.justice.services.core.mapping.DefaultSchemaIdMappingCache;
import uk.gov.justice.services.core.mapping.MediaTypesMappingCacheInitialiser;
import uk.gov.justice.services.core.mapping.SchemaIdMappingCacheInitialiser;
import uk.gov.justice.services.core.mapping.SchemaIdMappingObserver;
import uk.gov.justice.services.core.postgres.OpenEjbConfigurationBuilder;
import uk.gov.justice.services.core.requester.RequesterProducer;
import uk.gov.justice.services.core.sender.SenderProducer;
import uk.gov.justice.services.event.buffer.core.repository.streambuffer.EventBufferEvent;
import uk.gov.justice.services.event.buffer.core.repository.streambuffer.EventBufferJdbcRepository;
import uk.gov.justice.services.event.buffer.core.repository.subscription.Subscription;
import uk.gov.justice.services.event.buffer.core.repository.subscription.SubscriptionJdbcRepository;
import uk.gov.justice.services.event.buffer.core.service.BufferInitialisationStrategyProducer;
import uk.gov.justice.services.event.buffer.core.service.ConsecutiveEventBufferService;
import uk.gov.justice.services.jdbc.persistence.JdbcRepositoryHelper;
import uk.gov.justice.services.jdbc.persistence.ViewStoreJdbcDataSourceProvider;
import uk.gov.justice.services.messaging.DefaultJsonObjectEnvelopeConverter;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.jms.DefaultEnvelopeConverter;
import uk.gov.justice.services.messaging.jms.DefaultJmsEnvelopeSender;
import uk.gov.justice.services.messaging.logging.DefaultTraceLogger;
import uk.gov.justice.services.test.utils.common.envelope.TestEnvelopeRecorder;
import uk.gov.justice.services.test.utils.persistence.DatabaseCleaner;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.UUID;

import javax.annotation.Resource;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.apache.openejb.jee.Application;
import org.apache.openejb.jee.WebApp;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.testing.Classes;
import org.apache.openejb.testing.Configuration;
import org.apache.openejb.testing.Module;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(ApplicationComposer.class)
@Adapter(EVENT_LISTENER)
public class EventBufferIT {

    private static final String SOURCE = "my-context";
    private static final String EVENT_ABC = SOURCE + ".event-abc";
    private static final String CONTEXT_ROOT = "core-test";

    @Resource(name = "openejb/Resource/frameworkviewstore")
    private DataSource dataSource;

    @Inject
    private InterceptorChainProcessor interceptorChainProcessor;

    @Inject
    private AbcEventHandler abcEventHandler;

    @Inject
    private EventBufferJdbcRepository jdbcStreamBufferRepository;

    @Inject
    private SubscriptionJdbcRepository statusRepository;

    @Before
    public void setup() throws Exception {
        final InitialContext initialContext = new InitialContext();
        initialContext.bind("java:/DS.EventBufferIT", dataSource);
        new DatabaseCleaner().cleanStreamBufferTable("framework");
    }

    @Configuration
    public Properties postgresqlConfiguration() {
        return OpenEjbConfigurationBuilder.createOpenEjbConfigurationBuilder()
                .addInitialContext()
                .addPostgresqlViewStore()
                .build();
    }


    @Module
    @Classes(cdi = true, value = {
            AbcEventHandler.class,
            ServiceComponentScanner.class,
            RequesterProducer.class,
            ServiceComponentObserver.class,

            InterceptorChainProcessorProducer.class,
            InterceptorCache.class,
            InterceptorChainObserver.class,
            EventBufferInterceptor.class,
            EventListenerInterceptorChainProvider.class,

            SenderProducer.class,
            DefaultJmsEnvelopeSender.class,
            DefaultEnvelopeConverter.class,

            StringToJsonObjectConverter.class,
            DefaultJsonObjectEnvelopeConverter.class,
            ObjectToJsonValueConverter.class,
            ObjectMapperProducer.class,
            Enveloper.class,

            AccessControlFailureMessageGenerator.class,
            AllowAllPolicyEvaluator.class,
            DefaultAccessControlService.class,
            DispatcherCache.class,
            DispatcherFactory.class,
            EnvelopePayloadTypeConverter.class,
            JsonEnvelopeRepacker.class,
            PolicyEvaluator.class,

            ConsecutiveEventBufferService.class,
            BufferInitialisationStrategyProducer.class,
            LoggerProducer.class,
            EmptySystemUserProvider.class,
            SystemUserUtil.class,
            BeanInstantiater.class,
            UtcClock.class,

            GlobalValueProducer.class,
            EnvelopeValidationExceptionHandlerProducer.class,
            FileBasedJsonSchemaValidator.class,
            JsonSchemaLoader.class,
            DefaultTraceLogger.class,
            JdbcRepositoryHelper.class,
            ViewStoreJdbcDataSourceProvider.class,
            EventBufferJdbcRepository.class,
            SubscriptionJdbcRepository.class,

            SchemaCatalogAwareJsonSchemaValidator.class,
            PayloadExtractor.class,
            DefaultNameToMediaTypeConverter.class,
            DefaultSchemaIdMappingCache.class,
            SchemaIdMappingObserver.class,

            CatalogProducer.class,
            SchemaCatalogService.class,
            SchemaCatalogResolverProducer.class,

            DefaultMediaTypesMappingCache.class,
            ActionNameToMediaTypesMappingObserver.class,

            SenderProducer.class,
            MediaTypeProvider.class,
            EnvelopeValidator.class,
            EnvelopeInspector.class,
            RequesterProducer.class,
            BackwardsCompatibleJsonSchemaValidator.class,

            MediaTypesMappingCacheInitialiser.class,
            SchemaIdMappingCacheInitialiser.class
    })

    public WebApp war() {
        return new WebApp()
                .contextRoot(CONTEXT_ROOT)
                .addServlet("TestApp", Application.class.getName());
    }

    @Test
    public void shouldAddEventToBufferIfVersionNotOne() {

        final UUID metadataId = randomUUID();
        final UUID streamId = randomUUID();
        final JsonEnvelope envelope = envelope()
                .with(metadataOf(metadataId, EVENT_ABC)
                        .withStreamId(streamId).withVersion(2L))
                .build();

        interceptorChainProcessor.process(interceptorContextWithInput(envelope));

        final List<EventBufferEvent> eventBufferEvents = jdbcStreamBufferRepository.findStreamByIdAndSource(streamId, SOURCE).collect(toList());

        assertThat(eventBufferEvents, hasSize(1));
        assertThat(eventBufferEvents.get(0).getStreamId(), is(streamId));
        assertThat(eventBufferEvents.get(0).getPosition(), is(2L));
        assertThat(eventBufferEvents.get(0).getSource(), is(SOURCE));

        final List<JsonEnvelope> handledEnvelopes = abcEventHandler.recordedEnvelopes();
        assertThat(handledEnvelopes, empty());
    }

    @Test
    public void shouldAddStatusVersionForNewStreamIdAndProcessIncomingEvent() {

        final UUID metadataId = randomUUID();
        final UUID streamId = randomUUID();

        final JsonEnvelope jsonEnvelope = envelope()
                .with(metadataOf(metadataId, EVENT_ABC)
                        .withStreamId(streamId).withVersion(1L))
                .build();
        interceptorChainProcessor.process(interceptorContextWithInput(jsonEnvelope));

        final List<EventBufferEvent> eventBufferEvents = jdbcStreamBufferRepository.findStreamByIdAndSource(streamId, SOURCE).collect(toList());
        final Optional<Subscription> subscription = statusRepository.findByStreamIdAndSource(streamId, SOURCE);

        assertThat(eventBufferEvents, empty());
        assertThat(subscription.isPresent(), is(true));
        assertThat(subscription.get().getPosition(), is(1L));

        final List<JsonEnvelope> handledEnvelopes = abcEventHandler.recordedEnvelopes();
        assertThat(handledEnvelopes, hasSize(1));

        assertThat(handledEnvelopes.get(0).metadata().id(), is(metadataId));
        assertThat(handledEnvelopes.get(0).metadata().position(), contains(1L));
    }

    @Test
    public void shouldIncrementVersionWhenEventInOrder() throws SQLException, NamingException {

        final UUID metadataId = randomUUID();
        final UUID streamId = randomUUID();

        statusRepository.insert(new Subscription(streamId, 1L, SOURCE));


        final JsonEnvelope envelope = envelope()
                .with(metadataOf(metadataId, EVENT_ABC)
                        .withStreamId(streamId).withVersion(2L))
                .build();


        interceptorChainProcessor.process(interceptorContextWithInput(envelope));

        final List<EventBufferEvent> eventBufferEvents = jdbcStreamBufferRepository.findStreamByIdAndSource(streamId, SOURCE).collect(toList());
        final Optional<Subscription> subscription = statusRepository.findByStreamIdAndSource(streamId, SOURCE);

        assertThat(eventBufferEvents, empty());
        assertThat(subscription.isPresent(), is(true));
        assertThat(subscription.get().getPosition(), is(2L));

    }

    @Test
    public void shouldNotIncrementVersionWhenEventNotInOrder() throws SQLException, NamingException {

        final UUID metadataId = randomUUID();
        final UUID streamId = randomUUID();

        statusRepository.insert(new Subscription(streamId, 2L, SOURCE));

        final JsonEnvelope jsonEnvelope = envelope()
                .with(metadataOf(metadataId, EVENT_ABC)
                        .withStreamId(streamId).withVersion(4L))
                .build();
        interceptorChainProcessor.process(interceptorContextWithInput(jsonEnvelope));

        final List<EventBufferEvent> eventBufferEvents = jdbcStreamBufferRepository.findStreamByIdAndSource(streamId, SOURCE).collect(toList());
        final Optional<Subscription> subscription = statusRepository.findByStreamIdAndSource(streamId, SOURCE);

        assertThat(eventBufferEvents, hasSize(1));
        assertThat(eventBufferEvents.get(0).getStreamId(), is(streamId));
        assertThat(eventBufferEvents.get(0).getPosition(), is(4L));
        assertThat(subscription.isPresent(), is(true));
        assertThat(subscription.get().getPosition(), is(2L));

    }

    @Test
    public void shouldReleaseBufferWhenMissingEventArrives() throws SQLException, NamingException {

        final UUID metadataId2 = randomUUID();
        final UUID streamId = randomUUID();

        final JsonEnvelope jsonEnvelope = envelope()
                .with(metadataOf(metadataId2, EVENT_ABC)
                        .withStreamId(streamId).withVersion(2L))
                .build();


        statusRepository.insert(new Subscription(streamId, 1L, SOURCE));

        final UUID metadataId3 = randomUUID();
        final UUID metadataId4 = randomUUID();
        final UUID metadataId5 = randomUUID();

        jdbcStreamBufferRepository.insert(
                new EventBufferEvent(
                        streamId,
                        3L,
                        envelope()
                                .with(metadataOf(metadataId3, EVENT_ABC)
                                        .withStreamId(streamId)
                                        .withVersion(3L)
                                ).toJsonString(),
                        SOURCE
                )
        );

        jdbcStreamBufferRepository.insert(
                new EventBufferEvent(
                        streamId,
                        4L,
                        envelope().with(metadataOf(metadataId4, EVENT_ABC)
                                .withStreamId(streamId)
                                .withVersion(4L))
                                .toJsonString(),
                        SOURCE

                )
        );

        jdbcStreamBufferRepository.insert(
                new EventBufferEvent(
                        streamId,
                        5L,
                        envelope().with(
                                metadataOf(metadataId5, EVENT_ABC)
                                        .withStreamId(streamId).withVersion(5L))
                                .toJsonString(),
                        SOURCE
                )
        );

        interceptorChainProcessor.process(interceptorContextWithInput(jsonEnvelope));


        final List<EventBufferEvent> eventBufferEvents = jdbcStreamBufferRepository.findStreamByIdAndSource(streamId, SOURCE).collect(toList());
        final Optional<Subscription> subscription = statusRepository.findByStreamIdAndSource(streamId, SOURCE);

        assertThat(subscription.isPresent(), is(true));
        assertThat(subscription.get().getPosition(), is(5L));

        final List<JsonEnvelope> handledEnvelopes = abcEventHandler.recordedEnvelopes();
        assertThat(handledEnvelopes, hasSize(4));

        assertThat(handledEnvelopes.get(0).metadata().id(), is(metadataId2));
        assertThat(handledEnvelopes.get(0).metadata().position(), contains(2L));

        assertThat(handledEnvelopes.get(1).metadata().id(), is(metadataId3));
        assertThat(handledEnvelopes.get(1).metadata().position(), contains(3L));

        assertThat(handledEnvelopes.get(2).metadata().id(), is(metadataId4));
        assertThat(handledEnvelopes.get(2).metadata().position(), contains(4L));

        assertThat(handledEnvelopes.get(3).metadata().id(), is(metadataId5));
        assertThat(handledEnvelopes.get(3).metadata().position(), contains(5L));


        assertThat(eventBufferEvents, hasSize(0));
    }

    @Test
    public void shouldIgnoreEventWithSupersededVersion() throws SQLException, NamingException {

        final UUID metadataId = randomUUID();
        final UUID streamId = randomUUID();

        final JsonEnvelope jsonEnvelope = envelope()
                .with(metadataOf(metadataId, EVENT_ABC)
                        .withStreamId(streamId).withVersion(1L))
                .build();

        final EventBufferEvent eventBufferEvent2 = new EventBufferEvent(streamId, 4L, "payload", SOURCE);
        final EventBufferEvent eventBufferEvent3 = new EventBufferEvent(streamId, 5L, "payload", SOURCE);


        statusRepository.insert(new Subscription(streamId, 2L, SOURCE));
        jdbcStreamBufferRepository.insert(eventBufferEvent2);
        jdbcStreamBufferRepository.insert(eventBufferEvent3);

        interceptorChainProcessor.process(interceptorContextWithInput(jsonEnvelope));

        final List<EventBufferEvent> eventBufferEvents = jdbcStreamBufferRepository
                .findStreamByIdAndSource(streamId, SOURCE)
                .collect(toList());
        final Optional<Subscription> subscription = statusRepository.findByStreamIdAndSource(streamId, SOURCE);

        assertThat(eventBufferEvents, hasSize(2));
        assertThat(subscription.isPresent(), is(true));
        assertThat(subscription.get().getPosition(), is(2L));
        assertThat(abcEventHandler.recordedEnvelopes(), empty());
    }

    @ServiceComponent(EVENT_LISTENER)
    @ApplicationScoped
    public static class AbcEventHandler extends TestEnvelopeRecorder {

        @Handles(EVENT_ABC)
        public void handle(JsonEnvelope envelope) {
            record(envelope);
        }

    }

    public static class EventListenerInterceptorChainProvider implements InterceptorChainEntryProvider {

        @Override
        public String component() {
            return EVENT_LISTENER;
        }

        @Override
        public List<InterceptorChainEntry> interceptorChainTypes() {
            final List<InterceptorChainEntry> interceptorChainTypes = new ArrayList<>();
            interceptorChainTypes.add(new InterceptorChainEntry(1, EventBufferInterceptor.class));
            return interceptorChainTypes;
        }
    }
}
