package uk.gov.justice.services.generators.commons.helper;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

/**
 * Unit test for the {@link MessagingClientBaseUri} class.
 */
public class MessagingClientBaseUriTest {

    @Test
    public void shouldReturnClassName() {
        assertThat(new MessagingClientBaseUri("message://event/listener/message/system").toClassName(), is("EventListenerMessageSystem"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionForInvalidUri() {
        new MessagingClientBaseUri("blah").toClassName();
    }
}
