package uk.gov.justice.subscription;

import static java.util.Comparator.comparing;
import static java.util.Comparator.naturalOrder;

import uk.gov.justice.subscription.domain.subscriptiondescriptor.Subscription;
import uk.gov.justice.subscription.domain.subscriptiondescriptor.SubscriptionsDescriptor;

public class SubscriptionSorter {

    public void sortSubscriptionsByPrioritisation(final SubscriptionsDescriptor subscriptionsDescriptor) {
        if (!subscriptionsDescriptor.getSubscriptions().isEmpty()) {
            subscriptionsDescriptor.getSubscriptions().sort(comparing(Subscription::getPrioritisation, naturalOrder()));
        }
    }
}
