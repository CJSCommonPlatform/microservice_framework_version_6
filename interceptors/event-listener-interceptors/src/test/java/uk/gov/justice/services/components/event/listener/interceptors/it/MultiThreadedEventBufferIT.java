package uk.gov.justice.services.components.event.listener.interceptors.it;

import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;
import static uk.gov.justice.services.core.annotation.Component.EVENT_LISTENER;
import static uk.gov.justice.services.test.utils.core.messaging.JsonEnvelopeBuilder.envelope;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataOf;

import uk.gov.justice.schema.catalog.CatalogProducer;
import uk.gov.justice.schema.service.SchemaCatalogService;
import uk.gov.justice.services.common.configuration.GlobalValueProducer;
import uk.gov.justice.services.common.converter.ObjectToJsonValueConverter;
import uk.gov.justice.services.common.converter.StringToJsonObjectConverter;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.justice.services.components.event.listener.interceptors.EventBufferInterceptor;
import uk.gov.justice.services.components.event.listener.interceptors.it.util.repository.StreamBufferOpenEjbAwareJdbcRepository;
import uk.gov.justice.services.components.event.listener.interceptors.it.util.repository.StreamStatusOpenEjbAwareJdbcRepository;
import uk.gov.justice.services.core.accesscontrol.AccessControlFailureMessageGenerator;
import uk.gov.justice.services.core.accesscontrol.AllowAllPolicyEvaluator;
import uk.gov.justice.services.core.accesscontrol.DefaultAccessControlService;
import uk.gov.justice.services.core.accesscontrol.PolicyEvaluator;
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
import uk.gov.justice.services.core.interceptor.InterceptorChainProcessorProducer;
import uk.gov.justice.services.core.json.BackwardsCompatibleJsonSchemaValidator;
import uk.gov.justice.services.core.json.DefaultFileSystemUrlResolverStrategy;
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
import uk.gov.justice.services.event.buffer.core.repository.streambuffer.StreamBufferJdbcRepository;
import uk.gov.justice.services.event.buffer.core.repository.streamstatus.StreamStatusJdbcRepository;
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
import uk.gov.justice.services.test.utils.core.messaging.Poller;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Resource;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.naming.InitialContext;
import javax.sql.DataSource;

import liquibase.Liquibase;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;
import org.apache.commons.lang3.time.StopWatch;
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
public class MultiThreadedEventBufferIT {

    private static final String LIQUIBASE_STREAM_STATUS_CHANGELOG_XML = "liquibase/event-buffer-changelog.xml";
    private static final String EVENT_SUPPORTED_ABC = "test.event-abc";

    @Resource(name = "openejb/Resource/frameworkviewstore")
    private DataSource dataSource;

    @Inject
    private AsynchronousDispatchBean asynchronousDispatchBean;

    @Inject
    private AbcEventHandler abcEventHandler;

    @Module
    @Classes(cdi = true, value = {
            AbcEventHandler.class,
            AsynchronousDispatchBean.class,
            ServiceComponentScanner.class,
            RequesterProducer.class,
            ServiceComponentObserver.class,

            SenderProducer.class,
            DefaultJmsEnvelopeSender.class,
            DefaultEnvelopeConverter.class,

            StringToJsonObjectConverter.class,
            DefaultJsonObjectEnvelopeConverter.class,
            ObjectToJsonValueConverter.class,
            ObjectMapperProducer.class,
            Enveloper.class,

            InterceptorChainProcessorProducer.class,
            InterceptorCache.class,
            InterceptorChainObserver.class,
            EventListenerInterceptorChainProvider.class,

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
            EventBufferInterceptor.class,
            LoggerProducer.class,
            EmptySystemUserProvider.class,
            SystemUserUtil.class,
            BeanInstantiater.class,
            UtcClock.class,

            DefaultFileSystemUrlResolverStrategy.class,

            GlobalValueProducer.class,
            EnvelopeValidationExceptionHandlerProducer.class,

            FileBasedJsonSchemaValidator.class,
            JsonSchemaLoader.class,
            DefaultTraceLogger.class,
            JdbcRepositoryHelper.class,
            ViewStoreJdbcDataSourceProvider.class,
            StreamBufferOpenEjbAwareJdbcRepository.class,
            StreamStatusOpenEjbAwareJdbcRepository.class,

            SchemaCatalogAwareJsonSchemaValidator.class,
            PayloadExtractor.class,
            DefaultNameToMediaTypeConverter.class,
            DefaultSchemaIdMappingCache.class,
            SchemaIdMappingObserver.class,

            CatalogProducer.class,
            SchemaCatalogService.class,

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
                .contextRoot("core-test")
                .addServlet("TestApp", Application.class.getName());
    }

    @Before
    public void init() throws Exception {
        final InitialContext initialContext = new InitialContext();
        initialContext.bind("java:/DS.MultiThreadedEventBufferIT", dataSource);
        initDatabase();
        asynchronousDispatchBean.init();
    }

    @Configuration
    public Properties postgresqlConfiguration() {
        return OpenEjbConfigurationBuilder.createOpenEjbConfigurationBuilder()
                .addInitialContext()
                .addPostgresqlViewStore()
                .build();
    }

    @Test
    public void shouldSupportMultiThreadingEventBuffering() throws Exception {


        final int numberOfStreams = 10;
        final int numberOfEventsPerStream = 100;
        final int totalNumberOfEvents = numberOfStreams * numberOfEventsPerStream;
        final List<UUID> streamIds = generateStreamIds(numberOfStreams);

        final List<Stream<JsonEnvelope>> streams = streamIds
                .stream()
                .map(streamId -> generateEnvelopes(numberOfEventsPerStream, streamId))
                .collect(toList());

        
        final StopWatch stopWatch = new StopWatch();

        stopWatch.start();
        streams.forEach(asynchronousDispatchBean::process);

        stopWatch.stop();

        System.out.println("========================================================================");
        System.out.println("Took " + stopWatch.getTime() + " milliseconds");
        System.out.println("========================================================================");

        final Optional<List<JsonEnvelope>> recordedJsonEnvelopes = new Poller(30, 1000L).pollUntilFound(() -> {
            final List<JsonEnvelope> jsonEnvelopes = abcEventHandler.recordedEnvelopes();

            if (jsonEnvelopes.size() == totalNumberOfEvents) {
                return Optional.of(jsonEnvelopes);
            }

            return Optional.empty();
        });

        assertThat(recordedJsonEnvelopes.get().size(), is(totalNumberOfEvents));
    }

    private Stream<JsonEnvelope> generateEnvelopes(final int numberOfEvents, final UUID streamId) {
        final List<JsonEnvelope> envelopes = new ArrayList<>();
        for(long version = 1; version <= numberOfEvents; version++) {
            envelopes.add(createEnvelope(streamId, version));
        }

        return envelopes.stream();
    }

    private List<UUID> generateStreamIds(final int numberOfStreams) {

        final List<UUID> streamIds = new ArrayList<>();
        for (int i = 0; i < numberOfStreams; i++) {
            streamIds.add(randomUUID());
        }

        return streamIds;
    }

    private JsonEnvelope createEnvelope(final UUID streamId, final long version) {
        return envelope()
                .with(metadataOf(randomUUID(), EVENT_SUPPORTED_ABC)
                        .withStreamId(streamId)
                        .withVersion(version))
                .build();
    }

    @ServiceComponent(EVENT_LISTENER)
    @ApplicationScoped
    public static class AbcEventHandler extends TestEnvelopeRecorder {

        @Handles("*")
        public void handle(JsonEnvelope envelope) {
            record(envelope);
        }

    }

    private void initDatabase() throws Exception {
        Liquibase liquibase = new Liquibase(LIQUIBASE_STREAM_STATUS_CHANGELOG_XML,
                new ClassLoaderResourceAccessor(), new JdbcConnection(dataSource.getConnection()));
        liquibase.dropAll();
        liquibase.update("");
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
