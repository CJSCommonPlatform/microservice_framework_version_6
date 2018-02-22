package uk.gov.justice.raml.jms.it;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import uk.gov.justice.api.CustomEventListenerEventFilter;
import uk.gov.justice.api.CustomEventListenerPeopleEventJmsListener;
import uk.gov.justice.api.CustomEventValidationInterceptor;
import uk.gov.justice.api.mapper.ListenerMediaTypeToSchemaIdMapper;
import uk.gov.justice.schema.catalog.CatalogProducer;
import uk.gov.justice.schema.service.SchemaCatalogService;
import uk.gov.justice.services.adapter.messaging.DefaultJmsParameterChecker;
import uk.gov.justice.services.adapter.messaging.DefaultJmsProcessor;
import uk.gov.justice.services.common.configuration.ServiceContextNameProvider;
import uk.gov.justice.services.common.converter.StringToJsonObjectConverter;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.justice.services.core.cdi.LoggerProducer;
import uk.gov.justice.services.core.extension.BeanInstantiater;
import uk.gov.justice.services.core.json.BackwardsCompatibleJsonSchemaValidator;
import uk.gov.justice.services.core.json.DefaultFileSystemUrlResolverStrategy;
import uk.gov.justice.services.core.json.DefaultJsonValidationLoggerHelper;
import uk.gov.justice.services.core.json.FileBasedJsonSchemaValidator;
import uk.gov.justice.services.core.json.JsonSchemaLoader;
import uk.gov.justice.services.core.json.PayloadExtractor;
import uk.gov.justice.services.core.json.SchemaCatalogAwareJsonSchemaValidator;
import uk.gov.justice.services.core.mapping.ActionNameToMediaTypesMappingObserver;
import uk.gov.justice.services.core.mapping.DefaultMediaTypesMappingCache;
import uk.gov.justice.services.core.mapping.DefaultNameToMediaTypeConverter;
import uk.gov.justice.services.core.mapping.DefaultSchemaIdMappingCache;
import uk.gov.justice.services.core.mapping.MediaTypeToSchemaIdMapper;
import uk.gov.justice.services.core.mapping.SchemaIdMappingObserver;
import uk.gov.justice.services.event.buffer.api.AllowAllEventFilter;
import uk.gov.justice.services.generators.test.utils.interceptor.RecordingInterceptorChainProcessor;
import uk.gov.justice.services.messaging.DefaultJsonObjectEnvelopeConverter;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.jms.DefaultEnvelopeConverter;
import uk.gov.justice.services.messaging.logging.DefaultJmsMessageLoggerHelper;
import uk.gov.justice.services.messaging.logging.DefaultTraceLogger;

import java.util.UUID;

import javax.annotation.Resource;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.jms.JMSException;
import javax.jms.Topic;

import org.apache.openejb.jee.WebApp;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.testing.Classes;
import org.apache.openejb.testing.Module;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Integration tests for the generated JAX-RS classes.
 */
@RunWith(ApplicationComposer.class)
public class JmsEndpointGenerationCustomIT extends AbstractJmsAdapterGenerationIT {
    @Module
    @Classes(cdi = true, value = {
            DefaultJmsProcessor.class,
            RecordingInterceptorChainProcessor.class,
            CustomEventListenerEventFilter.class,
            CustomEventListenerPeopleEventJmsListener.class,
            CustomEventValidationInterceptor.class,
            ObjectMapperProducer.class,
            DefaultEnvelopeConverter.class,
            StringToJsonObjectConverter.class,
            DefaultJsonObjectEnvelopeConverter.class,
            FileBasedJsonSchemaValidator.class,
            JsonSchemaLoader.class,
            LoggerProducer.class,
            AllowAllEventFilter.class,
            DefaultJmsParameterChecker.class,
            TestServiceContextNameProvider.class,
            DefaultJmsMessageLoggerHelper.class,
            DefaultTraceLogger.class,
            DefaultJsonValidationLoggerHelper.class,
            DefaultFileSystemUrlResolverStrategy.class,

            ListenerMediaTypeToSchemaIdMapper.class,
            SchemaCatalogAwareJsonSchemaValidator.class,
            PayloadExtractor.class,
            DefaultNameToMediaTypeConverter.class,

            DefaultSchemaIdMappingCache.class,
            SchemaIdMappingObserver.class,
            MediaTypeToSchemaIdMapper.class,
            BeanInstantiater.class,
            MediaTypeToSchemaIdMapper.class,

            CatalogProducer.class,
            SchemaCatalogService.class,

            DefaultMediaTypesMappingCache.class,
            ActionNameToMediaTypesMappingObserver.class,

            BackwardsCompatibleJsonSchemaValidator.class
    })
    public WebApp war() {
        return new WebApp()
                .contextRoot("jms-endpoint-test");
    }

    @Inject
    RecordingInterceptorChainProcessor interceptorChainProcessor;

    @Resource(name = "people.event")
    private Topic peopleEventsDestination;

    @Test
    public void eventListenerDispatcherShouldReceiveCustomEventSpecifiedInMessageSelector()
            throws JMSException, InterruptedException {

        Thread.sleep(300);
        final String metadataId = "861c9430-7bc6-4bf0-b549-6534b3457c56";
        final String eventName = "people.eventbb";

        sendEnvelope(metadataId, eventName, peopleEventsDestination);
        final JsonEnvelope receivedEnvelope = interceptorChainProcessor.awaitForEnvelopeWithMetadataOf("id", metadataId);
        assertThat(receivedEnvelope.metadata().id(), is(UUID.fromString(metadataId)));
        assertThat(receivedEnvelope.metadata().name(), is(eventName));

    }

    @ApplicationScoped
    public static class TestServiceContextNameProvider implements ServiceContextNameProvider {

        @Override
        public String getServiceContextName() {
            return "test-component";
        }
    }
}
