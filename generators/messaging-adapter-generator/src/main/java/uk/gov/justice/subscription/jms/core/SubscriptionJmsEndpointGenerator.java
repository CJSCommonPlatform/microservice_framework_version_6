package uk.gov.justice.subscription.jms.core;

import static java.util.stream.Collectors.toList;
import static uk.gov.justice.services.generators.commons.helper.GeneratedClassWriter.writeClass;
import static uk.gov.justice.subscription.jms.core.JmsEndPointGeneratorUtil.shouldGenerateEventFilter;

import uk.gov.justice.maven.generator.io.files.parser.core.Generator;
import uk.gov.justice.maven.generator.io.files.parser.core.GeneratorConfig;
import uk.gov.justice.services.generators.commons.config.CommonGeneratorProperties;
import uk.gov.justice.services.generators.commons.mapping.SubscriptionMediaTypeToSchemaIdGenerator;
import uk.gov.justice.subscription.domain.eventsource.EventSource;
import uk.gov.justice.subscription.domain.subscriptiondescriptor.Event;
import uk.gov.justice.subscription.domain.subscriptiondescriptor.Subscription;
import uk.gov.justice.subscription.domain.subscriptiondescriptor.SubscriptionDescriptor;
import uk.gov.justice.subscription.jms.interceptor.EventFilterInterceptorCodeGenerator;
import uk.gov.justice.subscription.jms.interceptor.EventListenerInterceptorChainProviderCodeGenerator;
import uk.gov.justice.subscription.jms.interceptor.EventValidationInterceptorCodeGenerator;
import uk.gov.justice.subscription.jms.parser.SubscriptionWrapper;

import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import com.squareup.javapoet.TypeSpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Generates JMS endpoint classes out of RAML object
 */
public class SubscriptionJmsEndpointGenerator implements Generator<SubscriptionWrapper> {

    private static final Logger LOGGER = LoggerFactory.getLogger(SubscriptionJmsEndpointGenerator.class);


    private final MessageListenerCodeGenerator messageListenerCodeGenerator;
    private final EventFilterCodeGenerator eventFilterCodeGenerator;
    private final SubscriptionMediaTypeToSchemaIdGenerator subscriptionMediaTypeToSchemaIdGenerator;
    private final EventFilterInterceptorCodeGenerator eventFilterInterceptorCodeGenerator;
    private final EventValidationInterceptorCodeGenerator eventValidationInterceptorCodeGenerator;
    private final EventListenerInterceptorChainProviderCodeGenerator eventListenerInterceptorChainProviderCodeGenerator;

    public SubscriptionJmsEndpointGenerator(
            final MessageListenerCodeGenerator messageListenerCodeGenerator,
            final EventFilterCodeGenerator eventFilterCodeGenerator,
            final SubscriptionMediaTypeToSchemaIdGenerator subscriptionMediaTypeToSchemaIdGenerator,
            final EventFilterInterceptorCodeGenerator eventFilterInterceptorCodeGenerator,
            final EventValidationInterceptorCodeGenerator eventValidationInterceptorCodeGenerator,
            final EventListenerInterceptorChainProviderCodeGenerator eventListenerInterceptorChainProviderCodeGenerator) {
        this.messageListenerCodeGenerator = messageListenerCodeGenerator;
        this.eventFilterCodeGenerator = eventFilterCodeGenerator;
        this.subscriptionMediaTypeToSchemaIdGenerator = subscriptionMediaTypeToSchemaIdGenerator;
        this.eventFilterInterceptorCodeGenerator = eventFilterInterceptorCodeGenerator;
        this.eventValidationInterceptorCodeGenerator = eventValidationInterceptorCodeGenerator;
        this.eventListenerInterceptorChainProviderCodeGenerator = eventListenerInterceptorChainProviderCodeGenerator;
    }

    /**
     * Generates JMS endpoint classes from a SubscriptionDescriptorDef document.
     *
     * @param subscriptionWrapper the subscriptionWrapper document
     * @param configuration       contains package of generated sources, as well as source and destination
     *                            folders
     */
    @Override
    public void run(final SubscriptionWrapper subscriptionWrapper, final GeneratorConfig configuration) {

        final CommonGeneratorProperties commonGeneratorProperties = (CommonGeneratorProperties) configuration.getGeneratorProperties();
        final String basePackageName = configuration.getBasePackageName();

        final SubscriptionDescriptor subscriptionDescriptor = subscriptionWrapper.getSubscriptionDescriptor();
        final List<Subscription> subscriptions = subscriptionDescriptor.getSubscriptions();
        subscriptions.stream()
                .flatMap(subscription -> generatedClassesFrom(subscriptionWrapper, subscription, commonGeneratorProperties, basePackageName))
                .forEach(generatedClass ->
                        writeClass(configuration, basePackageName, generatedClass, LOGGER)
                );

        final List<Event> allEvents = subscriptions.stream()
                .map(Subscription::getEvents)
                .flatMap(Collection::stream)
                .collect(toList());

        final String contextName = subscriptionDescriptor.getService();
        final String componentName = subscriptionDescriptor.getServiceComponent();

        subscriptionMediaTypeToSchemaIdGenerator.generateMediaTypeToSchemaIdMapper(
                contextName,
                componentName,
                allEvents,
                configuration);
    }

    private Stream<TypeSpec> generatedClassesFrom(final SubscriptionWrapper subscriptionWrapper,
                                                  final Subscription subscription,
                                                  final CommonGeneratorProperties commonGeneratorProperties,
                                                  final String basePackageName) {

        final Stream.Builder<TypeSpec> streamBuilder = Stream.builder();
        final SubscriptionDescriptor subscriptionDescriptor = subscriptionWrapper.getSubscriptionDescriptor();
        final String contextName = subscriptionDescriptor.getService();
        final String componentName = subscriptionDescriptor.getServiceComponent();


        final EventSource eventSourceDefinition = subscriptionWrapper.getEventSourceByName(subscription.getEventSourceName());

        final ClassNameFactory classNameFactory = new ClassNameFactory(
                basePackageName,
                contextName,
                componentName,
                eventSourceDefinition.getLocation().getJmsUri());

        if (shouldGenerateEventFilter(subscription.getEvents(), componentName)) {

            streamBuilder
                    .add(eventFilterCodeGenerator.generate(subscription, classNameFactory))
                    .add(eventFilterInterceptorCodeGenerator.generate(classNameFactory))
                    .add(eventValidationInterceptorCodeGenerator.generate(classNameFactory))
                    .add(eventListenerInterceptorChainProviderCodeGenerator.generate(
                            commonGeneratorProperties.getServiceComponent(),
                            classNameFactory));

        }

        streamBuilder.add(messageListenerCodeGenerator.generate(
                subscriptionWrapper,
                subscription,
                commonGeneratorProperties,
                classNameFactory));

        return streamBuilder.build();
    }
}
