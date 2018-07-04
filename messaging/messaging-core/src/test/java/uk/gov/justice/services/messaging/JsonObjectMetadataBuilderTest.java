package uk.gov.justice.services.messaging;

import static co.unruly.matchers.OptionalMatchers.contains;
import static java.util.UUID.randomUUID;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static uk.gov.justice.services.messaging.JsonObjectMetadata.metadataFrom;
import static uk.gov.justice.services.messaging.JsonObjectMetadata.metadataOf;

import java.util.UUID;

import org.junit.Test;

/**
 * Unit tests for the {@link JsonObjectMetadata.Builder} class.
 */
public class JsonObjectMetadataBuilderTest {


    @Test
    public void shouldBuildMetadataWithMandatoryElements() {
        final UUID id = randomUUID();
        final String name = "some.name";

        final Metadata metadata = metadataOf(id, name).build();

        assertThat(metadata.id(), is(id));
        assertThat(metadata.name(), is(name));
    }

    @Test
    public void shouldBuildMetadataWithCausation() throws Exception {
        final UUID id1 = randomUUID();
        final UUID id2 = randomUUID();

        final Metadata metadata = metadataWithDefaults().withCausation(id1, id2).build();
        assertThat(metadata.causation(), hasItems(id1, id2));
    }

    @Test
    public void shouldBuildMetadataWithClientCorrelationId() throws Exception {
        final String correlationId = "d51597dc-2526-4c71-bd08-5031c79f11e1";
        final Metadata metadata = metadataWithDefaults().withClientCorrelationId(correlationId).build();

        assertThat(metadata.clientCorrelationId().get(), is(correlationId));
    }

    @Test
    public void shouldBuildMetadataWithUserId() {
        final String userId = "a51597dc-2526-4c71-bd08-5031c79f11e3";
        final Metadata metadata = metadataWithDefaults().withUserId(userId).build();

        assertThat(metadata.userId().get(), is(userId));

    }

    @Test
    public void shouldBuildMetadataWithSessionId() {
        final String sessionId = "b51597dc-2526-4c71-bd08-5031c79f11e3";
        final Metadata metadata = metadataWithDefaults().withSessionId(sessionId).build();

        assertThat(metadata.sessionId().get(), is(sessionId));

    }

    @Test
    public void shouldBuildMetadataWithStreamId() {
        final UUID streamId = randomUUID();
        final Metadata metadata = metadataWithDefaults().withStreamId(streamId).build();

        assertThat(metadata.streamId().get(), is(streamId));

    }

    @Test
    public void shouldBuildMetadataWithStreamIdAndPosition() {
        final UUID streamId = randomUUID();
        final Long position = 1234567l;
        final Metadata metadata = metadataWithDefaults().withStreamId(streamId).withVersion(position).build();

        assertThat(metadata.streamId().get(), is(streamId));
        assertThat(metadata.position().get(), is(position));

    }

    @Test
    public void shouldBuildFromMetadataAndOverwriteFields() throws Exception {
        final UUID id = randomUUID();
        final UUID streamId = randomUUID();
        final UUID causationId1 = randomUUID();
        final UUID causationId2 = randomUUID();
        final String name = "some.name";
        final Metadata originalMetadata = metadataOf(id, name).withUserId("usrIdAAAA").withStreamId(streamId).withCausation(causationId1, causationId2).build();

        final Metadata metadata = metadataFrom(originalMetadata).withUserId("usrIdBBBB").withVersion(4L).build();
        assertThat(metadata.position(), contains(4L));
        assertThat(metadata.userId(), contains("usrIdBBBB"));
        assertThat(metadata.id(), is(id));
        assertThat(metadata.name(), is(name));
        assertThat(metadata.streamId(), contains(streamId));
        assertThat(metadata.causation(), hasItems(causationId1, causationId2));

    }

    private JsonObjectMetadata.Builder metadataWithDefaults() {
        return metadataOf(randomUUID(), "defaultName");
    }
}
