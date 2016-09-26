package uk.gov.justice.services.core.aggregate.util;

import uk.gov.justice.services.eventsourcing.repository.jdbc.eventlog.EventLogJdbcRepository;
import uk.gov.justice.services.eventsourcing.repository.jdbc.snapshot.SnapshotJdbcRepository;

public class EventLogOpenEjbAwareJdbcRepository extends EventLogJdbcRepository {

    @Override
    protected String jndiName() {
        return "java:openejb/Resource/eventStore";
    }

}
