package uk.gov.justice.services.test.utils.core.matchers;

import static java.util.UUID.randomUUID;
import static org.junit.Assert.assertThat;
import static uk.gov.justice.services.messaging.JsonObjectMetadata.metadataOf;
import static uk.gov.justice.services.messaging.JsonObjectMetadata.metadataWithRandomUUID;

import uk.gov.justice.services.messaging.Metadata;

import java.util.UUID;

import org.junit.Test;

public class JsonEnvelopeMetadataMatcherTest {

    @Test
    public void shouldMatchMetadataAll() throws Exception {
        final UUID id = randomUUID();
        final UUID causationId = randomUUID();
        final String userId = "user id";
        final String sessionId = "session id";
        final UUID streamId = randomUUID();
        final Long version = 1L;

        final Metadata metadata = metadataOf(id, "event.action")
                .withCausation(causationId)
                .withUserId(userId)
                .withSessionId(sessionId)
                .withStreamId(streamId)
                .withVersion(version)
                .build();

        assertThat(metadata, JsonEnvelopeMetadataMatcher.metadata()
                .withId(id)
                .withName("event.action")
                .withCausationIds(causationId)
                .withUserId(userId)
                .withSessionId(sessionId)
                .withStreamId(streamId)
                .withVersion(version));
    }

    @Test
    public void shouldMatchMetadataById() throws Exception {
        final UUID id = randomUUID();
        final Metadata metadata = metadataOf(id, "event.action").build();

        assertThat(metadata, JsonEnvelopeMetadataMatcher.metadata()
                .withId(id));
    }

    @Test
    public void shouldMatchMetadataByName() throws Exception {
        final Metadata metadata = metadataWithRandomUUID("event.action").build();

        assertThat(metadata, JsonEnvelopeMetadataMatcher.metadata()
                .withName("event.action"));
    }

    @Test
    public void shouldMatchMetadataByCausation() throws Exception {
        final UUID causationId = randomUUID();
        final Metadata metadata = metadataWithRandomUUID("event.action")
                .withCausation(causationId)
                .build();

        assertThat(metadata, JsonEnvelopeMetadataMatcher.metadata()
                .withCausationIds(causationId));
    }

    @Test
    public void shouldMatchMetadataByUserId() throws Exception {
        final String userId = "user id";
        final Metadata metadata = metadataWithRandomUUID("event.action")
                .withUserId(userId)
                .build();

        assertThat(metadata, JsonEnvelopeMetadataMatcher.metadata()
                .withUserId(userId));
    }

    @Test
    public void shouldMatchMetadataBySessionId() throws Exception {
        final String sessionId = "session id";
        final Metadata metadata = metadataWithRandomUUID("event.action")
                .withSessionId(sessionId)
                .build();

        assertThat(metadata, JsonEnvelopeMetadataMatcher.metadata()
                .withSessionId(sessionId));
    }

    @Test
    public void shouldMatchMetadataByStreamId() throws Exception {
        final UUID streamId = randomUUID();
        final Metadata metadata = metadataWithRandomUUID("event.action")
                .withStreamId(streamId)
                .build();

        assertThat(metadata, JsonEnvelopeMetadataMatcher.metadata()
                .withStreamId(streamId));
    }

    @Test
    public void shouldMatchMetadataByVersion() throws Exception {
        final Long version = 1L;
        final Metadata metadata = metadataWithRandomUUID("event.action")
                .withVersion(version)
                .build();

        assertThat(metadata, JsonEnvelopeMetadataMatcher.metadata()
                .withVersion(version));
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
}