package uk.gov.justice.services.adapter.rest.envelope;

import static javax.json.Json.createObjectBuilder;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static uk.gov.justice.services.common.http.HeaderConstants.CLIENT_CORRELATION_ID;

import uk.gov.justice.services.common.http.HeaderConstants;
import uk.gov.justice.services.messaging.JsonEnvelope;

import java.util.Map;
import java.util.UUID;

import javax.json.JsonObject;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedHashMap;

import com.google.common.collect.ImmutableMap;
import org.jboss.resteasy.specimpl.ResteasyHttpHeaders;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * Unit tests for the {@link RestEnvelopeBuilder} class.
 */
@RunWith(MockitoJUnitRunner.class)
public class RestEnvelopeBuilderTest {

    private static final UUID UUID_ID = UUID.randomUUID();
    private static final UUID UUID_CLIENT_CORRELATION_ID = UUID.randomUUID();
    private static final UUID UUID_USER_ID = UUID.randomUUID();
    private static final UUID UUID_SESSION_ID = UUID.randomUUID();


    @Test
    public void shouldBuildEnvelopeWithUUID() throws Exception {

        UUID uuid = UUID.randomUUID();
        JsonEnvelope envelope = new RestEnvelopeBuilder(uuid).withAction("a").build();
        assertThat(envelope.metadata().id(), equalTo(uuid));
    }

    @Test
    public void shouldBuildEmptyEnvelopeWithNameBasedOnAction() throws Exception {
        JsonEnvelope envelope = new RestEnvelopeBuilder(UUID_ID).withAction("blah").build();
        assertThat(envelope.metadata().name(), equalTo("blah"));
    }

    @Test
    public void shouldAddInitialPayload() throws Exception {
        JsonObject initialPayload = createObjectBuilder()
                .add("test", "value")
                .build();

        JsonEnvelope envelope = builderWithDefaultAction().withInitialPayload(initialPayload).build();

        assertThat(envelope.payloadAsJsonObject(), equalTo(initialPayload));
    }

    @Test
    public void shouldAddPathParams() throws Exception {

        JsonEnvelope envelope = builderWithDefaultAction()
                .withInitialPayload(
                        createObjectBuilder()
                                .add("test", "value")
                                .build())
                .withParams(ImmutableMap.of("test2", "value2"))
                .build();

        JsonObject expectedPayload = createObjectBuilder()
                .add("test", "value")
                .add("test2", "value2")
                .build();

        assertThat(envelope.payloadAsJsonObject(), equalTo(expectedPayload));
    }


    @Test
    public void shouldSetClientCorrelationId() throws Exception {

        JsonEnvelope envelope = builderWithDefaultAction()
                .withHeaders(
                        httpHeadersOf(ImmutableMap.of(CLIENT_CORRELATION_ID, UUID_CLIENT_CORRELATION_ID.toString())))
                .build();

        assertThat(envelope.metadata().clientCorrelationId().isPresent(), is(true));
        assertThat(envelope.metadata().clientCorrelationId().get(), equalTo(UUID_CLIENT_CORRELATION_ID.toString()));
    }

    @Test
    public void shouldSetUserId() throws Exception {

        JsonEnvelope envelope = builderWithDefaultAction()
                .withHeaders(
                        httpHeadersOf(ImmutableMap.of(HeaderConstants.USER_ID, UUID_USER_ID.toString())))
                .build();

        assertThat(envelope.metadata().userId().isPresent(), is(true));
        assertThat(envelope.metadata().userId().get(), equalTo(UUID_USER_ID.toString()));
    }

    @Test
    public void shouldSetSessionId() throws Exception {

        JsonEnvelope envelope = builderWithDefaultAction()
                .withHeaders(
                        httpHeadersOf(ImmutableMap.of(HeaderConstants.SESSION_ID, UUID_SESSION_ID.toString())))
                .build();

        assertThat(envelope.metadata().sessionId().isPresent(), is(true));
        assertThat(envelope.metadata().sessionId().get(), equalTo(UUID_SESSION_ID.toString()));
    }

    private RestEnvelopeBuilder builderWithDefaultAction() {
        return new RestEnvelopeBuilder(UUID_ID).withAction("abc");
    }


    private HttpHeaders httpHeadersOf(final Map<String, String> headersMap) {
        return new ResteasyHttpHeaders(new MultivaluedHashMap<>(headersMap));
    }

}
