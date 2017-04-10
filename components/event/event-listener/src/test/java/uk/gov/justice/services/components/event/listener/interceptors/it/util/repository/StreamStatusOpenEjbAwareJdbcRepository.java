package uk.gov.justice.services.components.event.listener.interceptors.it.util.repository;

import uk.gov.justice.services.event.buffer.core.repository.streamstatus.StreamStatusJdbcRepository;

public class StreamStatusOpenEjbAwareJdbcRepository extends StreamStatusJdbcRepository {
    @Override
    protected String jndiName() {
        return "java:openejb/Resource/viewStore";
    }
}
