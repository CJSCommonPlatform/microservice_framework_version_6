package uk.gov.justice.services.test.utils.enveloper.spi;

import static java.lang.Integer.MIN_VALUE;

import uk.gov.justice.services.common.converter.ObjectToJsonValueConverter;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.justice.services.core.enveloper.DefaultEnveloper;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.core.enveloper.spi.EnveloperProvider;
import uk.gov.justice.services.messaging.Envelope;
import uk.gov.justice.services.messaging.JsonEnvelope;

import java.util.function.Function;

public class EnveloperTestProvider implements EnveloperProvider {

    @Override
    public <T> Enveloper.EnveloperBuilder<T> envelop(final T payload) {
        return getDefaultEnveloper().envelop(payload);
    }

    @Override
    public Function<Object, JsonEnvelope> toEnvelopeWithMetadataFrom(final Envelope<?> envelope) {
        return getDefaultEnveloper().toEnvelopeWithMetadataFrom(envelope);
    }

    @Override
    public int priority() {
        return MIN_VALUE;
    }

    private DefaultEnveloper getDefaultEnveloper() {
        return new DefaultEnveloper(new UtcClock(), new ObjectToJsonValueConverter(new ObjectMapperProducer().objectMapper()));
    }
}
