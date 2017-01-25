package uk.gov.justice.services.adapter.rest.processor;

import static javax.ws.rs.core.Response.Status.OK;
import static javax.ws.rs.core.Response.status;
import static org.slf4j.LoggerFactory.getLogger;

import uk.gov.justice.services.adapter.rest.envelope.RestEnvelopeBuilderFactory;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.JsonObjectEnvelopeConverter;

import javax.ws.rs.core.Response;

import org.slf4j.Logger;

public class EnvelopeResponseRestProcessor extends BaseRestProcessor {

    private static final Logger LOGGER = getLogger(EnvelopeResponseRestProcessor.class);
    private final JsonObjectEnvelopeConverter jsonObjectEnvelopeConverter;

    EnvelopeResponseRestProcessor(final RestEnvelopeBuilderFactory envelopeBuilderFactory,
                                  final JsonObjectEnvelopeConverter jsonObjectEnvelopeConverter) {
        super(envelopeBuilderFactory, LOGGER);
        this.jsonObjectEnvelopeConverter = jsonObjectEnvelopeConverter;
    }

    protected Response okResponseFrom(final JsonEnvelope envelope) {
        return status(OK)
                .entity(jsonObjectEnvelopeConverter.fromEnvelope(envelope))
                .build();
    }
}