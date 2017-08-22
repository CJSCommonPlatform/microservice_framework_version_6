package uk.gov.justice.services.eventsourcing.source.api.util;

import uk.gov.justice.services.eventsourcing.repository.jdbc.event.EventJdbcRepository;

import org.slf4j.LoggerFactory;

public class OpenEjbAwareEventRepository extends EventJdbcRepository {

    @Override
    protected String jndiName() {
        return "java:openejb/Resource/eventStore";
    }
}
