package uk.gov.justice.services.adapter.rest.envelope;

import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.justice.services.adapter.rest.HeaderConstants;
import uk.gov.justice.services.messaging.Envelope;

import javax.json.Json;
import javax.json.JsonObject;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import java.util.UUID;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

/**
 * Unit tests for the {@link RestEnvelopeBuilder} class.
 */
@RunWith(MockitoJUnitRunner.class)
public class RestEnvelopeBuilderTest {

    private static final UUID UUID_ID = UUID.randomUUID();
    private static final UUID UUID_CLIENT_CORRELATION_ID = UUID.randomUUID();
    private static final UUID UUID_USER_ID = UUID.randomUUID();
    private static final UUID UUID_SESSION_ID = UUID.randomUUID();
    private static final MultivaluedHashMap<String, String> EMPTY_HEADERS = new MultivaluedHashMap<>();

    @Mock
    private HttpHeaders httpHeaders;

    private RestEnvelopeBuilder builder;

    @Before
    public void setup() {
        builder = new RestEnvelopeBuilder(UUID_ID);
    }

    @Test(expected = IllegalStateException.class)
    public void shouldThrowExceptionIfHeadersAreNotSet() throws Exception {
        builder.build();
    }

    @Test
    public void shouldBuildEmptyEnvelope() throws Exception {

        setupHttpHeaders("vnd.blah+json", EMPTY_HEADERS);

        Envelope envelope = builder.build();

        assertThat(envelope.metadata().id(), equalTo(UUID_ID));
        assertThat(envelope.metadata().name(), equalTo("blah"));
        assertThat(envelope.metadata().asJsonObject().keySet(), hasSize(2));
        assertThat(envelope.payload().keySet(), hasSize(0));
    }

    @Test
    public void shouldAddInitialPayload() throws Exception {
        setupHttpHeaders("vnd.blah+json", EMPTY_HEADERS);
        JsonObject initialPayload = Json.createObjectBuilder()
                .add("test", "value")
                .build();
        builder = builder.withInitialPayload(initialPayload);

        Envelope envelope = builder.build();

        assertThat(envelope.payload(), equalTo(initialPayload));
    }

    @Test
    public void shouldAddPathParams() throws Exception {
        setupHttpHeaders("vnd.blah+json", EMPTY_HEADERS);
        JsonObject initialPayload = Json.createObjectBuilder()
                .add("test", "value")
                .build();
        builder = builder.withInitialPayload(initialPayload);
        builder = builder.withPathParams(ImmutableMap.of("test2", "value2"));

        Envelope envelope = builder.build();

        JsonObject expectedPayload = Json.createObjectBuilder()
                .add("test", "value")
                .add("test2", "value2")
                .build();

        assertThat(envelope.payload(), equalTo(expectedPayload));
    }

    @Test
    public void shouldSetClientCorrelationId() throws Exception {
        setupHttpHeaders("vnd.blah+json", new MultivaluedHashMap<>(
                ImmutableMap.of(HeaderConstants.CLIENT_CORRELATION_ID, UUID_CLIENT_CORRELATION_ID.toString())));

        Envelope envelope = builder.build();

        assertThat(envelope.metadata().clientCorrelationId().isPresent(), is(true));
        assertThat(envelope.metadata().clientCorrelationId().get(), equalTo(UUID_CLIENT_CORRELATION_ID.toString()));
    }

    @Test
    public void shouldSetUserId() throws Exception {
        setupHttpHeaders("vnd.blah+json", new MultivaluedHashMap<>(
                ImmutableMap.of(HeaderConstants.USER_ID, UUID_USER_ID.toString())));

        Envelope envelope = builder.build();

        assertThat(envelope.metadata().userId().isPresent(), is(true));
        assertThat(envelope.metadata().userId().get(), equalTo(UUID_USER_ID.toString()));
    }

    @Test
    public void shouldSetSessionId() throws Exception {
        setupHttpHeaders("vnd.blah+json", new MultivaluedHashMap<>(
                ImmutableMap.of(HeaderConstants.SESSION_ID, UUID_SESSION_ID.toString())));

        Envelope envelope = builder.build();

        assertThat(envelope.metadata().sessionId().isPresent(), is(true));
        assertThat(envelope.metadata().sessionId().get(), equalTo(UUID_SESSION_ID.toString()));
    }

    private void setupHttpHeaders(final String subtype, final MultivaluedHashMap<String, String> headers) {
        MediaType mediaType = new MediaType("application", subtype);
        when(httpHeaders.getMediaType()).thenReturn(mediaType);
        when(httpHeaders.getRequestHeaders()).thenReturn(headers);
        for (String key : headers.keySet()) {
            when(httpHeaders.getHeaderString(key)).thenReturn(headers.getFirst(key));
        }
        builder = builder.withHeaders(httpHeaders);
    }
}
