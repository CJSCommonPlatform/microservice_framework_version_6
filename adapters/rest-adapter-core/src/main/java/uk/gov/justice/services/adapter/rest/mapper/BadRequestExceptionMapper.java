package uk.gov.justice.services.adapter.rest.mapper;

import static javax.json.Json.createObjectBuilder;
import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.status;
import static org.slf4j.LoggerFactory.getLogger;

import uk.gov.justice.services.adapter.rest.exception.BadRequestException;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.slf4j.Logger;

@Provider
public class BadRequestExceptionMapper implements ExceptionMapper<BadRequestException> {

    private static final Logger LOGGER = getLogger(BadRequestExceptionMapper.class);

    @Override
    public Response toResponse(final BadRequestException exception) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Bad Request", exception);
        }

        return status(BAD_REQUEST)
                .entity(createObjectBuilder().add("error", exception.getMessage()).build().toString())
                .build();
    }

}
