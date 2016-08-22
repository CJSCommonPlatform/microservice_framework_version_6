package uk.gov.justice.services.core.eventfilter;

import javax.annotation.Priority;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Alternative;

@ApplicationScoped
@Alternative
@Priority(1)
public class AllowAllEventFilter implements EventFilter {
    @Override
    public boolean accepts(final String eventName) {
        return true;
    }
}