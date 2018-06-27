package uk.gov.justice.subscription.registry;

import static java.lang.String.format;
import static java.util.stream.Collectors.toSet;

import uk.gov.justice.subscription.SubscriptionsDescriptorParser;
import uk.gov.justice.subscription.YamlFileFinder;
import uk.gov.justice.subscription.domain.subscriptiondescriptor.SubscriptionsDescriptor;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import javax.enterprise.inject.Produces;
import javax.faces.bean.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;

/**
 * Producer for the {@link SubscriptionsDescriptorsRegistry} creates a single instance and returns the same
 * instance.
 */
@ApplicationScoped
public class SubscriptionsDescriptorsRegistryProducer {

    @Inject
    Logger logger;

    @Inject
    YamlFileFinder yamlFileFinder;

    @Inject
    SubscriptionsDescriptorParser subscriptionsDescriptorParser;

    private SubscriptionsDescriptorsRegistry subscriptionsDescriptorsRegistry;

    private Consumer<SubscriptionsDescriptor> logRegisteredSubscriptionNames = subscriptionDescriptorDefinition ->
            subscriptionDescriptorDefinition.getSubscriptions()
                    .forEach(subscription ->
                            logger.info(format("Subscription name in registry : %s", subscription.getName())));

    /**
     * Either creates the single instance of the {@link SubscriptionsDescriptorsRegistry} and returns it, or
     * returns the previously created instance.
     *
     * @return the instance of the {@link SubscriptionsDescriptorsRegistry}
     */
    @Produces
    public SubscriptionsDescriptorsRegistry subscriptionDescriptorRegistry() {
        if (null == subscriptionsDescriptorsRegistry) {
            try {
                final List<URL> subscriptionsDescriptorsPaths = yamlFileFinder.getSubscriptionsDescriptorsPaths();

                if (subscriptionsDescriptorsPaths.isEmpty()){
                    throw new RegistryException("No event-sources.yaml files found!");
                }

                final Set<SubscriptionsDescriptor> subscriptionsDescriptors = subscriptionsDescriptorParser
                        .getSubscriptionDescriptorsFrom(subscriptionsDescriptorsPaths)
                        .peek(logRegisteredSubscriptionNames).collect(toSet());

                subscriptionsDescriptorsRegistry = new SubscriptionsDescriptorsRegistry(subscriptionsDescriptors);
            } catch (final IOException e) {
                throw new RegistryException("Failed to find yaml/subscriptions-descriptor.yaml resources on the classpath", e);
            }
        }
        return subscriptionsDescriptorsRegistry;
    }
}
