package uk.gov.justice.services.event.buffer.api;



import static org.junit.Assert.assertTrue;

import uk.gov.justice.services.event.buffer.api.AllowAllEventFilter;
import uk.gov.justice.services.event.buffer.api.EventFilter;

import org.junit.Test;

public class AllowAllEventFilterTest {

    @Test
    public void shouldAllowAllEventNames() throws Exception {
        EventFilter filter = new AllowAllEventFilter();

        assertTrue(filter.accepts("event-nameA"));
        assertTrue(filter.accepts("aaa"));
        assertTrue(filter.accepts("bbb"));
    }
}
