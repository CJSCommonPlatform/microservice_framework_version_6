package uk.gov.justice.services.core.eventfilter;



import static org.junit.Assert.assertTrue;

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
