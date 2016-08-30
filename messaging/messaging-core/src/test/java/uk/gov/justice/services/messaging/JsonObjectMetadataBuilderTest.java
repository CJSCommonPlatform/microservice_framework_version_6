package uk.gov.justice.services.messaging;

import static co.unruly.matchers.OptionalMatchers.contains;
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
        final UUID id = UUID.randomUUID();
        final String name = "some.name";

        final Metadata metadata = metadataOf(id, name).build();

        assertThat(metadata.id(), is(id));
        assertThat(metadata.name(), is(name));
    }

    @Test
    public void shouldBuildMetadataWithCausation() throws Exception {
        final UUID id1 = UUID.randomUUID();
        final UUID id2 = UUID.randomUUID();

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
        final UUID streamId = UUID.randomUUID();
        final Metadata metadata = metadataWithDefaults().withStreamId(streamId).build();

        assertThat(metadata.streamId().get(), is(streamId));

    }

    @Test
    public void shouldBuildMetadataWithStreamIdAndVersion() {
        final UUID streamId = UUID.randomUUID();
        final Long version = 1234567l;
        final Metadata metadata = metadataWithDefaults().withStreamId(streamId).withVersion(version).build();

        assertThat(metadata.streamId().get(), is(streamId));
        assertThat(metadata.version().get(), is(version));

    }

    @Test
    public void shouldBuildFromMetadataAndOverwriteFields() throws Exception {
        final UUID id = UUID.randomUUID();
        final String name = "some.name";
        final Metadata originalMetadata = metadataOf(id, name).withUserId("usrIdAAAA").build();

        final Metadata metadata = metadataFrom(originalMetadata).withUserId("usrIdBBBB").build();
        assertThat(metadata.id(), is(id));
        assertThat(metadata.name(), is(name));
        assertThat(metadata.userId(), contains("usrIdBBBB"));
    }

    private JsonObjectMetadata.Builder metadataWithDefaults() {
        return metadataOf(UUID.randomUUID(), "defaultName");
    }
}
