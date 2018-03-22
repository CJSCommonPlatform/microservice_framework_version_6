package uk.gov.justice.subscription.jms.core;

import static org.raml.model.ActionType.POST;

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
import uk.gov.justice.services.generators.commons.validator.RequestContentTypeRamlValidator;
import uk.gov.justice.subscription.jms.interceptor.EventFilterInterceptorCodeGenerator;
import uk.gov.justice.subscription.jms.interceptor.EventListenerInterceptorChainProviderCodeGenerator;
import uk.gov.justice.subscription.jms.interceptor.EventValidationInterceptorCodeGenerator;

public class JmsEndpointGenerationObjects {

    public JmsEndpointGenerator jmsEndpointGenerator() {
        return new JmsEndpointGenerator(
                compositeRamlValidator(),
                subscriptionJmsEndpointGenerator(),
                ramlToJmsSubscriptionConverter()
        );
    }

    public CompositeRamlValidator compositeRamlValidator() {
        return new CompositeRamlValidator(
                containsResourcesRamlValidator(),
                containsActionsRamlValidator(),
                requestContentTypeRamlValidator(),
                baseUriRamlValidator()
        );
    }

    public ContainsResourcesRamlValidator containsResourcesRamlValidator() {
        return new ContainsResourcesRamlValidator();
    }

    public ContainsActionsRamlValidator containsActionsRamlValidator() {
        return new ContainsActionsRamlValidator();
    }

    public RequestContentTypeRamlValidator requestContentTypeRamlValidator() {
        return new RequestContentTypeRamlValidator(POST);
    }

    public BaseUriRamlValidator baseUriRamlValidator() {
        return new BaseUriRamlValidator();
    }

    public SubscriptionJmsEndpointGenerator subscriptionJmsEndpointGenerator() {
        return new SubscriptionJmsEndpointGenerator(
                messageListenerCodeGenerator(),
                eventFilterCodeGenerator(),
                subscriptionMediaTypeToSchemaIdGenerator(),
                eventFilterInterceptorCodeGenerator(),
                eventValidationInterceptorCodeGenerator(),
                eventListenerInterceptorChainProviderCodeGenerator());
    }

    public MessageListenerCodeGenerator messageListenerCodeGenerator() {
        return new MessageListenerCodeGenerator();
    }

    public EventFilterCodeGenerator eventFilterCodeGenerator() {
        return new EventFilterCodeGenerator();
    }

    public SubscriptionMediaTypeToSchemaIdGenerator subscriptionMediaTypeToSchemaIdGenerator() {
        return new SubscriptionMediaTypeToSchemaIdGenerator();
    }

    public EventFilterInterceptorCodeGenerator eventFilterInterceptorCodeGenerator() {
        return new EventFilterInterceptorCodeGenerator();
    }

    public EventValidationInterceptorCodeGenerator eventValidationInterceptorCodeGenerator() {
        return new EventValidationInterceptorCodeGenerator();
    }

    public EventListenerInterceptorChainProviderCodeGenerator eventListenerInterceptorChainProviderCodeGenerator() {
        return new EventListenerInterceptorChainProviderCodeGenerator();
    }

    public RamlToJmsSubscriptionConverter ramlToJmsSubscriptionConverter() {
        return new RamlToJmsSubscriptionConverter(
                subscriptionNamesGenerator(),
                resourcesListToSubscriptionListConverter()
        );
    }

    public ResourcesListToSubscriptionListConverter resourcesListToSubscriptionListConverter() {
        return new ResourcesListToSubscriptionListConverter(ramlResourceToSubscriptionConverter());
    }

    public SubscriptionNamesGenerator subscriptionNamesGenerator() {
        return new SubscriptionNamesGenerator();
    }

    public RamlResourceToSubscriptionConverter ramlResourceToSubscriptionConverter() {
        return new RamlResourceToSubscriptionConverter(
                subscriptionNamesGenerator(),
                ramlMimeTypeListToEventListConverter(),
                jmsUriGenerator()
        );
    }

    public JmsUriGenerator jmsUriGenerator() {
        return new JmsUriGenerator();
    }

    public RamlMimeTypeListToEventListConverter ramlMimeTypeListToEventListConverter() {
        return new RamlMimeTypeListToEventListConverter(
                mimeTypeToEventConverter());
    }

    public MimeTypeToEventConverter mimeTypeToEventConverter() {
        return new MimeTypeToEventConverter(
                eventNameExtractor(),
                schemaIdParser()
        );
    }

    public EventNameExtractor eventNameExtractor() {
        return new EventNameExtractor();
    }

    public SchemaIdParser schemaIdParser() {
        return new SchemaIdParser();
    }
}
