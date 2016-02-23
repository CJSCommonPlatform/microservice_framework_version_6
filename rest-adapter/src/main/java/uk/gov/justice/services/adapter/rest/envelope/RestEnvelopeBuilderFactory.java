package uk.gov.justice.services.adapter.rest.envelope;

import javax.inject.Inject;

/**
 * Factory for generating {@link RestEnvelopeBuilder} objects.
 */
public class RestEnvelopeBuilderFactory {

    @Inject
    RandomUUIDGenerator uuidGenerator;

    public RestEnvelopeBuilder builder() {
        return new RestEnvelopeBuilder(uuidGenerator);
    }
}
