package uk.gov.justice.raml.jms.converters;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Stream.of;

import org.apache.commons.lang3.StringUtils;

public class SubscriptionNamesGenerator {

    public String createContextNameFrom(final String baseUri) {
        return baseUri.substring(baseUri.lastIndexOf('/') + 1);
    }

    public String createSubscriptionNameFrom(final String resourceUri) {

        return createBaseNameFrom(resourceUri) +  "Subscription";
    }

    public String createEventSourceNameFrom(final String resourceUri) {

        return createBaseNameFrom(resourceUri) +  "Source";
    }

    private String createBaseNameFrom(final String resourceUri) {
        return of(resourceUri.split("[/.:]"))
                .map(StringUtils::capitalize)
                .collect(joining());
    }
}
