package uk.gov.justice.subscription.domain.builders;

import static java.util.Optional.ofNullable;

import uk.gov.justice.subscription.domain.eventsource.Location;

public final class LocationBuilder {

    private String jmsUri;
    private String restUri;
    private String dataSource;

    private LocationBuilder() {
    }

    public static LocationBuilder location() {
        return new LocationBuilder();
    }

    public LocationBuilder withJmsUri(final String jmsUri) {
        this.jmsUri = jmsUri;
        return this;
    }

    public LocationBuilder withRestUri(final String restUri) {
        this.restUri = restUri;
        return this;
    }

    public LocationBuilder withDataSource(final String dataSource) {
        this.dataSource = dataSource;
        return this;
    }

    public Location build() {
        return new Location(jmsUri, ofNullable(restUri), ofNullable(dataSource));
    }
}
