package uk.gov.justice.subscription.jms.core;

import static com.squareup.javapoet.TypeSpec.classBuilder;
import static java.lang.String.format;
import static java.util.stream.Collectors.joining;
import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.PUBLIC;
import static javax.lang.model.element.Modifier.STATIC;
import static uk.gov.justice.subscription.jms.core.ClassNameFactory.EVENT_VALIDATION_INTERCEPTOR;
import static uk.gov.justice.subscription.jms.core.ClassNameFactory.JMS_LISTENER;
import static uk.gov.justice.subscription.jms.core.JmsEndPointGeneratorUtil.shouldGenerateEventFilter;
import static uk.gov.justice.subscription.jms.core.JmsEndPointGeneratorUtil.shouldListenToAllMessages;

import uk.gov.justice.services.adapter.messaging.JmsLoggerMetadataInterceptor;
import uk.gov.justice.services.adapter.messaging.JsonSchemaValidationInterceptor;
import uk.gov.justice.services.adapter.messaging.SubscriptionJmsProcessor;
import uk.gov.justice.services.core.annotation.Adapter;
import uk.gov.justice.services.generators.commons.config.CommonGeneratorProperties;
import uk.gov.justice.services.messaging.logging.LoggerUtils;
import uk.gov.justice.services.subscription.SubscriptionManager;
import uk.gov.justice.services.subscription.annotation.SubscriptionName;
import uk.gov.justice.subscription.domain.eventsource.EventSourceDefinition;
import uk.gov.justice.subscription.domain.subscriptiondescriptor.Event;
import uk.gov.justice.subscription.domain.subscriptiondescriptor.Subscription;
import uk.gov.justice.subscription.domain.subscriptiondescriptor.SubscriptionDescriptor;
import uk.gov.justice.subscription.jms.parser.SubscriptionWrapper;

import java.util.List;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Internal code generation class for generating the JMS {@link MessageListener} that listens to the
 * message types described in the RAML.
 */
public class MessageListenerCodeGenerator {

    private static final String CLASS_NAME = "$T.class";
    private static final String DEFAULT_ANNOTATION_PARAMETER = "value";
    private static final String ACTIVATION_CONFIG_PARAMETER = "activationConfig";
    private static final String SUBSCRIPTION_MANAGER = "subscriptionManager";
    private static final String JMS_PROCESSOR_FIELD = "subscriptionJmsProcessor";
    private static final String LOGGER_FIELD = "LOGGER";

    private static final String DESTINATION_TYPE = "destinationType";
    private static final String DESTINATION_LOOKUP = "destinationLookup";
    private static final String MESSAGE_SELECTOR = "messageSelector";
    private static final String SUBSCRIPTION_DURABILITY = "subscriptionDurability";
    private static final String DURABLE = "Durable";
    private static final String CLIENT_ID = "clientId";
    private static final String SUBSCRIPTION_NAME = "subscriptionName";
    private static final String SHARE_SUBSCRIPTIONS = "shareSubscriptions";

    private final ComponentDestinationType componentDestinationType = new ComponentDestinationType();

    /**
     * Create an implementation of the {@link MessageListener}.
     *
     * @param subscriptionWrapper       the subscription descriptor Wrapper
     * @param subscription              the subscription
     * @param commonGeneratorProperties used to query the generator properties
     * @param classNameFactory          creates the class name for this generated class
     * @return the message listener class specification
     */
    TypeSpec generate(final SubscriptionWrapper subscriptionWrapper,
                      final Subscription subscription,
                      final CommonGeneratorProperties commonGeneratorProperties,
                      final ClassNameFactory classNameFactory) {
        return classSpecFrom(subscriptionWrapper, subscription, commonGeneratorProperties, classNameFactory)
                .addMethod(generateOnMessageMethod())
                .build();
    }

    /**
     * Generate the @link MessageListener} class implementation.
     *
     * @param subscriptionWrapper       the subscription descriptor Wrapper
     * @param subscription              the subscription
     * @param commonGeneratorProperties used to query the generator properties
     * @param classNameFactory          creates the class name for this generated class
     * @return the {@link TypeSpec.Builder} that defines the class
     */
    private TypeSpec.Builder classSpecFrom(final SubscriptionWrapper subscriptionWrapper,
                                           final Subscription subscription,
                                           final CommonGeneratorProperties commonGeneratorProperties,
                                           final ClassNameFactory classNameFactory) {
        final SubscriptionDescriptor subscriptionDescriptor = subscriptionWrapper.getSubscriptionDescriptor();
        final String serviceComponent = subscriptionDescriptor.getServiceComponent().toUpperCase();

        if (componentDestinationType.isSupported(serviceComponent)) {

            final ClassName className = classNameFactory.classNameFor(JMS_LISTENER);
            final EventSourceDefinition eventSourceDefinition = subscriptionWrapper.getEventSourceByName(subscription.getEventSourceName());
            final String destination = destinationFromJmsUri(eventSourceDefinition.getLocation().getJmsUri());

            final TypeSpec.Builder typeSpecBuilder = classBuilder(className)
                    .addModifiers(PUBLIC)
                    .addSuperinterface(MessageListener.class)
                    .addField(FieldSpec.builder(ClassName.get(Logger.class), LOGGER_FIELD)
                            .addModifiers(PRIVATE, STATIC, FINAL)
                            .initializer("$T.getLogger($L.class)", LoggerFactory.class, className)
                            .build())
                    .addField(FieldSpec.builder(ClassName.get(SubscriptionManager.class), SUBSCRIPTION_MANAGER)
                            .addAnnotation(Inject.class)
                            .addAnnotation(AnnotationSpec.builder(SubscriptionName.class)
                                    .addMember(DEFAULT_ANNOTATION_PARAMETER, "$S", subscription.getName())
                                    .build())
                            .build())
                    .addField(FieldSpec.builder(ClassName.get(SubscriptionJmsProcessor.class), JMS_PROCESSOR_FIELD)
                            .addAnnotation(Inject.class)
                            .build())
                    .addAnnotation(AnnotationSpec.builder(Adapter.class)
                            .addMember(DEFAULT_ANNOTATION_PARAMETER, "$S", serviceComponent)
                            .build())
                    .addAnnotation(messageDrivenAnnotation(serviceComponent, subscriptionDescriptor.getService(), subscription, destination));

            if (!subscription.getEvents().isEmpty()) {
                AnnotationSpec.Builder builder = AnnotationSpec.builder(Interceptors.class)
                        .addMember(DEFAULT_ANNOTATION_PARAMETER, CLASS_NAME, JmsLoggerMetadataInterceptor.class)
                        .addMember(DEFAULT_ANNOTATION_PARAMETER, CLASS_NAME, getValidationInterceptorClassName(classNameFactory, serviceComponent, subscription.getEvents()));

                typeSpecBuilder.addAnnotation(builder.build());
            }

            if (shouldAddCustomPoolConfiguration(commonGeneratorProperties)) {
                typeSpecBuilder.addAnnotation(AnnotationSpec.builder(Pool.class)
                        .addMember(DEFAULT_ANNOTATION_PARAMETER, "$S", poolNameFrom(destination, serviceComponent))
                        .build());
            }

            return typeSpecBuilder;
        }

        throw new IllegalStateException(format("JMS Endpoint generation is unsupported for framework component type %s", serviceComponent));
    }

    private boolean shouldAddCustomPoolConfiguration(final CommonGeneratorProperties commonGeneratorProperties) {
        return Boolean.valueOf(commonGeneratorProperties.getCustomMDBPool());
    }

    private ClassName getValidationInterceptorClassName(final ClassNameFactory classNameFactory,
                                                        final String component,
                                                        final List<Event> events) {

        if (shouldGenerateEventFilter(events, component)) {
            return classNameFactory.classNameFor(EVENT_VALIDATION_INTERCEPTOR);
        }

        return ClassName.get(JsonSchemaValidationInterceptor.class);
    }

    private String poolNameFrom(final String destination, final String serviceComponent) {
        return format("%s-%s-pool", hypotinize(destination), hypotinize(serviceComponent));
    }

    private String hypotinize(String source) {
        return source.toLowerCase()
                .replace("_", "-")
                .replace(".", "-");
    }

    /**
     * Generate the onMessage method that processes a JMS message.
     *
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
                        .addStatement("$T.trace(LOGGER, () -> \"Received JMS message\")", LoggerUtils.class)
                        .addStatement("$L.process($L, $L)",
                                JMS_PROCESSOR_FIELD,
                                SUBSCRIPTION_MANAGER,
                                messageFieldName)
                        .build())
                .build();
    }

    /**
     * Generate the @MessageDriven annotation
     *
     * @param component    the service component
     * @param service      the service
     * @param subscription the subscription
     */
    private AnnotationSpec messageDrivenAnnotation(final String component,
                                                   final String service,
                                                   final Subscription subscription,
                                                   final String destination) {

        final Class<? extends Destination> inputType = componentDestinationType.inputTypeFor(component);

        AnnotationSpec.Builder builder = AnnotationSpec.builder(MessageDriven.class)
                .addMember(ACTIVATION_CONFIG_PARAMETER, "$L",
                        generateActivationConfigPropertyAnnotation(DESTINATION_TYPE, inputType.getName()))
                .addMember(ACTIVATION_CONFIG_PARAMETER, "$L",
                        generateActivationConfigPropertyAnnotation(DESTINATION_LOOKUP, destination))
                .addMember(ACTIVATION_CONFIG_PARAMETER, "$L",
                        generateActivationConfigPropertyAnnotation(SHARE_SUBSCRIPTIONS, "true"));

        if (!shouldListenToAllMessages(subscription.getEvents(), component)) {
            builder.addMember(ACTIVATION_CONFIG_PARAMETER, "$L",
                    generateActivationConfigPropertyAnnotation(MESSAGE_SELECTOR, messageSelectorsFrom(subscription.getEvents())));
        }

        if (Topic.class.equals(inputType)) {
            final String clientId = adapterClientId(service, component);
            builder
                    .addMember(ACTIVATION_CONFIG_PARAMETER, "$L",
                            generateActivationConfigPropertyAnnotation(SUBSCRIPTION_DURABILITY, DURABLE))
                    .addMember(ACTIVATION_CONFIG_PARAMETER, "$L",
                            generateActivationConfigPropertyAnnotation(CLIENT_ID, clientId))
                    .addMember(ACTIVATION_CONFIG_PARAMETER, "$L",
                            generateActivationConfigPropertyAnnotation(SUBSCRIPTION_NAME, subscriptionNameOf(destination, clientId)));
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
     * Parse and format all the message selectors from the Post Action
     *
     * @param events the list of events
     * @return formatted message selector String
     */
    private String messageSelectorsFrom(final List<Event> events) {
        return format("CPPNAME in('%s')", events.stream().map(Event::getName)
                .collect(joining("','")));
    }

    /**
     * Constructs clientId used in durable JMS subscribers
     *
     * @return messaging adapter clientId
     */
    private String adapterClientId(final String service, final String component) {
        return format("%s.%s", service, component.replaceAll("_", "\\.").toLowerCase());
    }

    /**
     * Retrieves the destination from jmsUri
     *
     * @return messaging adapter clientId
     */
    private String destinationFromJmsUri(final String jmsUri) {
        return jmsUri.split(":")[2];
    }

    /**
     * Generate the subscription name for a resource.
     *
     * @param destination the jms destination
     * @param clientId    the previously generated client id
     * @return the subscription name
     */
    private String subscriptionNameOf(final String destination, final String clientId) {
        return format("%s.%s", clientId, destination);
    }
}
