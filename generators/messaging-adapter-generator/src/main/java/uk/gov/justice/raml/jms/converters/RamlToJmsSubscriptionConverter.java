package uk.gov.justice.raml.jms.converters;

import uk.gov.justice.subscription.domain.Subscription;
import uk.gov.justice.subscription.domain.SubscriptionDescriptor;
import uk.gov.justice.subscription.domain.SubscriptionDescriptorDef;

import java.util.List;

import org.raml.model.Raml;

public class RamlToJmsSubscriptionConverter {

    private static final String SUBSCRIPTION_SPEC_VERSION = "1.0.0";

    private final SubscriptionNamesGenerator subscriptionNamesGenerator;
    private final ResourcesListToSubscriptionListConverter resourcesListToSubscriptionListConverter;

    public RamlToJmsSubscriptionConverter(
            final SubscriptionNamesGenerator subscriptionNamesGenerator,
            final ResourcesListToSubscriptionListConverter resourcesListToSubscriptionListConverter) {
        this.subscriptionNamesGenerator = subscriptionNamesGenerator;
        this.resourcesListToSubscriptionListConverter = resourcesListToSubscriptionListConverter;
    }

    public SubscriptionDescriptorDef convert(final Raml raml, final String componentName) {

        final String baseUri = raml.getBaseUri();
        final String service = subscriptionNamesGenerator.createContextNameFrom(baseUri);

        final List<Subscription> subscriptions = resourcesListToSubscriptionListConverter.getSubscriptions(raml.getResources().values());
        final SubscriptionDescriptor subscriptionDescriptor = new SubscriptionDescriptor(
                SUBSCRIPTION_SPEC_VERSION,
                service,
                componentName,
                subscriptions
        );

        return new SubscriptionDescriptorDef(subscriptionDescriptor);
    }
}
