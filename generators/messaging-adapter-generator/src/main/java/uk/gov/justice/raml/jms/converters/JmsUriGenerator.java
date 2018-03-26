package uk.gov.justice.raml.jms.converters;

public class JmsUriGenerator {

    public String createJmsUriFrom(final String resourceUri) {

        final String strippedUri = resourceUri.substring(1, resourceUri.length());

        return String.format("jms:topic:%s", strippedUri);
    }
}
