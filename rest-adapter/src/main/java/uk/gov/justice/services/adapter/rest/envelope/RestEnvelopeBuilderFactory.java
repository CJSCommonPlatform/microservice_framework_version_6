package uk.gov.justice.services.adapter.rest.envelope;

import java.util.UUID;

/**
 * Factory for generating {@link RestEnvelopeBuilder} objects.
 */
public class RestEnvelopeBuilderFactory {

    public RestEnvelopeBuilder builder() {
        return new RestEnvelopeBuilder(UUID.randomUUID());
    }
}
