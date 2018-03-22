package uk.gov.justice.raml.jms.converters;

import static org.raml.model.ActionType.POST;

import uk.gov.justice.domain.subscriptiondescriptor.Event;
import uk.gov.justice.domain.subscriptiondescriptor.Eventsource;
import uk.gov.justice.domain.subscriptiondescriptor.Location;
import uk.gov.justice.domain.subscriptiondescriptor.Subscription;

import java.util.Collection;
import java.util.List;

import org.raml.model.MimeType;
import org.raml.model.Resource;

public class RamlResourceToSubscriptionConverter {

    private final SubscriptionNamesGenerator subscriptionNamesGenerator;
    private final RamlMimeTypeListToEventListConverter ramlMimeTypeListToEventListConverter;
    private final JmsUriGenerator jmsUriGenerator;

    public RamlResourceToSubscriptionConverter(
            final SubscriptionNamesGenerator subscriptionNamesGenerator,
            final RamlMimeTypeListToEventListConverter ramlMimeTypeListToEventListConverter,
            final JmsUriGenerator jmsUriGenerator) {
        this.subscriptionNamesGenerator = subscriptionNamesGenerator;
        this.ramlMimeTypeListToEventListConverter = ramlMimeTypeListToEventListConverter;
        this.jmsUriGenerator = jmsUriGenerator;
    }

    public Subscription asSubscription(final Resource resource) {

        final String resourceUri = resource.getUri();
        final String subscriptionName = subscriptionNamesGenerator.createSubscriptionNameFrom(
                resourceUri);

        final Collection<MimeType> mimeTypes = resource.getAction(POST).getBody().values();
        final List<Event> events = ramlMimeTypeListToEventListConverter.toEvents(mimeTypes);

        final String eventSourceName = subscriptionNamesGenerator.createEventSourceNameFrom(resourceUri);
        final String jmsUri = jmsUriGenerator.createJmsUriFrom(resourceUri);
        final String restUri = null;

        final Eventsource eventsource = new Eventsource(
                eventSourceName,
                new Location(jmsUri, restUri));

        return new Subscription(subscriptionName, events, eventsource);
    }


}
