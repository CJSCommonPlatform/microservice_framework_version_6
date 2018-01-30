package uk.gov.justice.services.core.interceptor.spi;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;

import uk.gov.justice.services.core.interceptor.DefaultInterceptorContext;
import uk.gov.justice.services.core.interceptor.InterceptorContext;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.Metadata;
import uk.gov.justice.services.messaging.spi.DefaultJsonEnvelopeProvider;

import javax.json.JsonValue;

import org.junit.Test;
import org.mockito.Mock;

public class DefaultInterceptorContextProviderTest {

    @Mock
    private Metadata metadata;

    @Mock
    private JsonValue payload;

    @Test
    public void shouldProvideInterceptorContext() {
        final DefaultInterceptorContextProvider provider = new DefaultInterceptorContextProvider();

        final JsonEnvelope jsonEnvelope = new DefaultJsonEnvelopeProvider().envelopeFrom(metadata, payload);

        InterceptorContext interceptorContext = provider.interceptorContextWithInput(jsonEnvelope);
        assertThat(interceptorContext, instanceOf(DefaultInterceptorContext.class));

    }
}
