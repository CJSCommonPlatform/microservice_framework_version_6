package uk.gov.justice.services.core.dispatcher;


import static uk.gov.justice.services.messaging.DefaultJsonEnvelope.envelope;
import static uk.gov.justice.services.messaging.JsonObjectMetadata.metadataFrom;

import uk.gov.justice.services.messaging.JsonEnvelope;

import java.util.UUID;

public class DefaultRequester implements Requester {
    private final Dispatcher dispatcher;
    private final SystemUserProvider systemUserProvider;

    public DefaultRequester(final Dispatcher dispatcher, final SystemUserProvider systemUserProvider) {
        this.dispatcher = dispatcher;
        this.systemUserProvider = systemUserProvider;
    }

    @Override
    public JsonEnvelope request(final JsonEnvelope envelope) {
        return dispatcher.dispatch(envelope);
    }

    @Override
    public JsonEnvelope requestAsAdmin(final JsonEnvelope envelope) {
        final UUID sysUserId = systemUserProvider.getContextSystemUserId()
                .orElseThrow(() -> new IllegalStateException("System userId not found"));
        return dispatcher.dispatch(
                envelope()
                        .with(metadataFrom(envelope.metadata())
                                .withUserId(sysUserId.toString()))
                        .withPayloadFrom(envelope)
                        .build());

    }
}
