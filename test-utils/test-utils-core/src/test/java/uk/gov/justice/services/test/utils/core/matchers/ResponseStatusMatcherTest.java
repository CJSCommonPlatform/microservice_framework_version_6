package uk.gov.justice.services.test.utils.core.matchers;

import static javax.ws.rs.core.Response.Status.ACCEPTED;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.rules.ExpectedException.none;
import static uk.gov.justice.services.test.utils.core.matchers.ResponseStatusMatcher.status;

import uk.gov.justice.services.test.utils.core.http.ResponseData;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class ResponseStatusMatcherTest {

    @Rule
    public ExpectedException expectedException = none();

    @Test
    public void shouldMatchForStatusFromResponse() throws Exception {
        assertThat(new ResponseData(ACCEPTED, null), status().is(ACCEPTED));
    }

    @Test
    public void shouldFailWhenResponseStatusDoesNotMatch() {
        expectedException.expect(AssertionError.class);

        assertThat(new ResponseData(NOT_FOUND, null), status().is(ACCEPTED));
    }
}