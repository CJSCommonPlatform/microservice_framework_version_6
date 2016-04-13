package uk.gov.justice.services.adapter.rest.envelope;

import com.google.common.collect.ImmutableMap;
import org.jboss.resteasy.specimpl.ResteasyHttpHeaders;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.justice.services.adapter.rest.HeaderConstants;
import uk.gov.justice.services.messaging.JsonEnvelope;

import javax.json.Json;
import javax.json.JsonObject;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import java.util.UUID;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static uk.gov.justice.services.adapter.rest.HeaderConstants.CLIENT_CORRELATION_ID;

/**
 * Unit tests for the {@link RestEnvelopeBuilder} class.
 */
@RunWith(MockitoJUnitRunner.class)
public class RestJsonEnvelopeBuilderTest {

    private static final UUID UUID_ID = UUID.randomUUID();
    private static final UUID UUID_CLIENT_CORRELATION_ID = UUID.randomUUID();
    private static final UUID UUID_USER_ID = UUID.randomUUID();
    private static final UUID UUID_SESSION_ID = UUID.randomUUID();
    private static final MultivaluedHashMap<String, String> DEFAULT_HEADERS = new MultivaluedHashMap<>();

    static {
        DEFAULT_HEADERS.add("Content-Type", "application/vnd.sometype+json");
    }

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
    public void shouldBuildEnvelopeWithUUID() throws Exception {

        MultivaluedMap<String, String> headers = new MultivaluedHashMap<>();
        headers.add("Content-Type", "application/vnd.blah+json");
        setupHttpHeaders(headers);

        JsonEnvelope jsonEnvelope = builder.build();

        assertThat(jsonEnvelope.metadata().id(), equalTo(UUID_ID));
        assertThat(jsonEnvelope.metadata().name(), equalTo("blah"));
    }

    @Test
    public void shouldBuildEmptyEnvelopeWithNameBasedOnContentTypeHeader() throws Exception {

        MultivaluedMap<String, String> headers = new MultivaluedHashMap<>();
        headers.add("Content-Type", "application/vnd.blah+json");
        setupHttpHeaders(headers);

        JsonEnvelope jsonEnvelope = builder.build();

        assertThat(jsonEnvelope.metadata().name(), equalTo("blah"));

    }

    @Test
    public void shouldBuildEmptyEnvelopeWithNameBasedOnAcceptHeader() throws Exception {

        MultivaluedMap<String, String> headers = new MultivaluedHashMap<>();
        headers.add("Accept", "application/vnd.blahblah+json");
        setupHttpHeaders(headers);

        JsonEnvelope jsonEnvelope = builder.build();

        assertThat(jsonEnvelope.metadata().name(), equalTo("blahblah"));

    }

    @Test
    public void shouldBuildEmptyEnvelopeWithNameBasedOnAcceptHeader_ContentTypeInvalid() throws Exception {

        MultivaluedMap<String, String> headers = new MultivaluedHashMap<>();
        headers.add("Content-Type", "*/*");
        headers.add("Accept", "application/vnd.blahblah2+json");
        setupHttpHeaders(headers);

        JsonEnvelope jsonEnvelope = builder.build();

        assertThat(jsonEnvelope.metadata().name(), equalTo("blahblah2"));

    }



    @Test
    public void shouldAddInitialPayload() throws Exception {
        setupHttpHeaders(DEFAULT_HEADERS);
        JsonObject initialPayload = Json.createObjectBuilder()
                .add("test", "value")
                .build();
        builder = builder.withInitialPayload(initialPayload);

        JsonEnvelope jsonEnvelope = builder.build();

        assertThat(jsonEnvelope.payloadAsJsonObject(), equalTo(initialPayload));
    }

    @Test
    public void shouldAddPathParams() throws Exception {
        setupHttpHeaders(DEFAULT_HEADERS);
        JsonObject initialPayload = Json.createObjectBuilder()
                .add("test", "value")
                .build();
        builder = builder.withInitialPayload(initialPayload);
        builder = builder.withPathParams(ImmutableMap.of("test2", "value2"));

        JsonEnvelope jsonEnvelope = builder.build();

        JsonObject expectedPayload = Json.createObjectBuilder()
                .add("test", "value")
                .add("test2", "value2")
                .build();

        assertThat(jsonEnvelope.payloadAsJsonObject(), equalTo(expectedPayload));
    }

    @Test
    public void shouldSetClientCorrelationId() throws Exception {
        setupHttpHeaders(new MultivaluedHashMap<>(
                ImmutableMap.of("Content-Type", "application/vnd.blah+json", CLIENT_CORRELATION_ID, UUID_CLIENT_CORRELATION_ID.toString())));

        JsonEnvelope jsonEnvelope = builder.build();

        assertThat(jsonEnvelope.metadata().clientCorrelationId().isPresent(), is(true));
        assertThat(jsonEnvelope.metadata().clientCorrelationId().get(), equalTo(UUID_CLIENT_CORRELATION_ID.toString()));
    }

    @Test
    public void shouldSetUserId() throws Exception {
        setupHttpHeaders(new MultivaluedHashMap<>(
                ImmutableMap.of("Content-Type", "application/vnd.blah+json", HeaderConstants.USER_ID, UUID_USER_ID.toString())));

        JsonEnvelope jsonEnvelope = builder.build();

        assertThat(jsonEnvelope.metadata().userId().isPresent(), is(true));
        assertThat(jsonEnvelope.metadata().userId().get(), equalTo(UUID_USER_ID.toString()));
    }

    @Test
    public void shouldSetSessionId() throws Exception {
        setupHttpHeaders(new MultivaluedHashMap<>(
                ImmutableMap.of("Content-Type", "application/vnd.blah+json",HeaderConstants.SESSION_ID, UUID_SESSION_ID.toString())));

        JsonEnvelope jsonEnvelope = builder.build();

        assertThat(jsonEnvelope.metadata().sessionId().isPresent(), is(true));
        assertThat(jsonEnvelope.metadata().sessionId().get(), equalTo(UUID_SESSION_ID.toString()));
    }

    private void setupHttpHeaders(final MultivaluedMap<String, String> headers) {


        builder = builder.withHeaders(new ResteasyHttpHeaders(headers));
    }
}
