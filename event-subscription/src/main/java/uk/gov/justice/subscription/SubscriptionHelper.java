package uk.gov.justice.subscription;

import static java.lang.Integer.parseInt;
import static java.lang.String.format;
import static java.util.Comparator.comparing;
import static java.util.Comparator.naturalOrder;

import uk.gov.justice.services.yaml.YamlParserException;
import uk.gov.justice.subscription.domain.subscriptiondescriptor.SubscriptionsDescriptor;

public class SubscriptionHelper {

    private final static int HIGHEST_PRIORITY = 0;

    public void sortSubscriptionsByPrioritisation(final SubscriptionsDescriptor subscriptionsDescriptor) {
        if (!subscriptionsDescriptor.getSubscriptions().isEmpty()) {
            subscriptionsDescriptor.getSubscriptions().sort(comparing(subscription -> getPrioritisationNumber(subscription.getPrioritisation()), naturalOrder()));
        }
    }

    private Integer getPrioritisationNumber(final String prioritisation) {
        try {
            if (null != prioritisation) {
                return parseInt(prioritisation);
            }
            return HIGHEST_PRIORITY;
        } catch (final NumberFormatException nfe) {
            throw new YamlParserException(format("Incorrect prioritisation number: %s in subscription-descriptor.yaml", prioritisation), nfe);
        }
    }
}
