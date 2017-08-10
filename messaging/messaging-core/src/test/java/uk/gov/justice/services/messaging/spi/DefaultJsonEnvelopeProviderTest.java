package uk.gov.justice.services.messaging.spi;

import static com.jayway.jsonassert.JsonAssert.with;
import static java.util.UUID.randomUUID;
import static javax.json.Json.createObjectBuilder;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static uk.gov.justice.services.messaging.JsonEnvelope.metadataBuilder;

import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.Metadata;
import uk.gov.justice.services.messaging.MetadataBuilder;

import java.util.UUID;

import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;

import org.junit.Test;

public class DefaultJsonEnvelopeProviderTest {

    @Test
    public void shouldProvideDefaultJsonEnvelopeFromMetadataAndJsonValue() throws Exception {
        final Metadata metadata = mock(Metadata.class);
        final JsonValue payload = mock(JsonValue.class);

        final JsonEnvelope envelope = new DefaultJsonEnvelopeProvider().envelopeFrom(metadata, payload);

        assertThat(envelope, instanceOf(DefaultJsonEnvelope.class));
        assertThat(envelope.metadata(), is(metadata));
        assertThat(envelope.payload(), is(payload));
    }

    @Test
    public void shouldProvideDefaultJsonEnvelopeFromMetadataBuilderAndJsonValue() throws Exception {
        final UUID id = randomUUID();
        final String name = "name";

        final MetadataBuilder metadataBuilder = metadataBuilder().withId(id).withName(name);
        final JsonValue payload = mock(JsonValue.class);

        final JsonEnvelope envelope = new DefaultJsonEnvelopeProvider().envelopeFrom(metadataBuilder, payload);

        assertThat(envelope, instanceOf(DefaultJsonEnvelope.class));

        final Metadata metadata = envelope.metadata();
        assertThat(metadata.id(), is(id));
        assertThat(metadata.name(), is(name));
        assertThat(envelope.payload(), is(payload));
    }

    @Test
    public void shouldProvideDefaultJsonEnvelopeFromMetadataBuilderAndJsonObjectBuilder() throws Exception {
        final UUID id = randomUUID();
        final String name = "name";

        final MetadataBuilder metadataBuilder = metadataBuilder().withId(id).withName(name);
        final JsonObjectBuilder jsonObjectBuilder = createObjectBuilder().add("test", "value");

        final JsonEnvelope envelope = new DefaultJsonEnvelopeProvider().envelopeFrom(metadataBuilder, jsonObjectBuilder);

        assertThat(envelope, instanceOf(DefaultJsonEnvelope.class));

        final Metadata metadata = envelope.metadata();
        assertThat(metadata.id(), is(id));
        assertThat(metadata.name(), is(name));
        with(envelope.payload().toString()).assertEquals("test", "value");
    }

    @Test
    public void shouldProvideJsonObjectMetadataBuilder() throws Exception {
        final MetadataBuilder metadataBuilder = new DefaultJsonEnvelopeProvider().metadataBuilder();

        assertThat(metadataBuilder, instanceOf(DefaultJsonMetadata.Builder.class));
    }

    @Test
    public void shouldProvideJsonObjectMetadataBuilderFromMetadata() throws Exception {
        final UUID id = randomUUID();
        final String name = "name";

        final Metadata metadata = metadataBuilder().withId(id).withName(name).build();

        final MetadataBuilder metadataBuilder = new DefaultJsonEnvelopeProvider().metadataFrom(metadata);

        assertThat(metadataBuilder, instanceOf(DefaultJsonMetadata.Builder.class));
        final Metadata resultMetadata = metadataBuilder.build();
        assertThat(resultMetadata.id(), is(id));
        assertThat(resultMetadata.name(), is(name));
    }

    @Test
    public void shouldProvideJsonObjectMetadataBuilderFromJsonObject() throws Exception {
        final UUID id = randomUUID();
        final String name = "name";

        final Metadata metadata = metadataBuilder().withId(id).withName(name).build();

        final MetadataBuilder metadataBuilder = new DefaultJsonEnvelopeProvider().metadataFrom(metadata.asJsonObject());

        assertThat(metadataBuilder, instanceOf(DefaultJsonMetadata.Builder.class));
        final Metadata resultMetadata = metadataBuilder.build();
        assertThat(resultMetadata.id(), is(id));
        assertThat(resultMetadata.name(), is(name));
    }

}