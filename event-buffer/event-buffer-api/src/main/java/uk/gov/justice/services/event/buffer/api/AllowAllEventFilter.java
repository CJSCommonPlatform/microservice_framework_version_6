package uk.gov.justice.services.event.buffer.api;

import static java.util.Collections.emptySet;

import java.util.Collections;
import java.util.Set;

import javax.annotation.Priority;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Alternative;

@ApplicationScoped
//@Alternative
//@Priority(1)
public class AllowAllEventFilter implements EventFilter {
    @Override
    public boolean accepts(final String eventName) {
        return true;
    }

    // TODO check if we need to send an * or something and then use this as a special char to set a flag in the registry
    @Override
    public Set<String> getSupportedEvents() {
        return emptySet();
    }
}
