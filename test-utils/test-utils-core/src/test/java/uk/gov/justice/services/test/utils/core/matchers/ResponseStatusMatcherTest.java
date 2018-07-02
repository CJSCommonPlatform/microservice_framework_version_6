package uk.gov.justice.services.test.utils.core.matchers;

import static javax.ws.rs.core.Response.Status.OK;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static uk.gov.justice.services.test.utils.core.matchers.ResponseStatusMatcher.isStatus;

import org.hamcrest.Description;
import org.junit.Test;

public class ResponseStatusMatcherTest {

    @Test
    public void shouldMatchIfTheSuppliedResponseStatusMatchesTheExpectedStatusCode() throws Exception {

        final int actualStatusCode = 200;

        assertThat(actualStatusCode, isStatus(OK));
    }

    @Test
    public void shouldNotMatchIfTheSuppliedResponseStatusDoesNotMatchTheExpectedStatusCode() throws Exception {

        final int actualStatusCode = 500;

        assertThat(actualStatusCode, not(isStatus(OK)));
    }

    @Test
    public void shouldAppendTheCorrectFailureDescription() throws Exception {

        final ResponseStatusMatcher responseStatusMatcher = new ResponseStatusMatcher(OK);

        final Description description = mock(Description.class);

        responseStatusMatcher.describeTo(description);

        verify(description).appendText("http status of 200");
    }
}
