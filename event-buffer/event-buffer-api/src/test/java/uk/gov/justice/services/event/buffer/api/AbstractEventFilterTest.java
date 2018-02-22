package uk.gov.justice.services.event.buffer.api;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Set;

import org.junit.Test;

public class AbstractEventFilterTest {

    private AbstractEventFilter filter;

    @Test
    public void shouldAllowConfiguredEventNames() throws Exception {
        filter = new AbstractEventFilter("event1", "event2", "event3") {
            // TODO fix
            @Override
            public Set<String> getSupportedEvents() {
                return null;
            }
        };
        assertTrue(filter.accepts("event1"));
        assertTrue(filter.accepts("event2"));
        assertTrue(filter.accepts("event3"));
        assertFalse(filter.accepts("event4"));
    }
}
