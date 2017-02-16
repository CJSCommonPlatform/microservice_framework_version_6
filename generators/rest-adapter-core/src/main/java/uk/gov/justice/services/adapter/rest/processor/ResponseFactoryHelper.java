package uk.gov.justice.services.adapter.rest.processor;

import static java.lang.String.format;
import static javax.json.JsonValue.NULL;
import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static javax.ws.rs.core.Response.status;

import uk.gov.justice.services.messaging.JsonEnvelope;

import java.util.Optional;
import java.util.function.Function;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;

/**
 * Create {@link Response} for resulting JsonEnvelope.
 */
@ApplicationScoped
public class ResponseFactoryHelper {

    @Inject
    Logger logger;

    /**
     * Create {@link Response} for given action and resulting JsonEnvelope.
     * If result is not present return INTERNAL SERVER ERROR status.
     * If result payload is a NULL JsonValue return NOT FOUND status.
     * Otherwise create response from given okResponseCreator function.
     *
     * @param action            the action being processed
     * @param result            the resulting JsonEnvelope
     * @param okResponseCreator the okResponseCreator function
     * @return the {@link Response} to return from the REST call
     */
    Response responseFor(final String action, final Optional<JsonEnvelope> result, final Function<JsonEnvelope, Response> okResponseCreator) {
        if (result.isPresent()) {
            final JsonEnvelope outputEnvelope = result.get();

            if (outputEnvelope.payload() == NULL) {
                return status(NOT_FOUND).build();
            } else {
                return okResponseCreator.apply(outputEnvelope);
            }
        } else {
            logger.error(format("Dispatcher returned a null envelope for %s", action));
            return status(INTERNAL_SERVER_ERROR).build();
        }
    }
}