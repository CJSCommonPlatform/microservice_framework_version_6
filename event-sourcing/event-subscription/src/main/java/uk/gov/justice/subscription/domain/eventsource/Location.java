package uk.gov.justice.subscription.domain.eventsource;

import java.util.Optional;

public class Location {
    private final String jmsUri;
    private final String restUri;
    private final Optional<String> dataSource;

    public Location(final String jmsUri,
                    final String restUri,
                    final Optional<String> dataSource) {
        this.jmsUri = jmsUri;
        this.restUri = restUri;
        this.dataSource = dataSource;
    }

    public String getJmsUri() {
        return jmsUri;
    }

    public String getRestUri() {
        return restUri;
    }

    public Optional<String> getDataSource() {
        return dataSource;
    }
}
