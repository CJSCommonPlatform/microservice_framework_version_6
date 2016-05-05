package uk.gov.justice.services.adapter.rest.mapper;

import static javax.ws.rs.core.Response.Status.BAD_REQUEST;

import uk.gov.justice.services.adapter.rest.exception.BadRequestException;

import javax.inject.Singleton;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Singleton
@Provider
public class BadRequestExceptionMapper implements ExceptionMapper<BadRequestException> {

    @Override
    public Response toResponse(final BadRequestException exception) {
        return Response.status(BAD_REQUEST).build();
    }

}
