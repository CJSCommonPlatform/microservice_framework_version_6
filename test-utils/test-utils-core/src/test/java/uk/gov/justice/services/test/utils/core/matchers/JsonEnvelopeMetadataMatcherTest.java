package uk.gov.justice.services.test.utils.core.matchers;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.allOf;
import static org.junit.Assert.assertThat;
import static uk.gov.justice.services.test.utils.core.messaging.JsonEnvelopeBuilder.envelope;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataOf;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataWithRandomUUID;

import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.Metadata;
import uk.gov.justice.services.messaging.MetadataBuilder;

import java.util.UUID;

import org.junit.Test;

public class JsonEnvelopeMetadataMatcherTest {

    private static final UUID ID = randomUUID();
    private static final UUID CAUSATION_ID = randomUUID();
    private static final String USER_ID = "user id";
    private static final String SESSION_ID = "session id";
    private static final UUID STREAM_ID = randomUUID();
    private static final Long VERSION = 1L;
    private static final String CLIENT_CORRELATION_ID = "client correlation id";
    private static final String EVENT_NAME = "event.action";
    private static final String COMMAND_ACTION = "command.action";

    @Test
    public void shouldMatchMetadataAll() throws Exception {
        final Metadata metadata = defaultMetadataWithName(EVENT_NAME).build();

        assertThat(metadata, JsonEnvelopeMetadataMatcher.metadata()
                .withId(ID)
                .withName(EVENT_NAME)
                .withCausationIds(CAUSATION_ID)
                .withUserId(USER_ID)
                .withSessionId(SESSION_ID)
                .withStreamId(STREAM_ID)
                .withVersion(VERSION)
                .withClientCorrelationId(CLIENT_CORRELATION_ID));
    }

    @Test
    public void shouldMatchAGivenMetadata() throws Exception {
        final Metadata testMetadata = defaultMetadataWithName(EVENT_NAME).build();
        final Metadata expectedMetadata = defaultMetadataWithName(EVENT_NAME).build();

        assertThat(testMetadata, JsonEnvelopeMetadataMatcher.metadata().of(expectedMetadata));
    }

    @Test
    public void shouldMatchAGivenMetadataWhereIdBecomesCausationAndDoesNotMatchName() throws Exception {
        final Metadata testMetadata = defaultMetadataRandomIdWithName(EVENT_NAME)
                .withCausation(ID, CAUSATION_ID)
                .build();
        final Metadata expectedMetadata = defaultMetadataWithName(COMMAND_ACTION).build();

        assertThat(testMetadata, JsonEnvelopeMetadataMatcher.metadata().envelopedWith(expectedMetadata));
    }

    @Test
    public void shouldMatchAGivenMetadataWhereEnvelopedFromJsonEnvelope() throws Exception {
        final Metadata testMetadata = defaultMetadataRandomIdWithName(EVENT_NAME)
                .withCausation(ID, CAUSATION_ID)
                .build();

        final JsonEnvelope jsonEnvelope = envelope()
                .with(defaultMetadataWithName(COMMAND_ACTION))
                .withPayloadOf("Test", "value")
                .build();

        assertThat(testMetadata, JsonEnvelopeMetadataMatcher.withMetadataEnvelopedFrom(jsonEnvelope));
    }

    @Test
    public void shouldMatchMetadataById() throws Exception {
        final Metadata metadata = metadataOf(ID, EVENT_NAME).build();

        assertThat(metadata, JsonEnvelopeMetadataMatcher.metadata()
                .withId(ID));
    }

    @Test
    public void shouldMatchMetadataByName() throws Exception {
        final Metadata metadata = metadataWithRandomUUID(EVENT_NAME).build();

        assertThat(metadata, JsonEnvelopeMetadataMatcher.metadata()
                .withName(EVENT_NAME));
    }

    @Test
    public void shouldMatchMetadataByCausation() throws Exception {
        final Metadata metadata = metadataWithRandomUUID(EVENT_NAME)
                .withCausation(CAUSATION_ID)
                .build();

        assertThat(metadata, JsonEnvelopeMetadataMatcher.metadata()
                .withCausationIds(CAUSATION_ID));
    }

    @Test
    public void shouldMatchMetadataByUserId() throws Exception {
        final Metadata metadata = metadataWithRandomUUID(EVENT_NAME)
                .withUserId(USER_ID)
                .build();

        assertThat(metadata, JsonEnvelopeMetadataMatcher.metadata()
                .withUserId(USER_ID));
    }

    @Test
    public void shouldMatchMetadataBySessionId() throws Exception {
        final Metadata metadata = metadataWithRandomUUID(EVENT_NAME)
                .withSessionId(SESSION_ID)
                .build();

        assertThat(metadata, JsonEnvelopeMetadataMatcher.metadata()
                .withSessionId(SESSION_ID));
    }

    @Test
    public void shouldMatchMetadataByStreamId() throws Exception {
        final Metadata metadata = metadataWithRandomUUID(EVENT_NAME)
                .withStreamId(STREAM_ID)
                .build();

        assertThat(metadata, JsonEnvelopeMetadataMatcher.metadata()
                .withStreamId(STREAM_ID));
    }

    @Test
    public void shouldMatchMetadataByVersion() throws Exception {
        final Metadata metadata = metadataWithRandomUUID(EVENT_NAME)
                .withVersion(VERSION)
                .build();

        assertThat(metadata, JsonEnvelopeMetadataMatcher.metadata()
                .withVersion(VERSION));
    }

    @Test
    public void shouldMatchMetadataByClientCorrelationId() throws Exception {
        final Metadata metadata = metadataWithRandomUUID(EVENT_NAME)
                .withClientCorrelationId(CLIENT_CORRELATION_ID)
                .build();

        assertThat(metadata, JsonEnvelopeMetadataMatcher.metadata()
                .withClientCorrelationId(CLIENT_CORRELATION_ID));
    }

    @Test
    public void shouldMatchWithNoSettings() throws Exception {
        final Metadata metadata = metadataWithRandomUUID(EVENT_NAME)
                .build();

        assertThat(metadata, JsonEnvelopeMetadataMatcher.metadata());
    }

    @Test
    public void shouldMatchAsJson() throws Exception {
        final Metadata metadata = defaultMetadataWithName(EVENT_NAME).build();

        assertThat(metadata, JsonEnvelopeMetadataMatcher.metadata().isJson(allOf(
                withJsonPath("$.id", equalTo(ID.toString())),
                withJsonPath("$.name", equalTo(EVENT_NAME)),
                withJsonPath("$.context.user", equalTo(USER_ID)))
        ));
    }

    @Test(expected = AssertionError.class)
    public void shouldFailIfIdDoesNotMatch() throws Exception {
        final Metadata metadata = metadataWithRandomUUID(EVENT_NAME).build();

        assertThat(metadata, JsonEnvelopeMetadataMatcher.metadata()
                .withId(randomUUID()));
    }

    @Test(expected = AssertionError.class)
    public void shouldFailIfNameDoesNotMatch() throws Exception {
        final Metadata metadata = metadataWithRandomUUID(EVENT_NAME).build();

        assertThat(metadata, JsonEnvelopeMetadataMatcher.metadata()
                .withName("event.not.match"));
    }

    @Test(expected = AssertionError.class)
    public void shouldFailIfCausationIdDoesNotMatch() throws Exception {
        final Metadata metadata = metadataWithRandomUUID(EVENT_NAME)
                .withCausation(randomUUID())
                .build();

        assertThat(metadata, JsonEnvelopeMetadataMatcher.metadata()
                .withCausationIds(randomUUID()));
    }

    @Test(expected = AssertionError.class)
    public void shouldFailIfUserIdDoesNotMatch() throws Exception {
        final Metadata metadata = metadataWithRandomUUID(EVENT_NAME)
                .withUserId(USER_ID)
                .build();

        assertThat(metadata, JsonEnvelopeMetadataMatcher.metadata()
                .withUserId("does not match"));
    }

    @Test(expected = AssertionError.class)
    public void shouldFailIfSessionIdDoesNotMatch() throws Exception {
        final Metadata metadata = metadataWithRandomUUID(EVENT_NAME)
                .withSessionId(SESSION_ID)
                .build();

        assertThat(metadata, JsonEnvelopeMetadataMatcher.metadata()
                .withSessionId("does not match"));
    }

    @Test(expected = AssertionError.class)
    public void shouldFailIfStreamIdDoesNotMatch() throws Exception {
        final Metadata metadata = metadataWithRandomUUID(EVENT_NAME)
                .withStreamId(randomUUID())
                .build();

        assertThat(metadata, JsonEnvelopeMetadataMatcher.metadata()
                .withStreamId(randomUUID()));
    }

    @Test(expected = AssertionError.class)
    public void shouldFailIfVersionDoesNotMatch() throws Exception {
        final Metadata metadata = metadataWithRandomUUID(EVENT_NAME)
                .withVersion(1L)
                .build();

        assertThat(metadata, JsonEnvelopeMetadataMatcher.metadata()
                .withVersion(2L));
    }

    @Test(expected = AssertionError.class)
    public void shouldFailIfClientCorrelationIdDoesNotMatch() throws Exception {
        final Metadata metadata = metadataWithRandomUUID(EVENT_NAME)
                .withClientCorrelationId(CLIENT_CORRELATION_ID)
                .build();

        assertThat(metadata, JsonEnvelopeMetadataMatcher.metadata()
                .withClientCorrelationId("does not match"));
    }

    @Test(expected = AssertionError.class)
    public void shouldNotMatchAsJson() throws Exception {
        final Metadata metadata = defaultMetadataWithName(EVENT_NAME).build();

        assertThat(metadata, JsonEnvelopeMetadataMatcher.metadata().isJson(allOf(
                withJsonPath("$.id", equalTo(ID.toString())),
                withJsonPath("$.name", equalTo("does not match")))
        ));
    }

    private MetadataBuilder defaultMetadataWithName(final String name) {
        return defaultMetadataWith(ID, name);
    }

    private MetadataBuilder defaultMetadataRandomIdWithName(final String name) {
        return defaultMetadataWith(randomUUID(), name);
    }

    private MetadataBuilder defaultMetadataWith(final UUID id, final String name) {
        return metadataOf(id, name)
                .withCausation(CAUSATION_ID)
                .withUserId(USER_ID)
                .withSessionId(SESSION_ID)
                .withStreamId(STREAM_ID)
                .withVersion(VERSION)
                .withClientCorrelationId(CLIENT_CORRELATION_ID);
    }
}