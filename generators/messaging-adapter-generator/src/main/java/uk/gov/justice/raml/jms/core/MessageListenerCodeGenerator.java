package uk.gov.justice.raml.jms.core;

import static com.squareup.javapoet.TypeSpec.classBuilder;
import static java.lang.String.format;
import static java.util.stream.Collectors.joining;
import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.PUBLIC;
import static javax.lang.model.element.Modifier.STATIC;
import static org.apache.commons.lang.StringUtils.capitalize;

import uk.gov.justice.raml.jms.uri.BaseUri;
import uk.gov.justice.services.adapter.messaging.JmsProcessor;
import uk.gov.justice.services.adapter.messaging.JsonSchemaValidationInterceptor;
import uk.gov.justice.services.core.annotation.Adapter;
import uk.gov.justice.services.core.annotation.Component;
import uk.gov.justice.services.core.dispatcher.AsynchronousDispatcher;
import uk.gov.justice.services.generators.commons.helper.MessagingResourceUri;
import uk.gov.justice.services.generators.commons.helper.Names;
import uk.gov.justice.services.messaging.logging.JmsMessageLoggerHelper;
import uk.gov.justice.services.messaging.logging.LoggerUtils;

import java.util.Map;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.inject.Inject;
import javax.interceptor.Interceptors;
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
    private static final String DISPATCHER_FIELD = "dispatcher";
    private static final String JMS_PROCESSOR_FIELD = "jmsProcessor";
    private static final String LOGGER_FIELD = "LOGGER";

    private static final String DESTINATION_TYPE = "destinationType";
    private static final String DESTINATION_LOOKUP = "destinationLookup";
    private static final String MESSAGE_SELECTOR = "messageSelector";
    private static final String SUBSCRIPTION_DURABILITY = "subscriptionDurability";
    private static final String DURABLE = "Durable";
    private static final String CLIENT_ID = "clientId";
    private static final String SUBSCRIPTION_NAME = "subscriptionName";

    /**
     * Create an implementation of the {@link MessageListener}.
     *
     * @param resource the resource definition this listener is being generated for
     * @param baseUri the base URI
     * @return the message listener class specification
     */
    TypeSpec generateFor(final Resource resource, final BaseUri baseUri) {
        return classSpecFrom(resource, baseUri)
                .addMethod(generateOnMessageMethod())
                .build();
    }

    /**
     * Generate the @link MessageListener} class implementation.
     * @param resource the resource definition this listener is being generated for
     * @param baseUri the base URI
     * @return the {@link TypeSpec.Builder} that defines the class
     */
    private TypeSpec.Builder classSpecFrom(final Resource resource, final BaseUri baseUri) {
        final MessagingResourceUri resourceUri = new MessagingResourceUri(resource.getUri());
        final Component component = componentOf(resourceUri, baseUri);

        return classBuilder(classNameOf(resourceUri, component))
                .addModifiers(PUBLIC)
                .addSuperinterface(MessageListener.class)
                .addField(FieldSpec.builder(ClassName.get(Logger.class), LOGGER_FIELD)
                        .addModifiers(PRIVATE, STATIC, FINAL)
                        .initializer("$T.getLogger($L.class)", LoggerFactory.class, classNameOf(resourceUri, component))
                        .build())
                .addField(FieldSpec.builder(ClassName.get(AsynchronousDispatcher.class), DISPATCHER_FIELD)
                        .addAnnotation(Inject.class)
                        .build())
                .addField(FieldSpec.builder(ClassName.get(JmsProcessor.class), JMS_PROCESSOR_FIELD)
                        .addAnnotation(Inject.class)
                        .build())
                .addAnnotation(AnnotationSpec.builder(Adapter.class)
                        .addMember(DEFAULT_ANNOTATION_PARAMETER, "$T.$L", Component.class, component)
                        .build())
                .addAnnotation(AnnotationSpec.builder(Interceptors.class)
                        .addMember(DEFAULT_ANNOTATION_PARAMETER, "$T.class", JsonSchemaValidationInterceptor.class)
                        .build())
                .addAnnotation(generateMessageDrivenAnnotation(component, resource.getActions(), resourceUri, baseUri));
    }

    /**
     * Generate the onMessage method that processes a JMS message.
     * @return the {@link MethodSpec} that represents the onMessage method
     */
    private MethodSpec generateOnMessageMethod() {

        final String messageFieldName = "message";

        return MethodSpec.methodBuilder("onMessage")
                .addModifiers(PUBLIC)
                .addAnnotation(Override.class)
                .addParameter(ParameterSpec
                        .builder(Message.class, messageFieldName)
                        .build())
                .addCode(CodeBlock.builder()
                        .addStatement("$T.trace(LOGGER, () -> $T.format(\"Received JMS message: %s\", $T.toJmsTraceString($L)))",
                                LoggerUtils.class, String.class, JmsMessageLoggerHelper.class, messageFieldName)
                        .addStatement("$L.process($L::dispatch, $L)", JMS_PROCESSOR_FIELD, DISPATCHER_FIELD, messageFieldName)
                        .build())
                .build();
    }

    /**
     * Generate the @MessageDriven annotation
     * @param component the service component
     * @param actions a map of actions for building the message selector
     * @param resourceUri the resource URI
     * @param baseUri the base URI
     * @return the annotation specification
     */
    private AnnotationSpec generateMessageDrivenAnnotation(final Component component,
                                                           final Map<ActionType, Action> actions,
                                                           final MessagingResourceUri resourceUri,
                                                           final BaseUri baseUri) {

        AnnotationSpec.Builder builder = AnnotationSpec.builder(MessageDriven.class)
            .addMember(ACTIVATION_CONFIG_PARAMETER, "$L",
                    generateActivationConfigPropertyAnnotation(DESTINATION_TYPE, component.inputDestinationType().getName()))
            .addMember(ACTIVATION_CONFIG_PARAMETER, "$L",
                    generateActivationConfigPropertyAnnotation(DESTINATION_LOOKUP, resourceUri.destinationName()))
            .addMember(ACTIVATION_CONFIG_PARAMETER, "$L",
                    generateActivationConfigPropertyAnnotation(MESSAGE_SELECTOR, messageSelectorsFrom(actions)));

        if (Topic.class.equals(component.inputDestinationType())) {
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
     * Generate a single @ActivationConfigProperty annotation to add to the @MessageDriven annotation.
     * @param name the property name
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
     * @param resourceUri URI String to convert
     * @return camel case class name
     */
    private String classNameOf(final MessagingResourceUri resourceUri, final Component component) {
        return format("%s%s%sJmsListener",
                capitalize(resourceUri.context()),
                capitalize(component.pillar()),
                capitalize(component.tier()));
    }

    /**
     * Convert given URI to a valid component.
     * Takes the last and second to last parts of the URI as the pillar and tier of the component.
     * @param resourceUri URI of the resource
     * @param baseUri     base uri of the resource
     * @return component the value of the pillar and tier parts of the uri
     */
    private Component componentOf(final MessagingResourceUri resourceUri, final BaseUri baseUri) {

        final String pillar = resourceUri.pillar();
        final String tier = resourceUri.tier() != null ? resourceUri.tier() : baseUri.tier();
        return Component.valueOf(pillar, tier);
    }

    /**
     * Parse and format all the message selectors from the Post Action
     * @param actions Map of ActionType to Action
     * @return formatted message selector String
     */
    private String messageSelectorsFrom(final Map<ActionType, Action> actions) {
        return format("CPPNAME in('%s')", parse(actions.get(ActionType.POST)));
    }

    /**
     * Parse an Action into a message selectors String
     * @param action Action to parse
     * @return formatted message selectors String
     */
    private String parse(final Action action) {
        return action.getBody().values().stream()
                .map(Names::nameFrom)
                .collect(joining("','"));
    }

    /**
     * Generate the subscription name for a resource.
     * @param resourceUri the URI of the resource
     * @param clientId the previously generated client id
     * @return the subscription name
     */
    private String subscriptionNameOf(final MessagingResourceUri resourceUri, final String clientId) {
        return format("%s.%s", clientId, resourceUri.destinationName());
    }
}
