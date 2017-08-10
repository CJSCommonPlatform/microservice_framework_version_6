package uk.gov.justice.services.core.dispatcher;


import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.messaging.JsonEnvelope.metadataFrom;

import uk.gov.justice.services.messaging.JsonEnvelope;

import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class SystemUserUtil {

    @Inject
    private SystemUserProvider systemUserProvider;

    /**
     * Replaces userId of the envelope with system userId
     *
     * @param envelope the JsonEnvelope with userId to be replaced
     * @return envelope with system user id
     */
    public JsonEnvelope asEnvelopeWithSystemUserId(final JsonEnvelope envelope) {
        final UUID sysUserId = systemUserProvider.getContextSystemUserId()
                .orElseThrow(() -> new IllegalStateException("System userId not found"));

        return envelopeFrom(
                metadataFrom(envelope.metadata())
                        .withUserId(sysUserId.toString()),
                envelope.payload());
    }
}