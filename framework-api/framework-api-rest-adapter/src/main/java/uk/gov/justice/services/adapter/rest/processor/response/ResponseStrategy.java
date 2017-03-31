package uk.gov.justice.services.adapter.rest.processor.response;

import uk.gov.justice.services.messaging.JsonEnvelope;

import java.util.Optional;

import javax.ws.rs.core.Response;

@FunctionalInterface
public interface ResponseStrategy {

    Response responseFor(final String action, final Optional<JsonEnvelope> result);
}
