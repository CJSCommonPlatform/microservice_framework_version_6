package uk.gov.justice.services.eventsourcing.source.api.util;

import uk.gov.justice.services.eventsourcing.repository.jdbc.eventstream.EventStreamJdbcRepository;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class OpenEjbAwareEventStreamRepository extends EventStreamJdbcRepository {

    protected String jndiName() {
        return "java:openejb/Resource/eventStore";
    }
}
