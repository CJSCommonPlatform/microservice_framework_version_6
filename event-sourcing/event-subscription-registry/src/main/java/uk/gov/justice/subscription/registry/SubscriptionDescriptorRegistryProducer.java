package uk.gov.justice.subscription.registry;

import static java.lang.String.format;

import uk.gov.justice.subscription.SubscriptionDescriptorsParser;
import uk.gov.justice.subscription.YamlFileFinder;
import uk.gov.justice.subscription.domain.subscriptiondescriptor.SubscriptionDescriptor;

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
 * Producer for the {@link SubscriptionDescriptorRegistry} creates a single instance and returns the same
 * instance.
 */
@ApplicationScoped
public class SubscriptionDescriptorRegistryProducer {

    @Inject
    Logger logger;

    @Inject
    YamlFileFinder yamlFileFinder;

    @Inject
    SubscriptionDescriptorsParser subscriptionDescriptorsParser;

    private SubscriptionDescriptorRegistry subscriptionDescriptorRegistry;

    private Consumer<SubscriptionDescriptor> logRegisteredSubscriptionNames = subscriptionDescriptor ->
            subscriptionDescriptor.getSubscriptions()
                    .forEach(subscription ->
                            logger.info(format("Subscription name in registry : %s", subscription.getName())));

    /**
     * Either creates the single instance of the {@link SubscriptionDescriptorRegistry} and returns it, or
     * returns the previously created instance.
     *
     * @return the instance of the {@link SubscriptionDescriptorRegistry}
     */
    @Produces
    public SubscriptionDescriptorRegistry subscriptionDescriptorRegistry() {
        if (null == subscriptionDescriptorRegistry) {
            try {
                final List<URL> subscriptionDescriptorPaths = yamlFileFinder.getSubscriptionDescriptorPaths();

                final Stream<SubscriptionDescriptor> subscriptionDescriptors = subscriptionDescriptorsParser
                        .getSubscriptionDescriptorsFrom(subscriptionDescriptorPaths)
                        .peek(logRegisteredSubscriptionNames);

                subscriptionDescriptorRegistry = new SubscriptionDescriptorRegistry(subscriptionDescriptors);
            } catch (final IOException e) {
                throw new RegistryException("Failed to find yaml/subscription-descriptor.yaml resources on the classpath", e);
            }
        }

        return subscriptionDescriptorRegistry;
    }
}
