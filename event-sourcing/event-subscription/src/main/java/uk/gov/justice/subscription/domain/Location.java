package uk.gov.justice.subscription.domain;

public class Location {
    private final String jmsUri;
    private final String restUri;

    public Location(final String jmsUri, final String restUri) {
        this.jmsUri = jmsUri;
        this.restUri = restUri;
    }

    public String getJmsUri() {
        return jmsUri;
    }

    public String getRestUri() {
        return restUri;
    }
}
