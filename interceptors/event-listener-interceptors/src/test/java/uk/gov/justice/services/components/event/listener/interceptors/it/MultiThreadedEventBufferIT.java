package uk.gov.justice.services.components.event.listener.interceptors.it;

import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.toList;
import static javax.ejb.TransactionAttributeType.NOT_SUPPORTED;
import static javax.ejb.TransactionAttributeType.REQUIRES_NEW;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;
import static uk.gov.justice.services.core.annotation.Component.EVENT_LISTENER;
import static uk.gov.justice.services.core.interceptor.InterceptorContext.interceptorContextWithInput;
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
import uk.gov.justice.services.event.buffer.core.repository.streambuffer.StreamBufferEvent;
import uk.gov.justice.services.event.buffer.core.repository.streambuffer.StreamBufferJdbcRepository;
import uk.gov.justice.services.event.buffer.core.repository.streamstatus.StreamStatus;
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
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.stream.Stream;

import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.enterprise.concurrent.ManagedExecutorService;
import javax.enterprise.concurrent.ManagedTask;
import javax.enterprise.concurrent.ManagedTaskListener;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.naming.InitialContext;
import javax.sql.DataSource;

import liquibase.Liquibase;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;
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
public class MultiThreadedEventBufferIT implements ManagedTaskListener {

    private static final String LIQUIBASE_STREAM_STATUS_CHANGELOG_XML = "liquibase/event-buffer-changelog.xml";
    private static final String EVENT_SUPPORTED_ABC = "test.event-abc";

    @Resource(name = "openejb/Resource/frameworkviewstore")
    private DataSource dataSource;

    @Resource
    private ManagedExecutorService managedExecutorService;

    @Inject
    private AsynchronousDispatchBean asynchronousDispatchBean;

    @Inject
    private AbcEventHandler abcEventHandler;

    @Inject
    private StreamStatusJdbcRepository streamStatusRepository;

    @Inject
    private StreamBufferJdbcRepository streamBufferRepository;

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
        InitialContext initialContext = new InitialContext();
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
    public void shouldSupportMultiThreadingEventBuffering() {

        final UUID metadataId1 = randomUUID();
        final UUID metadataId2 = randomUUID();
        final UUID metadataId3 = randomUUID();
        final UUID streamId = randomUUID();

        final JsonEnvelope jsonEnvelope_1 = envelope()
                .with(metadataOf(metadataId1, EVENT_SUPPORTED_ABC)
                        .withStreamId(streamId)
                        .withVersion(1L))
                .build();
        final JsonEnvelope jsonEnvelope_2 = envelope()
                .with(metadataOf(metadataId2, EVENT_SUPPORTED_ABC)
                        .withStreamId(streamId)
                        .withVersion(2L))
                .build();
        final JsonEnvelope jsonEnvelope_3 = envelope()
                .with(metadataOf(metadataId3, EVENT_SUPPORTED_ABC)
                        .withStreamId(streamId)
                        .withVersion(3L))
                .build();

        final Stream<JsonEnvelope> jsonEnvelopeStream = Stream.of(
                jsonEnvelope_2,
                jsonEnvelope_3,
                jsonEnvelope_1);

        jsonEnvelopeStream.forEach(jsonEnvelope -> {
            managedExecutorService.submit(new JsonEnvelopeDispatchTask(
                    jsonEnvelope,
                    asynchronousDispatchBean,
                    this
            ));
        });

        final Optional<List<JsonEnvelope>> recordedJsonEnvelopes = new Poller(10, 1000L).pollUntilFound(() -> {
            final List<JsonEnvelope> jsonEnvelopes = abcEventHandler.recordedEnvelopes();

            if (jsonEnvelopes.size() == 3) {
                return Optional.of(jsonEnvelopes);
            }

            return Optional.empty();
        });

        final List<StreamBufferEvent> bufferEvent = streamBufferRepository.findStreamByIdAndSource(streamId, "test").collect(toList());
        final StreamStatus streamStatus = streamStatusRepository.findByStreamIdAndSource(streamId, "test").get();

        System.out.println("Stream buffer number of events = " + bufferEvent.size());
        System.out.println("Stream status version = " + streamStatus.getVersion());
        assertThat(recordedJsonEnvelopes.get().size(), is(3));
    }

    @Override
    public void taskSubmitted(final Future<?> future, final ManagedExecutorService executor, final Object task) {
        System.out.println("Submitted");
    }

    @Override
    public void taskAborted(final Future<?> future, final ManagedExecutorService executor, final Object task, final Throwable exception) {
        exception.printStackTrace();
        fail(exception.getMessage());
    }

    @Override
    public void taskDone(final Future<?> future, final ManagedExecutorService executor, final Object task, final Throwable exception) {
        System.out.println("Done");
    }

    @Override
    public void taskStarting(final Future<?> future, final ManagedExecutorService executor, final Object task) {
        System.out.println("Starting...");
    }

    @ServiceComponent(EVENT_LISTENER)
    @ApplicationScoped
    public static class AbcEventHandler extends TestEnvelopeRecorder {

        @Handles("*")
        public void handle(JsonEnvelope envelope) {
            record(envelope);
        }

    }

    @Stateless
    @Adapter(EVENT_LISTENER)
    public static class AsynchronousDispatchBean {

        @Resource(name = "openejb/Resource/frameworkviewstore")
        private DataSource dataSource;

        @Inject
        private InterceptorChainProcessor interceptorChainProcessor;

        public void init() throws Exception {
            InitialContext initialContext = new InitialContext();
            initialContext.bind("java:/DS.MultiThreadedEventBufferIT", dataSource);
        }

        @TransactionAttribute(NOT_SUPPORTED)
        public Optional<JsonEnvelope> process(final JsonEnvelope jsonEnvelope) {
            try {
                return processWithChain(jsonEnvelope);
            } catch (final Throwable t) {
                t.printStackTrace();
                throw t;
            }
        }

        @TransactionAttribute(REQUIRES_NEW)
        private Optional<JsonEnvelope> processWithChain(final JsonEnvelope jsonEnvelope) {
            return interceptorChainProcessor.process(interceptorContextWithInput(jsonEnvelope));
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

    public static class JsonEnvelopeDispatchTask implements Callable<Optional<JsonEnvelope>>, ManagedTask {

        private final JsonEnvelope jsonEnvelope;
        private final AsynchronousDispatchBean asynchronousDispatchBean;
        private final ManagedTaskListener managedTaskListener;

        public JsonEnvelopeDispatchTask(final JsonEnvelope jsonEnvelope,
                                        final AsynchronousDispatchBean asynchronousDispatchBean,
                                        final ManagedTaskListener managedTaskListener) {
            this.jsonEnvelope = jsonEnvelope;
            this.asynchronousDispatchBean = asynchronousDispatchBean;
            this.managedTaskListener = managedTaskListener;
        }

        @Override
        public Optional<JsonEnvelope> call() throws Exception {
            try {
                return asynchronousDispatchBean.process(jsonEnvelope);
            } catch (final Throwable t) {
                t.printStackTrace();
                throw t;
            }
        }

        @Override
        public ManagedTaskListener getManagedTaskListener() {
            return managedTaskListener;
        }

        @Override
        public Map<String, String> getExecutionProperties() {
            return null;
        }
    }
}
