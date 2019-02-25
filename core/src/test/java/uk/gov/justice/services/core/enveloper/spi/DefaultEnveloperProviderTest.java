package uk.gov.justice.services.core.enveloper.spi;

import static javax.ejb.embeddable.EJBContainer.createEJBContainer;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static uk.gov.justice.services.messaging.Envelope.envelopeFrom;

import uk.gov.justice.domain.annotation.Event;
import uk.gov.justice.schema.service.CatalogProducer;
import uk.gov.justice.schema.service.SchemaCatalogResolverProducer;
import uk.gov.justice.schema.service.SchemaCatalogService;
import uk.gov.justice.services.cdi.LoggerProducer;
import uk.gov.justice.services.common.configuration.GlobalValueProducer;
import uk.gov.justice.services.common.converter.ObjectToJsonValueConverter;
import uk.gov.justice.services.common.converter.StringToJsonObjectConverter;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.justice.services.core.accesscontrol.AccessControlFailureMessageGenerator;
import uk.gov.justice.services.core.accesscontrol.AllowAllPolicyEvaluator;
import uk.gov.justice.services.core.accesscontrol.DefaultAccessControlService;
import uk.gov.justice.services.core.accesscontrol.PolicyEvaluator;
import uk.gov.justice.services.core.dispatcher.DispatcherCache;
import uk.gov.justice.services.core.dispatcher.DispatcherFactory;
import uk.gov.justice.services.core.dispatcher.EmptySystemUserProvider;
import uk.gov.justice.services.core.dispatcher.EnvelopePayloadTypeConverter;
import uk.gov.justice.services.core.dispatcher.JsonEnvelopeRepacker;
import uk.gov.justice.services.core.dispatcher.ServiceComponentObserver;
import uk.gov.justice.services.core.dispatcher.SystemUserUtil;
import uk.gov.justice.services.core.envelope.EnvelopeInspector;
import uk.gov.justice.services.core.envelope.EnvelopeValidationExceptionHandlerProducer;
import uk.gov.justice.services.core.envelope.MediaTypeProvider;
import uk.gov.justice.services.core.enveloper.DefaultEnveloper;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.core.extension.BeanInstantiater;
import uk.gov.justice.services.core.extension.EventFoundEvent;
import uk.gov.justice.services.core.extension.ServiceComponentScanner;
import uk.gov.justice.services.core.interceptor.InterceptorCache;
import uk.gov.justice.services.core.interceptor.InterceptorChainProcessor;
import uk.gov.justice.services.core.interceptor.InterceptorChainProcessorProducer;
import uk.gov.justice.services.core.it.AllEventsHandlerIT;
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
import uk.gov.justice.services.core.requester.RequesterProducer;
import uk.gov.justice.services.core.sender.SenderProducer;
import uk.gov.justice.services.messaging.DefaultJsonObjectEnvelopeConverter;
import uk.gov.justice.services.messaging.Envelope;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.Metadata;
import uk.gov.justice.services.messaging.jms.DefaultEnvelopeConverter;
import uk.gov.justice.services.messaging.jms.DefaultJmsEnvelopeSender;
import uk.gov.justice.services.messaging.logging.DefaultTraceLogger;
import uk.gov.justice.services.messaging.spi.DefaultEnvelope;
import uk.gov.justice.services.test.utils.common.validator.DummyJsonSchemaValidator;

import java.util.UUID;
import java.util.function.Function;

import javax.ejb.embeddable.EJBContainer;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.apache.openejb.jee.Application;
import org.apache.openejb.jee.WebApp;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.testing.Classes;
import org.apache.openejb.testing.Module;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(ApplicationComposer.class)
public class DefaultEnveloperProviderTest {

    private static final String TEST_EVENT_NAME = "test.event.something-happened";

    @Module
    @Classes(cdi = true, value = {
            AllEventsHandlerIT.AbcEventHandler.class,
            AllEventsHandlerIT.AllEventsHandler.class,
            ServiceComponentScanner.class,
            RequesterProducer.class,
            ServiceComponentObserver.class,
            DispatcherFactory.class,
            EnvelopePayloadTypeConverter.class,
            JsonEnvelopeRepacker.class,

            InterceptorChainProcessorProducer.class,
            InterceptorChainProcessor.class,
            InterceptorCache.class,
            AllEventsHandlerIT.EventListenerInterceptorChainProvider.class,

            SenderProducer.class,
            DefaultJmsEnvelopeSender.class,
            DefaultEnvelopeConverter.class,

            StringToJsonObjectConverter.class,
            DefaultJsonObjectEnvelopeConverter.class,
            ObjectToJsonValueConverter.class,
            Enveloper.class,

            AccessControlFailureMessageGenerator.class,
            AllowAllPolicyEvaluator.class,
            DefaultAccessControlService.class,
            DispatcherCache.class,
            PolicyEvaluator.class,

            EnvelopeValidationExceptionHandlerProducer.class,
            GlobalValueProducer.class,
            FileBasedJsonSchemaValidator.class,
            JsonSchemaLoader.class,

            LoggerProducer.class,
            EmptySystemUserProvider.class,
            SystemUserUtil.class,
            BeanInstantiater.class,
            UtcClock.class,
            DefaultTraceLogger.class,

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
            EnvelopeInspector.class,
            RequesterProducer.class,
            DummyJsonSchemaValidator.class,
            MediaTypesMappingCacheInitialiser.class,
            SchemaIdMappingCacheInitialiser.class,
            DefaultEnveloper.class,
            ObjectMapperProducer.class
    })
    public WebApp war() {
        return new WebApp()
                .contextRoot("DefaultEnveloperProviderTest")
                .addServlet("TestApp", Application.class.getName());
    }

    @Test
    public void shouldEnvelopWithDefaultEnvelope() {

        final Metadata metadata = createMetadata();

        final TestPojo payload = new TestPojo(TEST_EVENT_NAME);

        final Envelope<TestPojo> envelope = envelopeFrom(metadata, payload);

        final Envelope<TestPojo> resultEnvelope = new DefaultEnveloperProvider().envelop(payload).withName(TEST_EVENT_NAME).withMetadataFrom(envelope);

        assertThat(resultEnvelope, instanceOf(DefaultEnvelope.class));
        assertThat(resultEnvelope.metadata().name(), is(metadata.name()));
        assertThat(resultEnvelope.payload(), is(payload));
    }

    @Test
    public void shouldDelegateToDefaultEnveloperFromCdiToEnvelopeWithMetadateFrom() throws Exception {
        final DefaultEnveloper enveloper = new DefaultEnveloper(
                new UtcClock(),
                new ObjectToJsonValueConverter(new ObjectMapperProducer().objectMapper()));

        enveloper.register(new EventFoundEvent(DefaultEnveloperProviderTest.TestEvent.class, TEST_EVENT_NAME));

        final Metadata metadata = createMetadata();
        final TestPojo payload = new TestPojo(TEST_EVENT_NAME);

        final Envelope<TestPojo> envelope = envelopeFrom(metadata, payload);

        final Function<Object, JsonEnvelope> envelope1 = new DefaultEnveloperProvider().toEnvelopeWithMetadataFrom(envelope);
        final TestEvent testEvent = new TestEvent();

        assertThat(envelope1.apply(testEvent).metadata().name(), is(metadata.name()));
        assertThat(envelope.payload(), is(payload));
    }

    @Event("Test-Event")
    public static class TestEvent {

        private String somePayloadKey;

        public TestEvent(final String somePayloadKey) {
            this.somePayloadKey = somePayloadKey;
        }

        public TestEvent() {
        }

        public String getSomePayloadKey() {
            return somePayloadKey;
        }
    }

    private Metadata createMetadata() {
        return JsonEnvelope.metadataBuilder()
                .withName(TEST_EVENT_NAME)
                .withId(UUID.randomUUID())
                .withClientCorrelationId("asdsfd")
                .build();
    }

    class TestPojo {
        String name = TEST_EVENT_NAME;

        public TestPojo(final String name) {
            this.name = name;
        }

        public void setName(final String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

}
