package uk.gov.justice.services.adapter.rest.mapper;

import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import uk.gov.justice.services.adapter.rest.exception.BadRequestException;

import javax.ws.rs.core.Response;

import org.junit.Test;

public class BadRequestExceptionMapperTest {

    @Test
    public void shouldReturn400ResponseForBadRequestException() throws Exception {
        BadRequestExceptionMapper exceptionMapper = new BadRequestExceptionMapper();
        Response response = exceptionMapper.toResponse(new BadRequestException(""));
        assertThat(response.getStatus(), is(BAD_REQUEST.getStatusCode()));
    }

}