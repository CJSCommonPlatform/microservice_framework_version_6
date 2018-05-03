package uk.gov.justice.services.core.enveloper.spi;

import static javax.enterprise.inject.spi.CDI.current;

import uk.gov.justice.services.core.enveloper.DefaultEnveloper;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.messaging.Envelope;
import uk.gov.justice.services.messaging.JsonEnvelope;

import java.util.function.Function;

/**
 * The Default Enveloper Provider looks up the {@link DefaultEnveloper} using CDI if no CDI provider is
 * available then a new DefaultEnveloper is created.
 */
public class DefaultEnveloperProvider implements EnveloperProvider {

    @Override
    public <T> Enveloper.EnveloperBuilder<T> envelop(final T payload) {
        return getDefaultEnveloper().envelop(payload);
    }

    @Override
    public Function<Object, JsonEnvelope> toEnvelopeWithMetadataFrom(final Envelope<?> envelope) {
        return getDefaultEnveloper().toEnvelopeWithMetadataFrom(envelope);
    }

    private DefaultEnveloper getDefaultEnveloper() {

        if (isCdiAvailable()) {
            return current().select(DefaultEnveloper.class).get();
        }

        throw new IllegalStateException("No CDI container detected, DefaultEnvelopeProvider only works within a CDI container.");
    }

    private boolean isCdiAvailable() {

        try {
            current();
        } catch (IllegalStateException e) {
            return false;
        }

        return true;
    }
}
