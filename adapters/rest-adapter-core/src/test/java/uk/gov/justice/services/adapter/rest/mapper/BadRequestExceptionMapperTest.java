package uk.gov.justice.services.adapter.rest.mapper;

import static com.jayway.jsonassert.JsonAssert.with;
import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import uk.gov.justice.services.adapter.rest.exception.BadRequestException;

import javax.ws.rs.core.Response;

import org.junit.Test;

public class BadRequestExceptionMapperTest {

    private static final String TEST_ERROR_MESSAGE = "Test Error Message.";

    @Test
    public void shouldReturn400ResponseForBadRequestException() throws Exception {
        BadRequestExceptionMapper exceptionMapper = new BadRequestExceptionMapper();

        Response response = exceptionMapper.toResponse(new BadRequestException(TEST_ERROR_MESSAGE));

        assertThat(response.getStatus(), is(BAD_REQUEST.getStatusCode()));
        assertThat(response.getEntity(), notNullValue());
        with(response.getEntity().toString())
                .assertThat("error", equalTo(TEST_ERROR_MESSAGE));
    }

}