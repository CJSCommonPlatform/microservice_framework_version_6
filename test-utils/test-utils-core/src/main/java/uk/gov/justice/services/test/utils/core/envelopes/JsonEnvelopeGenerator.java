package uk.gov.justice.services.test.utils.core.envelopes;

import uk.gov.justice.services.messaging.JsonEnvelope;

import java.util.UUID;

public interface JsonEnvelopeGenerator {

    JsonEnvelope generate(final UUID streamId, final Long position);
}
