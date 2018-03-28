package uk.gov.justice.subscription.jms.core;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import uk.gov.justice.raml.jms.converters.EventNameExtractor;
import uk.gov.justice.raml.jms.converters.JmsUriGenerator;
import uk.gov.justice.raml.jms.converters.MimeTypeToEventConverter;
import uk.gov.justice.raml.jms.converters.RamlMimeTypeListToEventListConverter;
import uk.gov.justice.raml.jms.converters.RamlResourceToSubscriptionConverter;
import uk.gov.justice.raml.jms.converters.RamlToJmsSubscriptionConverter;
import uk.gov.justice.raml.jms.converters.ResourcesListToSubscriptionListConverter;
import uk.gov.justice.raml.jms.converters.SubscriptionNamesGenerator;
import uk.gov.justice.raml.jms.core.JmsEndpointGenerator;
import uk.gov.justice.raml.jms.validator.BaseUriRamlValidator;
import uk.gov.justice.services.generators.commons.mapping.SchemaIdParser;
import uk.gov.justice.services.generators.commons.mapping.SubscriptionMediaTypeToSchemaIdGenerator;
import uk.gov.justice.services.generators.commons.validator.CompositeRamlValidator;
import uk.gov.justice.services.generators.commons.validator.ContainsActionsRamlValidator;
import uk.gov.justice.services.generators.commons.validator.ContainsResourcesRamlValidator;
import uk.gov.justice.services.generators.commons.validator.RamlValidator;
import uk.gov.justice.services.generators.commons.validator.RequestContentTypeRamlValidator;
import uk.gov.justice.subscription.jms.interceptor.EventFilterInterceptorCodeGenerator;
import uk.gov.justice.subscription.jms.interceptor.EventListenerInterceptorChainProviderCodeGenerator;
import uk.gov.justice.subscription.jms.interceptor.EventValidationInterceptorCodeGenerator;

import java.lang.reflect.Field;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;


@RunWith(MockitoJUnitRunner.class)
public class JmsEndpointGenerationObjectsTest {

    @InjectMocks
    private JmsEndpointGenerationObjects jmsEndpointGenerationObjects;

    @Test
    public void shouldCreateAJmsEndpointGenerator() throws Exception {

        final JmsEndpointGenerator jmsEndpointGenerator = jmsEndpointGenerationObjects.jmsEndpointGenerator();

        final RamlValidator ramlValidator = getPrivateFieldFrom(jmsEndpointGenerator, "ramlValidator", RamlValidator.class);
        final SubscriptionJmsEndpointGenerator subscriptionJmsEndpointGenerator = getPrivateFieldFrom(jmsEndpointGenerator, "subscriptionJmsEndpointGenerator", SubscriptionJmsEndpointGenerator.class);
        final RamlToJmsSubscriptionConverter ramlToJmsSubscriptionConverter = getPrivateFieldFrom(jmsEndpointGenerator, "ramlToJmsSubscriptionConverter", RamlToJmsSubscriptionConverter.class);

        assertThat(ramlValidator, is(notNullValue()));
        assertThat(subscriptionJmsEndpointGenerator, is(notNullValue()));
        assertThat(ramlToJmsSubscriptionConverter, is(notNullValue()));
    }

    @Test
    public void shouldCreateACompositeRamlValidator() throws Exception {

        final CompositeRamlValidator compositeRamlValidator = jmsEndpointGenerationObjects.compositeRamlValidator();

        final RamlValidator[] ramlValidators = getPrivateFieldFrom(compositeRamlValidator, "validators", RamlValidator[].class);

        assertThat(ramlValidators, is(notNullValue()));

        assertThat(ramlValidators.length, is(4));

        assertThat(ramlValidators[0], is(instanceOf(ContainsResourcesRamlValidator.class)));
        assertThat(ramlValidators[1], is(instanceOf(ContainsActionsRamlValidator.class)));
        assertThat(ramlValidators[2], is(instanceOf(RequestContentTypeRamlValidator.class)));
        assertThat(ramlValidators[3], is(instanceOf(BaseUriRamlValidator.class)));
    }

    @Test
    public void shouldCreateAContainsResourcesRamlValidator() throws Exception {
        final ContainsResourcesRamlValidator containsResourcesRamlValidator = jmsEndpointGenerationObjects.containsResourcesRamlValidator();
        assertThat(containsResourcesRamlValidator, is(notNullValue()));
    }

    @Test
    public void shouldCreateAContainsActionsRamlValidator() throws Exception {
        final ContainsActionsRamlValidator containsActionsRamlValidator = jmsEndpointGenerationObjects.containsActionsRamlValidator();
        assertThat(containsActionsRamlValidator, is(notNullValue()));
    }

    @Test
    public void shouldCreateARequestContentTypeRamlValidator() throws Exception {
        final RequestContentTypeRamlValidator requestContentTypeRamlValidator = jmsEndpointGenerationObjects.requestContentTypeRamlValidator();
        assertThat(requestContentTypeRamlValidator, is(notNullValue()));
    }

    @Test
    public void shouldCreateABaseUriRamlValidator() throws Exception {
        final BaseUriRamlValidator baseUriRamlValidator = jmsEndpointGenerationObjects.baseUriRamlValidator();
        assertThat(baseUriRamlValidator, is(notNullValue()));
    }

    @Test
    public void shouldCreateASubscriptionJmsEndpointGenerator() throws Exception {

        final SubscriptionJmsEndpointGenerator jmsEndpointGenerator = jmsEndpointGenerationObjects.subscriptionJmsEndpointGenerator();

        final MessageListenerCodeGenerator ramlValidator = getPrivateFieldFrom(jmsEndpointGenerator, "messageListenerCodeGenerator", MessageListenerCodeGenerator.class);
        final EventFilterCodeGenerator eventFilterCodeGenerator = getPrivateFieldFrom(jmsEndpointGenerator, "eventFilterCodeGenerator", EventFilterCodeGenerator.class);
        final SubscriptionMediaTypeToSchemaIdGenerator subscriptionMediaTypeToSchemaIdGenerator = getPrivateFieldFrom(jmsEndpointGenerator, "subscriptionMediaTypeToSchemaIdGenerator", SubscriptionMediaTypeToSchemaIdGenerator.class);
        final EventFilterInterceptorCodeGenerator eventFilterInterceptorCodeGenerator = getPrivateFieldFrom(jmsEndpointGenerator, "eventFilterInterceptorCodeGenerator", EventFilterInterceptorCodeGenerator.class);
        final EventValidationInterceptorCodeGenerator eventValidationInterceptorCodeGenerator = getPrivateFieldFrom(jmsEndpointGenerator, "eventValidationInterceptorCodeGenerator", EventValidationInterceptorCodeGenerator.class);
        final EventListenerInterceptorChainProviderCodeGenerator eventListenerInterceptorChainProviderCodeGenerator = getPrivateFieldFrom(jmsEndpointGenerator, "eventListenerInterceptorChainProviderCodeGenerator", EventListenerInterceptorChainProviderCodeGenerator.class);

        assertThat(ramlValidator, is(notNullValue()));
        assertThat(eventFilterCodeGenerator, is(notNullValue()));
        assertThat(subscriptionMediaTypeToSchemaIdGenerator, is(notNullValue()));
        assertThat(eventFilterInterceptorCodeGenerator, is(notNullValue()));
        assertThat(eventValidationInterceptorCodeGenerator, is(notNullValue()));
        assertThat(eventListenerInterceptorChainProviderCodeGenerator, is(notNullValue()));
    }

    @Test
    public void shouldCreateAMessageListenerCodeGenerator() throws Exception {
        final MessageListenerCodeGenerator messageListenerCodeGenerator = jmsEndpointGenerationObjects.messageListenerCodeGenerator();
        assertThat(messageListenerCodeGenerator, is(notNullValue()));
    }

    @Test
    public void shouldCreateAnEventFilterCodeGenerator() throws Exception {
        final EventFilterCodeGenerator eventFilterCodeGenerator = jmsEndpointGenerationObjects.eventFilterCodeGenerator();
        assertThat(eventFilterCodeGenerator, is(notNullValue()));
    }

    @Test
    public void shouldCreateASubscriptionMediaTypeToSchemaIdGenerator() throws Exception {
        final SubscriptionMediaTypeToSchemaIdGenerator subscriptionMediaTypeToSchemaIdGenerator = jmsEndpointGenerationObjects.subscriptionMediaTypeToSchemaIdGenerator();
        assertThat(subscriptionMediaTypeToSchemaIdGenerator, is(notNullValue()));
    }

    @Test
    public void shouldCreateAnEventFilterInterceptorCodeGenerator() throws Exception {
        final EventFilterInterceptorCodeGenerator eventFilterInterceptorCodeGenerator = jmsEndpointGenerationObjects.eventFilterInterceptorCodeGenerator();
        assertThat(eventFilterInterceptorCodeGenerator, is(notNullValue()));
    }

    @Test
    public void shouldCreateAnEventValidationInterceptorCodeGenerator() throws Exception {
        final EventValidationInterceptorCodeGenerator eventValidationInterceptorCodeGenerator = jmsEndpointGenerationObjects.eventValidationInterceptorCodeGenerator();
        assertThat(eventValidationInterceptorCodeGenerator, is(notNullValue()));
    }

    @Test
    public void shouldCreateAnEventListenerInterceptorChainProviderCodeGenerator() throws Exception {
        final EventListenerInterceptorChainProviderCodeGenerator eventListenerInterceptorChainProviderCodeGenerator = jmsEndpointGenerationObjects.eventListenerInterceptorChainProviderCodeGenerator();
        assertThat(eventListenerInterceptorChainProviderCodeGenerator, is(notNullValue()));
    }

    @Test
    public void shouldCreateARamlToJmsSubscriptionConverter() throws Exception {
        final RamlToJmsSubscriptionConverter ramlToJmsSubscriptionConverter = jmsEndpointGenerationObjects.ramlToJmsSubscriptionConverter();
        final SubscriptionNamesGenerator subscriptionNamesGenerator = getPrivateFieldFrom(
                ramlToJmsSubscriptionConverter,
                "subscriptionNamesGenerator",
                SubscriptionNamesGenerator.class);
        final ResourcesListToSubscriptionListConverter resourcesListToSubscriptionListConverter = getPrivateFieldFrom(
                ramlToJmsSubscriptionConverter,
                "resourcesListToSubscriptionListConverter",
                ResourcesListToSubscriptionListConverter.class);

        assertThat(subscriptionNamesGenerator, is(notNullValue()));
        assertThat(resourcesListToSubscriptionListConverter, is(notNullValue()));
    }

    @Test
    public void shouldCreateAResourcesListToSubscriptionListConverter() throws Exception {
        final ResourcesListToSubscriptionListConverter resourcesListToSubscriptionListConverter = jmsEndpointGenerationObjects.resourcesListToSubscriptionListConverter();
        assertThat(resourcesListToSubscriptionListConverter, is(notNullValue()));
    }

    @Test
    public void shouldCreateASubscriptionNamesGenerator() throws Exception {
        final SubscriptionNamesGenerator subscriptionNamesGenerator = jmsEndpointGenerationObjects.subscriptionNamesGenerator();
        assertThat(subscriptionNamesGenerator, is(notNullValue()));
    }

    @Test
    public void shouldCreateARamlResourceToSubscriptionConverter() throws Exception {

        final RamlResourceToSubscriptionConverter ramlResourceToSubscriptionConverter = jmsEndpointGenerationObjects.ramlResourceToSubscriptionConverter();

        final SubscriptionNamesGenerator subscriptionNamesGenerator = getPrivateFieldFrom(
                ramlResourceToSubscriptionConverter,
                "subscriptionNamesGenerator",
                SubscriptionNamesGenerator.class);

        final RamlMimeTypeListToEventListConverter ramlMimeTypeListToEventListConverter = getPrivateFieldFrom(
                ramlResourceToSubscriptionConverter,
                "ramlMimeTypeListToEventListConverter",
                RamlMimeTypeListToEventListConverter.class);

        final JmsUriGenerator jmsUriGenerator = getPrivateFieldFrom(
                ramlResourceToSubscriptionConverter,
                "jmsUriGenerator",
                JmsUriGenerator.class);

        assertThat(subscriptionNamesGenerator, is(notNullValue()));
        assertThat(ramlMimeTypeListToEventListConverter, is(notNullValue()));
        assertThat(jmsUriGenerator, is(notNullValue()));
    }

    @Test
    public void shouldCreateAJmsUriGenerator() throws Exception {
        final JmsUriGenerator jmsUriGenerator = jmsEndpointGenerationObjects.jmsUriGenerator();
        assertThat(jmsUriGenerator, is(notNullValue()));
    }

    @Test
    public void shouldCreateARamlMimeTypeListToEventListConverter() throws Exception {
        final RamlMimeTypeListToEventListConverter ramlMimeTypeListToEventListConverter = jmsEndpointGenerationObjects.ramlMimeTypeListToEventListConverter();

        final MimeTypeToEventConverter mimeTypeToEventConverter = getPrivateFieldFrom(
                ramlMimeTypeListToEventListConverter,
                "mimeTypeToEventConverter",
                MimeTypeToEventConverter.class);

        assertThat(mimeTypeToEventConverter, is(notNullValue()));
    }

    @Test
    public void shouldCreateAMimeTypeToEventConverter() throws Exception {
        final MimeTypeToEventConverter mimeTypeToEventConverter = jmsEndpointGenerationObjects.mimeTypeToEventConverter();

        final EventNameExtractor eventNameExtractor = getPrivateFieldFrom(
                mimeTypeToEventConverter,
                "eventNameExtractor",
                EventNameExtractor.class);

        final SchemaIdParser schemaIdParser = getPrivateFieldFrom(
                mimeTypeToEventConverter,
                "schemaIdParser",
                SchemaIdParser.class);

        assertThat(eventNameExtractor, is(notNullValue()));
        assertThat(schemaIdParser, is(notNullValue()));
    }

    @Test
    public void shouldCreateAnEventNameExtractor() throws Exception {
        final EventNameExtractor eventNameExtractor = jmsEndpointGenerationObjects.eventNameExtractor();
        assertThat(eventNameExtractor, is(notNullValue()));
    }

    @Test
    public void shouldCreateASchemaIdParser() throws Exception {
        final SchemaIdParser schemaIdParser = jmsEndpointGenerationObjects.schemaIdParser();
        assertThat(schemaIdParser, is(notNullValue()));
    }

    @SuppressWarnings("unchecked")
    private <T> T getPrivateFieldFrom(final Object obj, final String fieldName, @SuppressWarnings("unused") final Class<T> type) throws Exception {

        final Field field = obj.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);

        return (T) field.get(obj);
    }
}
