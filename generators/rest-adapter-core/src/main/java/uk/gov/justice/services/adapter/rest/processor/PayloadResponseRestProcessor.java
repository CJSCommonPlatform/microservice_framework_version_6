package uk.gov.justice.services.adapter.rest.processor;

import static javax.ws.rs.core.Response.Status.OK;
import static javax.ws.rs.core.Response.status;
import static org.slf4j.LoggerFactory.getLogger;
import static uk.gov.justice.services.common.http.HeaderConstants.ID;

import uk.gov.justice.services.adapter.rest.envelope.RestEnvelopeBuilderFactory;
import uk.gov.justice.services.messaging.JsonEnvelope;

import javax.ws.rs.core.Response;

import org.slf4j.Logger;

public class PayloadResponseRestProcessor extends BaseRestProcessor {

    private static final Logger LOGGER = getLogger(EnvelopeResponseRestProcessor.class);

    PayloadResponseRestProcessor(final RestEnvelopeBuilderFactory envelopeBuilderFactory) {
        super(envelopeBuilderFactory, LOGGER);
    }

    protected Response okResponseFrom(final JsonEnvelope envelope) {
        return status(OK)
                .header(ID, envelope.metadata().id())
                .entity(envelope.payloadAsJsonObject())
                .build();
    }
}