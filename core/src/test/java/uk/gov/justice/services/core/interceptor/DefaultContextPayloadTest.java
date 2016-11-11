package uk.gov.justice.services.core.interceptor;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static uk.gov.justice.services.core.interceptor.DefaultContextPayload.contextPayloadWith;
import static uk.gov.justice.services.core.interceptor.DefaultContextPayload.contextPayloadWithNoEnvelope;
import static uk.gov.justice.services.core.interceptor.DefaultContextPayload.copyWithEnvelope;

import uk.gov.justice.services.messaging.JsonEnvelope;

import java.util.Optional;

import org.junit.Test;

public class DefaultContextPayloadTest {

    @Test
    public void shouldCreateContextPayloadWithEnvelope() throws Exception {
        final JsonEnvelope jsonEnvelope = mock(JsonEnvelope.class);

        final ContextPayload contextPayload = contextPayloadWith(jsonEnvelope);
        assertThat(contextPayload.getEnvelope(), is(Optional.of(jsonEnvelope)));
    }

    @Test
    public void shouldCreateContextPayloadWithNoEnvelope() throws Exception {
        final ContextPayload contextPayload = contextPayloadWithNoEnvelope();
        assertThat(contextPayload.getEnvelope(), is(Optional.empty()));
    }

    @Test
    public void shouldBeAbleToSetAndRetrieveParameters() throws Exception {
        final Object parameter = mock(Object.class);
        final ContextPayload contextPayload = contextPayloadWithNoEnvelope();

        contextPayload.setParameter("test", parameter);
        assertThat(contextPayload.getParameter("test"), is(Optional.of(parameter)));
    }

    @Test
    public void shouldReturnOptionalEmptyIfParameterDoesNotExist() throws Exception {
        final ContextPayload contextPayload = contextPayloadWithNoEnvelope();
        assertThat(contextPayload.getParameter("Unkown"), is(Optional.empty()));
    }

    @Test
    public void shouldCopyAllParametersAndAddEnvelope() throws Exception {
        final JsonEnvelope jsonEnvelope = mock(JsonEnvelope.class);
        final Object parameter = mock(Object.class);
        final ContextPayload initialContext = contextPayloadWithNoEnvelope();
        initialContext.setParameter("test", parameter);

        final ContextPayload result = copyWithEnvelope(initialContext, jsonEnvelope);

        assertThat(result.getEnvelope(), is(Optional.of(jsonEnvelope)));
        assertThat(result.getParameter("test"), is(Optional.of(parameter)));
    }

}