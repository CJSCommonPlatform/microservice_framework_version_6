package uk.gov.justice.raml.jms.converters;

import static java.util.stream.Collectors.toList;
import static org.raml.model.ActionType.POST;

import uk.gov.justice.subscription.domain.Subscription;

import java.util.Collection;
import java.util.List;

import org.raml.model.Resource;

public class ResourcesListToSubscriptionListConverter {

    private final RamlResourceToSubscriptionConverter ramlResourceToSubscriptionConverter;

    public ResourcesListToSubscriptionListConverter(final RamlResourceToSubscriptionConverter ramlResourceToSubscriptionConverter) {
        this.ramlResourceToSubscriptionConverter = ramlResourceToSubscriptionConverter;
    }

    public List<Subscription> getSubscriptions(final Collection<Resource> resources) {

        return resources.stream()
                .filter(resource -> resource.getAction(POST) != null)
                .map(ramlResourceToSubscriptionConverter::asSubscription)
                .collect(toList());
    }
}
