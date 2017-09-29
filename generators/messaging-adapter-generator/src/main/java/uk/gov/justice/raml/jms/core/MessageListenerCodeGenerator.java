package uk.gov.justice.raml.jms.core;

import static com.squareup.javapoet.TypeSpec.classBuilder;
import static java.lang.String.format;
import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.PUBLIC;
import static javax.lang.model.element.Modifier.STATIC;
import static uk.gov.justice.raml.jms.core.MediaTypesUtil.containsGeneralJsonMimeType;
import static uk.gov.justice.raml.jms.core.MediaTypesUtil.mediaTypesFrom;
import static uk.gov.justice.services.generators.commons.helper.Names.namesListStringFrom;

import uk.gov.justice.raml.core.GeneratorConfig;
import uk.gov.justice.services.adapter.messaging.JmsLoggerMetadataInterceptor;
import uk.gov.justice.services.adapter.messaging.JmsProcessor;
import uk.gov.justice.services.adapter.messaging.MessagingJsonSchemaValidationService;
import uk.gov.justice.services.core.annotation.Adapter;
import uk.gov.justice.services.core.annotation.Component;
import uk.gov.justice.services.core.interceptor.InterceptorChainProcessor;
import uk.gov.justice.services.generators.commons.helper.MessagingAdapterBaseUri;
import uk.gov.justice.services.generators.commons.helper.MessagingResourceUri;
import uk.gov.justice.services.messaging.logging.LoggerUtils;

import java.util.Map;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.inject.Inject;
import javax.interceptor.Interceptors;
import javax.jms.Destination;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.Topic;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeSpec;
import org.jboss.ejb3.annotation.Pool;
import org.raml.model.Action;
import org.raml.model.ActionType;
import org.raml.model.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Internal code generation class for generating the JMS {@link MessageListener} that listens to the
 * message types described in the RAML.
 */
class MessageListenerCodeGenerator {

    private static final String DEFAULT_ANNOTATION_PARAMETER = "value";
    private static final String ACTIVATION_CONFIG_PARAMETER = "activationConfig";
    private static final String INTERCEPTOR_CHAIN_PROCESS = "interceptorChainProcessor";
    private static final String JSON_SCHEMA_VALIDATION_SERVICE = "jsonSchemaValidationService";
    private static final String JMS_PROCESSOR_FIELD = "jmsProcessor";
    private static final String LOGGER_FIELD = "LOGGER";

    private static final String DESTINATION_TYPE = "destinationType";
    private static final String DESTINATION_LOOKUP = "destinationLookup";
    private static final String MESSAGE_SELECTOR = "messageSelector";
    private static final String SUBSCRIPTION_DURABILITY = "subscriptionDurability";
    private static final String DURABLE = "Durable";
    private static final String CLIENT_ID = "clientId";
    private static final String SUBSCRIPTION_NAME = "subscriptionName";
    private static final String SHARE_SUBSCRIPTIONS = "shareSubscriptions";
    private static final String CUSTOM_MDB_POOL_CONFIG_PROPERTY = "customMDBPool";
    private static final String TRUE = "TRUE";

    private final ComponentDestinationType componentDestinationType = new ComponentDestinationType();

    /**
     * Create an implementation of the {@link MessageListener}.
     *
     * @param resource the resource definition this listener is being generated for
     * @param baseUri  the base URI
     * @return the message listener class specification
     */
    TypeSpec generatedCodeFor(final Resource resource, final MessagingAdapterBaseUri baseUri, final boolean listenToAllMessages, final GeneratorConfig configuration) {
        return classSpecFrom(resource, baseUri, listenToAllMessages, configuration)
                .addMethod(generateOnMessageMethod(componentOf(baseUri), resource))
                .build();
    }

    /**
     * Generate the @link MessageListener} class implementation.
     *
     * @param resource               the resource definition this listener is being generated for
     * @param baseUri                the base URI
     * @param generatorConfiguration generator generatorConfiguration
     * @return the {@link TypeSpec.Builder} that defines the class
     */
    private TypeSpec.Builder classSpecFrom(final Resource resource,
                                           final MessagingAdapterBaseUri baseUri,
                                           final boolean listenToAllMessages,
                                           final GeneratorConfig generatorConfiguration) {
        final MessagingResourceUri resourceUri = new MessagingResourceUri(resource.getUri());
        final String component = componentOf(baseUri);

        if (componentDestinationType.isSupported(component)) {

            final TypeSpec.Builder typeSpecBuilder = classBuilder(classNameOf(baseUri, resourceUri))
                    .addModifiers(PUBLIC)
                    .addSuperinterface(MessageListener.class)
                    .addField(FieldSpec.builder(ClassName.get(Logger.class), LOGGER_FIELD)
                            .addModifiers(PRIVATE, STATIC, FINAL)
                            .initializer("$T.getLogger($L.class)", LoggerFactory.class, classNameOf(baseUri, resourceUri))
                            .build())
                    .addField(FieldSpec.builder(ClassName.get(InterceptorChainProcessor.class), INTERCEPTOR_CHAIN_PROCESS)
                            .addAnnotation(Inject.class)
                            .build())
                    .addField(FieldSpec.builder(ClassName.get(JmsProcessor.class), JMS_PROCESSOR_FIELD)
                            .addAnnotation(Inject.class)
                            .build())
                    .addAnnotation(AnnotationSpec.builder(Adapter.class)
                            .addMember(DEFAULT_ANNOTATION_PARAMETER, "$S", component)
                            .build())
                    .addAnnotation(messageDrivenAnnotation(component, resource.getActions(), resourceUri, baseUri, listenToAllMessages));

            if (!containsGeneralJsonMimeType(resource.getActions())) {
                typeSpecBuilder.addAnnotation(AnnotationSpec.builder(Interceptors.class)
                        .addMember(DEFAULT_ANNOTATION_PARAMETER, "$T.class", JmsLoggerMetadataInterceptor.class)
                        .build());
                typeSpecBuilder.addField(FieldSpec.builder(ClassName.get(MessagingJsonSchemaValidationService.class), JSON_SCHEMA_VALIDATION_SERVICE)
                        .addAnnotation(Inject.class)
                        .build());
            }

            if (shouldAddCustomPoolConfiguration(generatorConfiguration)) {
                typeSpecBuilder.addAnnotation(AnnotationSpec.builder(Pool.class)
                        .addMember(DEFAULT_ANNOTATION_PARAMETER, "$S", poolNameFrom(resourceUri, component))
                        .build());
            }

            return typeSpecBuilder;
        }

        throw new IllegalStateException(format("JMS Endpoint generation is unsupported for framework component type %s", component));
    }

    private String poolNameFrom(final MessagingResourceUri resourceUri, final String component) {
        return format("%s-%s-pool", resourceUri.hyphenated(), component.toLowerCase().replace("_", "-"));
    }

    private boolean shouldAddCustomPoolConfiguration(final GeneratorConfig configuration) {
        return TRUE.equalsIgnoreCase(configuration.getGeneratorProperties().get(CUSTOM_MDB_POOL_CONFIG_PROPERTY));
    }

    /**
     * Generate the onMessage method that processes a JMS message.
     *
     * @param component - the component the class belongs to.
     * @return the {@link MethodSpec} that represents the onMessage method
     */
    private MethodSpec generateOnMessageMethod(final String component, Resource resource) {

        final String messageFieldName = "message";

        final CodeBlock codeBlock = generateCodeBlock(component, resource, messageFieldName);

        return MethodSpec.methodBuilder("onMessage")
                .addModifiers(PUBLIC)
                .addAnnotation(Override.class)
                .addParameter(ParameterSpec
                        .builder(Message.class, messageFieldName)
                        .build())
                .addCode(codeBlock)
                .build();
    }

    private CodeBlock generateCodeBlock(final String component, final Resource resource, final String messageFieldName) {
        CodeBlock.Builder codeBlockBuilder = CodeBlock.builder()
                .addStatement("$T.trace(LOGGER, () -> \"Received JMS message\")", LoggerUtils.class);

        if (!containsGeneralJsonMimeType(resource.getActions())) {
            codeBlockBuilder.addStatement("$L.validate($L, \"$L\")", JSON_SCHEMA_VALIDATION_SERVICE, messageFieldName, component);
        }

        return codeBlockBuilder.addStatement("$L.process($L::process, $L)", JMS_PROCESSOR_FIELD, INTERCEPTOR_CHAIN_PROCESS, messageFieldName).build();
    }

    /**
     * Generate the @MessageDriven annotation
     *
     * @param component   the service component
     * @param actions     a map of actions for building the message selector
     * @param resourceUri the resource URI
     * @param baseUri     the base URI
     * @return the annotation specification
     */
    private AnnotationSpec messageDrivenAnnotation(final String component,
                                                   final Map<ActionType, Action> actions,
                                                   final MessagingResourceUri resourceUri,
                                                   final MessagingAdapterBaseUri baseUri, final boolean listenToAllMessages) {

        final Class<? extends Destination> inputType = componentDestinationType.inputTypeFor(component);

        AnnotationSpec.Builder builder = AnnotationSpec.builder(MessageDriven.class)
                .addMember(ACTIVATION_CONFIG_PARAMETER, "$L",
                        generateActivationConfigPropertyAnnotation(DESTINATION_TYPE, inputType.getName()))
                .addMember(ACTIVATION_CONFIG_PARAMETER, "$L",
                        generateActivationConfigPropertyAnnotation(DESTINATION_LOOKUP, resourceUri.destinationName()))
                .addMember(ACTIVATION_CONFIG_PARAMETER, "$L",
                        generateActivationConfigPropertyAnnotation(SHARE_SUBSCRIPTIONS, "true"));


        if (!listenToAllMessages) {
            builder.addMember(ACTIVATION_CONFIG_PARAMETER, "$L",
                    generateActivationConfigPropertyAnnotation(MESSAGE_SELECTOR, messageSelectorsFrom(actions)));
        }

        if (Topic.class.equals(inputType)) {
            final String clientId = baseUri.adapterClientId();
            builder = builder
                    .addMember(ACTIVATION_CONFIG_PARAMETER, "$L",
                            generateActivationConfigPropertyAnnotation(SUBSCRIPTION_DURABILITY, DURABLE))
                    .addMember(ACTIVATION_CONFIG_PARAMETER, "$L",
                            generateActivationConfigPropertyAnnotation(CLIENT_ID, clientId))
                    .addMember(ACTIVATION_CONFIG_PARAMETER, "$L",
                            generateActivationConfigPropertyAnnotation(SUBSCRIPTION_NAME, subscriptionNameOf(resourceUri, clientId)));
        }

        return builder.build();
    }

    /**
     * Generate a single @ActivationConfigProperty annotation to add to the @MessageDriven
     * annotation.
     *
     * @param name  the property name
     * @param value the property value
     * @return the annotation specification
     */
    private AnnotationSpec generateActivationConfigPropertyAnnotation(final String name, final String value) {
        return AnnotationSpec.builder(ActivationConfigProperty.class)
                .addMember("propertyName", "$S", name)
                .addMember("propertyValue", "$S", value)
                .build();
    }

    /**
     * Convert given URI and component to a camel cased class name
     *
     * @param baseUri URI String to convert
     * @return camel case class name
     */
    private String classNameOf(final MessagingAdapterBaseUri baseUri, final MessagingResourceUri resourceUri) {
        return format("%s%sJmsListener", baseUri.toClassName(), resourceUri.toClassName());
    }

    /**
     * Convert given URI to a valid component.
     *
     * @param baseUri base uri of the resource
     * @return component the value of the pillar and tier parts of the uri
     */
    private String componentOf(final MessagingAdapterBaseUri baseUri) {
        return Component.valueOf(baseUri.pillar(), baseUri.tier());
    }

    /**
     * Parse and format all the message selectors from the Post Action
     *
     * @param actions Map of ActionType to Action
     * @return formatted message selector String
     */
    private String messageSelectorsFrom(final Map<ActionType, Action> actions) {
        return format("CPPNAME in('%s')", namesListStringFrom(mediaTypesFrom(actions), "','"));
    }


    /**
     * Generate the subscription name for a resource.
     *
     * @param resourceUri the URI of the resource
     * @param clientId    the previously generated client id
     * @return the subscription name
     */
    private String subscriptionNameOf(final MessagingResourceUri resourceUri, final String clientId) {
        return format("%s.%s", clientId, resourceUri.destinationName());
    }
}
