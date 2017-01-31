package uk.gov.justice.services.adapter.rest.processor;

import uk.gov.justice.services.messaging.JsonEnvelope;

import java.util.Optional;

import javax.ws.rs.core.Response;

public interface ResponseFactory {

    Response responseFor(final String action, final Optional<JsonEnvelope> result);
}
