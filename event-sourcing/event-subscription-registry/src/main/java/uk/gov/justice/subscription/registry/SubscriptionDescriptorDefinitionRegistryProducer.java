package uk.gov.justice.subscription.registry;

import static java.lang.String.format;

import uk.gov.justice.subscription.SubscriptionDescriptorsParser;
import uk.gov.justice.subscription.YamlFileFinder;
import uk.gov.justice.subscription.domain.subscriptiondescriptor.SubscriptionDescriptorDefinition;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

import javax.enterprise.inject.Produces;
import javax.faces.bean.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;

/**
 * Producer for the {@link SubscriptionDescriptorDefinitionRegistry} creates a single instance and returns the same
 * instance.
 */
@ApplicationScoped
public class SubscriptionDescriptorDefinitionRegistryProducer {

    @Inject
    Logger logger;

    @Inject
    YamlFileFinder yamlFileFinder;

    @Inject
    SubscriptionDescriptorsParser subscriptionDescriptorsParser;

    private SubscriptionDescriptorDefinitionRegistry subscriptionDescriptorRegistry;

    private Consumer<SubscriptionDescriptorDefinition> logRegisteredSubscriptionNames = subscriptionDescriptorDefinition ->
            subscriptionDescriptorDefinition.getSubscriptions()
                    .forEach(subscription ->
                            logger.info(format("Subscription name in registry : %s", subscription.getName())));

    /**
     * Either creates the single instance of the {@link SubscriptionDescriptorDefinitionRegistry} and returns it, or
     * returns the previously created instance.
     *
     * @return the instance of the {@link SubscriptionDescriptorDefinitionRegistry}
     */
    @Produces
    public SubscriptionDescriptorDefinitionRegistry subscriptionDescriptorRegistry() {
        if (null == subscriptionDescriptorRegistry) {
            try {
                final List<URL> subscriptionDescriptorPaths = yamlFileFinder.getSubscriptionDescriptorPaths();

                final Stream<SubscriptionDescriptorDefinition> subscriptionDescriptorDefinitions = subscriptionDescriptorsParser
                        .getSubscriptionDescriptorsFrom(subscriptionDescriptorPaths)
                        .peek(logRegisteredSubscriptionNames);

                subscriptionDescriptorRegistry = new SubscriptionDescriptorDefinitionRegistry(subscriptionDescriptorDefinitions);
            } catch (final IOException e) {
                throw new RegistryException("Failed to find yaml/subscription-descriptor.yaml resources on the classpath", e);
            }
        }
        return subscriptionDescriptorRegistry;
    }
}
