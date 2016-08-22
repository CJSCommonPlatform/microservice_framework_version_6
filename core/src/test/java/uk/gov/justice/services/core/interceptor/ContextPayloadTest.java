package uk.gov.justice.services.core.interceptor;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

import uk.gov.justice.services.messaging.JsonEnvelope;

import java.util.Optional;

import org.junit.Test;

public class ContextPayloadTest {

    @Test
    public void shouldCreateContextPayloadWithEnvelope() throws Exception {
        final JsonEnvelope jsonEnvelope = mock(JsonEnvelope.class);

        final ContextPayload contextPayload = ContextPayload.contextPayloadWith(jsonEnvelope);
        assertThat(contextPayload.getEnvelope(), is(Optional.of(jsonEnvelope)));
    }

    @Test
    public void shouldCreateContextPayloadWithNoEnvelope() throws Exception {
        final ContextPayload contextPayload = ContextPayload.contextPayloadWithNoEnvelope();
        assertThat(contextPayload.getEnvelope(), is(Optional.empty()));
    }

    @Test
    public void shouldBeAbleToSetAndRetrieveParameters() throws Exception {
        final Object parameter = mock(Object.class);
        final ContextPayload contextPayload = ContextPayload.contextPayloadWithNoEnvelope();

        contextPayload.setParameter("test", parameter);
        assertThat(contextPayload.getParameter("test"), is(Optional.of(parameter)));
    }

    @Test
    public void shouldReturnOptionalEmptyIfParameterDoesNotExist() throws Exception {
        final ContextPayload contextPayload = ContextPayload.contextPayloadWithNoEnvelope();
        assertThat(contextPayload.getParameter("Unkown"), is(Optional.empty()));
    }

    @Test
    public void shouldCopyAllParametersAndAddEnvelope() throws Exception {
        final JsonEnvelope jsonEnvelope = mock(JsonEnvelope.class);
        final Object parameter = mock(Object.class);
        final ContextPayload initialContext = ContextPayload.contextPayloadWithNoEnvelope();
        initialContext.setParameter("test", parameter);

        final ContextPayload result = ContextPayload.copyWithEnvelope(initialContext, jsonEnvelope);

        assertThat(result.getEnvelope(), is(Optional.of(jsonEnvelope)));
        assertThat(result.getParameter("test"), is(Optional.of(parameter)));
    }

}