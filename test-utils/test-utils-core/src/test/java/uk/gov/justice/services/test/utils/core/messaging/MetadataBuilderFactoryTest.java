package uk.gov.justice.services.test.utils.core.messaging;

import static net.trajano.commons.testing.UtilityClassTestUtil.assertUtilityClassWellDefined;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.Metadata;

import java.util.UUID;

import javax.json.JsonObject;

import org.junit.Test;

public class MetadataBuilderFactoryTest {

    @Test
    public void shouldBeWellDefinedUtilityClass() {
        assertUtilityClassWellDefined(MetadataBuilderFactory.class);
    }

    @Test
    public void shouldProvideMetadataBuilderWithUuidAndName() throws Exception {
        final UUID id = UUID.randomUUID();
        final String name = "name";

        final Metadata metadata = MetadataBuilderFactory.metadataOf(id, name).build();

        assertThat(metadata.id(), is(id));
        assertThat(metadata.name(), is(name));
    }

    @Test
    public void shouldProvideMetadataBuilderWithUuidAsStringAndName() throws Exception {
        final UUID id = UUID.randomUUID();
        final String name = "name";

        final Metadata metadata = MetadataBuilderFactory.metadataOf(id.toString(), name).build();

        assertThat(metadata.id(), is(id));
        assertThat(metadata.name(), is(name));
    }

    @Test
    public void shouldProvideMetadataBuilderWithRandomUuidAndName() throws Exception {
        final String name = "name";

        final Metadata metadata = MetadataBuilderFactory.metadataWithRandomUUID(name).build();

        assertThat(metadata.id(), notNullValue());
        assertThat(metadata.name(), is(name));
    }

    @Test
    public void shouldProvideMetadataBuilderWithRandomUuidAndDummyName() throws Exception {
        final Metadata metadata = MetadataBuilderFactory.metadataWithRandomUUIDAndName().build();

        assertThat(metadata.id(), notNullValue());
        assertThat(metadata.name(), is("dummy"));
    }

    @Test
    public void shouldProvideMetadataBuilderWithRandomUuidAndDummyNameAndCreatedAt() throws Exception {
        final Metadata metadata = MetadataBuilderFactory.metadataWithDefaults().build();

        assertThat(metadata.id(), notNullValue());
        assertThat(metadata.name(), is("dummy"));
        assertThat(metadata.createdAt().isPresent(), is(true));
    }

    @Test
    public void shouldProvideMetadataBuilderFromMetadata() throws Exception {
        final UUID id = UUID.randomUUID();
        final String name = "name";

        final Metadata originalMetadata = JsonEnvelope.metadataBuilder()
                .withId(id)
                .withName(name)
                .build();

        final Metadata metadata = MetadataBuilderFactory.metadataFrom(originalMetadata).build();

        assertThat(metadata.id(), notNullValue());
        assertThat(metadata.name(), is(name));
    }

    @Test
    public void shouldProvideMetadataBuilderFromJsonObject() throws Exception {
        final UUID id = UUID.randomUUID();
        final String name = "name";

        final JsonObject jsonObject = JsonEnvelope.metadataBuilder()
                .withId(id)
                .withName(name)
                .build()
                .asJsonObject();

        final Metadata metadata = MetadataBuilderFactory.metadataFrom(jsonObject).build();

        assertThat(metadata.id(), notNullValue());
        assertThat(metadata.name(), is(name));
    }
}