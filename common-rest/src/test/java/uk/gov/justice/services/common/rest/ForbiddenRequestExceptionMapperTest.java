package uk.gov.justice.services.common.rest;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath;
import static javax.ws.rs.core.Response.Status.FORBIDDEN;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import uk.gov.justice.services.common.exception.ForbiddenRequestException;

import javax.ws.rs.core.Response;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ForbiddenRequestExceptionMapperTest {

    private static final String TEST_ERROR_MESSAGE = "Test Error Message.";

    @InjectMocks
    private ForbiddenRequestExceptionMapper exceptionMapper;

    @Test
    public void shouldReturn403ResponseForForbiddenRequestException() throws Exception {

        final Response response = exceptionMapper.toResponse(new ForbiddenRequestException(TEST_ERROR_MESSAGE));

        assertThat(response.getStatus(), is(FORBIDDEN.getStatusCode()));
        assertThat(response.getEntity(), notNullValue());
        assertThat(response.getEntity().toString(),
                hasJsonPath("$.error", equalTo(TEST_ERROR_MESSAGE)));
    }

}