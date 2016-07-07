package uk.gov.justice.services.adapter.rest.mapper;

import static javax.json.Json.createObjectBuilder;
import static javax.ws.rs.core.Response.Status.FORBIDDEN;
import static javax.ws.rs.core.Response.status;
import static org.slf4j.LoggerFactory.getLogger;

import uk.gov.justice.services.core.accesscontrol.AccessControlViolationException;

import javax.json.JsonObjectBuilder;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.slf4j.Logger;

@Provider
public class ForbiddenRequestExceptionMapper implements ExceptionMapper<AccessControlViolationException> {

    private static final Logger LOGGER = getLogger(ForbiddenRequestExceptionMapper.class);

    @Override
    public Response toResponse(final AccessControlViolationException exception) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Forbidden Request", exception);
        }

        final JsonObjectBuilder builder = createObjectBuilder().add("error", exception.getMessage());

        return status(FORBIDDEN)
                .entity(builder.build().toString())
                .build();
    }

}
