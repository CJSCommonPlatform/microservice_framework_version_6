package uk.gov.justice.services.test.utils.core.matchers;

import static java.util.UUID.randomUUID;
import static org.junit.Assert.assertThat;
import static uk.gov.justice.services.messaging.DefaultJsonEnvelope.envelope;
import static uk.gov.justice.services.messaging.JsonObjectMetadata.metadataOf;
import static uk.gov.justice.services.messaging.JsonObjectMetadata.metadataWithRandomUUID;

import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.JsonObjectMetadata;
import uk.gov.justice.services.messaging.Metadata;

import java.util.UUID;

import org.junit.Test;

public class JsonEnvelopeMetadataMatcherTest {

    private static final UUID ID = randomUUID();
    private static final UUID CAUSATION_ID = randomUUID();
    private static final String USER_ID = "user id";
    private static final String SESSION_ID = "session id";
    private static final UUID STREAM_ID = randomUUID();
    private static final Long VERSION = 1L;

    @Test
    public void shouldMatchMetadataAll() throws Exception {
        final Metadata metadata = defaultMetadataWithName("event.action").build();

        assertThat(metadata, JsonEnvelopeMetadataMatcher.metadata()
                .withId(ID)
                .withName("event.action")
                .withCausationIds(CAUSATION_ID)
                .withUserId(USER_ID)
                .withSessionId(SESSION_ID)
                .withStreamId(STREAM_ID)
                .withVersion(VERSION));
    }

    @Test
    public void shouldMatchAGivenMetadata() throws Exception {
        final Metadata testMetadata = defaultMetadataWithName("event.action").build();
        final Metadata expectedMetadata = defaultMetadataWithName("event.action").build();

        assertThat(testMetadata, JsonEnvelopeMetadataMatcher.metadata().of(expectedMetadata));
    }

    @Test
    public void shouldMatchAGivenMetadataWhereIdBecomesCausationAndDoesNotMatchName() throws Exception {
        final Metadata testMetadata = defaultMetadataRandomIdWithName("event.action")
                .withCausation(ID, CAUSATION_ID)
                .build();
        final Metadata expectedMetadata = defaultMetadataWithName("command.action").build();

        assertThat(testMetadata, JsonEnvelopeMetadataMatcher.metadata().envelopedWith(expectedMetadata));
    }

    @Test
    public void shouldMatchAGivenMetadataWhereEnvelopedFromJsonEnvelope() throws Exception {
        final Metadata testMetadata = defaultMetadataRandomIdWithName("event.action")
                .withCausation(ID, CAUSATION_ID)
                .build();

        final JsonEnvelope jsonEnvelope = envelope()
                .with(defaultMetadataWithName("command.action"))
                .withPayloadOf("Test", "value")
                .build();

        assertThat(testMetadata, JsonEnvelopeMetadataMatcher.withMetadataEnvelopedFrom(jsonEnvelope));
    }

    @Test
    public void shouldMatchMetadataById() throws Exception {
        final Metadata metadata = metadataOf(ID, "event.action").build();

        assertThat(metadata, JsonEnvelopeMetadataMatcher.metadata()
                .withId(ID));
    }

    @Test
    public void shouldMatchMetadataByName() throws Exception {
        final Metadata metadata = metadataWithRandomUUID("event.action").build();

        assertThat(metadata, JsonEnvelopeMetadataMatcher.metadata()
                .withName("event.action"));
    }

    @Test
    public void shouldMatchMetadataByCausation() throws Exception {
        final Metadata metadata = metadataWithRandomUUID("event.action")
                .withCausation(CAUSATION_ID)
                .build();

        assertThat(metadata, JsonEnvelopeMetadataMatcher.metadata()
                .withCausationIds(CAUSATION_ID));
    }

    @Test
    public void shouldMatchMetadataByUserId() throws Exception {
        final Metadata metadata = metadataWithRandomUUID("event.action")
                .withUserId(USER_ID)
                .build();

        assertThat(metadata, JsonEnvelopeMetadataMatcher.metadata()
                .withUserId(USER_ID));
    }

    @Test
    public void shouldMatchMetadataBySessionId() throws Exception {
        final Metadata metadata = metadataWithRandomUUID("event.action")
                .withSessionId(SESSION_ID)
                .build();

        assertThat(metadata, JsonEnvelopeMetadataMatcher.metadata()
                .withSessionId(SESSION_ID));
    }

    @Test
    public void shouldMatchMetadataByStreamId() throws Exception {
        final Metadata metadata = metadataWithRandomUUID("event.action")
                .withStreamId(STREAM_ID)
                .build();

        assertThat(metadata, JsonEnvelopeMetadataMatcher.metadata()
                .withStreamId(STREAM_ID));
    }

    @Test
    public void shouldMatchMetadataByVersion() throws Exception {
        final Metadata metadata = metadataWithRandomUUID("event.action")
                .withVersion(VERSION)
                .build();

        assertThat(metadata, JsonEnvelopeMetadataMatcher.metadata()
                .withVersion(VERSION));
    }

    @Test
    public void shouldMatchWithNoSettings() throws Exception {
        final Metadata metadata = metadataWithRandomUUID("event.action")
                .build();

        assertThat(metadata, JsonEnvelopeMetadataMatcher.metadata());
    }

    @Test(expected = AssertionError.class)
    public void shouldFailIfIdDoesNotMatch() throws Exception {
        final Metadata metadata = metadataWithRandomUUID("event.action").build();

        assertThat(metadata, JsonEnvelopeMetadataMatcher.metadata()
                .withId(randomUUID()));
    }

    @Test(expected = AssertionError.class)
    public void shouldFailIfNameDoesNotMatch() throws Exception {
        final Metadata metadata = metadataWithRandomUUID("event.action").build();

        assertThat(metadata, JsonEnvelopeMetadataMatcher.metadata()
                .withName("event.not.match"));
    }

    @Test(expected = AssertionError.class)
    public void shouldFailIfCausationIdDoesNotMatch() throws Exception {
        final Metadata metadata = metadataWithRandomUUID("event.action")
                .withCausation(randomUUID())
                .build();

        assertThat(metadata, JsonEnvelopeMetadataMatcher.metadata()
                .withCausationIds(randomUUID()));
    }

    @Test(expected = AssertionError.class)
    public void shouldFailIfUserIdDoesNotMatch() throws Exception {
        final Metadata metadata = metadataWithRandomUUID("event.action")
                .withUserId("user id")
                .build();

        assertThat(metadata, JsonEnvelopeMetadataMatcher.metadata()
                .withUserId("does not match"));
    }

    @Test(expected = AssertionError.class)
    public void shouldFailIfSessionIdDoesNotMatch() throws Exception {
        final Metadata metadata = metadataWithRandomUUID("event.action")
                .withSessionId("session id")
                .build();

        assertThat(metadata, JsonEnvelopeMetadataMatcher.metadata()
                .withSessionId("does not match"));
    }

    @Test(expected = AssertionError.class)
    public void shouldFailIfStreamIdDoesNotMatch() throws Exception {
        final Metadata metadata = metadataWithRandomUUID("event.action")
                .withStreamId(randomUUID())
                .build();

        assertThat(metadata, JsonEnvelopeMetadataMatcher.metadata()
                .withStreamId(randomUUID()));
    }

    @Test(expected = AssertionError.class)
    public void shouldFailIfVersionDoesNotMatch() throws Exception {
        final Metadata metadata = metadataWithRandomUUID("event.action")
                .withVersion(1L)
                .build();

        assertThat(metadata, JsonEnvelopeMetadataMatcher.metadata()
                .withVersion(2L));
    }

    private JsonObjectMetadata.Builder defaultMetadataWithName(final String name) {
        return defaultMetadataWith(ID, name);
    }

    private JsonObjectMetadata.Builder defaultMetadataRandomIdWithName(final String name) {
        return defaultMetadataWith(randomUUID(), name);
    }

    private JsonObjectMetadata.Builder defaultMetadataWith(final UUID id, final String name) {
        return metadataOf(id, name)
                .withCausation(CAUSATION_ID)
                .withUserId(USER_ID)
                .withSessionId(SESSION_ID)
                .withStreamId(STREAM_ID)
                .withVersion(VERSION);
    }
}