package uk.gov.justice.services.messaging;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsInstanceOf.instanceOf;

import uk.gov.justice.services.messaging.spi.DummyJsonEnvelopeProvider;
import uk.gov.justice.services.messaging.spi.JsonEnvelopeProvider;

import org.junit.Test;

public class JsonEnvelopeProviderTest {

    @Test
    public void shouldProviderJsonEnvelopeProvider() throws Exception {
        final JsonEnvelopeProvider provider = JsonEnvelopeProvider.provider();

        assertThat(provider, notNullValue());
        assertThat(provider, instanceOf(DummyJsonEnvelopeProvider.class));
    }
}
