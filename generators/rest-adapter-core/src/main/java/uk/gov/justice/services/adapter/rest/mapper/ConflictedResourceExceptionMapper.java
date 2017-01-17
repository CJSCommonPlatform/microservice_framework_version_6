package uk.gov.justice.services.adapter.rest.mapper;

import static javax.json.Json.createObjectBuilder;
import static javax.ws.rs.core.Response.Status.CONFLICT;
import static javax.ws.rs.core.Response.status;

import uk.gov.justice.services.adapter.rest.exception.ConflictedResourceException;

import javax.inject.Inject;
import javax.json.JsonObjectBuilder;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.slf4j.Logger;

@Provider
public class ConflictedResourceExceptionMapper implements ExceptionMapper<ConflictedResourceException> {

    @Inject
    Logger logger;

    @Override
    public Response toResponse(final ConflictedResourceException exception) {
        logger.debug("Conflict", exception);

        final JsonObjectBuilder builder = createObjectBuilder()
                .add("error", exception.getMessage())
                .add("id", exception.getConflictingId().toString());

        return status(CONFLICT)
                .entity(builder.build().toString())
                .build();
    }

}
