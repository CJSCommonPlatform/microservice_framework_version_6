package uk.gov.justice.services.components.event.listener.interceptors.it.util.repository;

import uk.gov.justice.services.event.buffer.core.repository.subscription.SubscriptionJdbcRepository;

public class SubscriptionOpenEjbAwareJdbcRepository extends SubscriptionJdbcRepository {

    protected String jndiName() {
        return "java:openejb/Resource/viewStore";
    }
}
