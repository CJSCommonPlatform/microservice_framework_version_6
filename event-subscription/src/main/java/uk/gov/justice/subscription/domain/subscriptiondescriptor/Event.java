package uk.gov.justice.subscription.domain.subscriptiondescriptor;

public class Event {

    private final String name;
    private final String schemaUri;

    public Event(final String name, final String schemaUri) {
        this.name = name;
        this.schemaUri = schemaUri;
    }

    public String getName() {
        return name;
    }

    public String getSchemaUri() {
        return schemaUri;
    }
}
