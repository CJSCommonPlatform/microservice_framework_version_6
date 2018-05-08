package uk.gov.justice.services.eventsourcing.source.api.util;

import uk.gov.justice.services.eventsourcing.repository.jdbc.event.EventJdbcRepository;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class OpenEjbAwareEventRepository extends EventJdbcRepository {

    protected String jndiName() {
        return "java:openejb/Resource/eventStore";
    }
}
