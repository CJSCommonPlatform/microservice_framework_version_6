package uk.gov.justice.services.messaging;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static uk.gov.justice.services.messaging.DefaultJsonEnvelope.envelope;
import static uk.gov.justice.services.messaging.JsonObjectMetadata.metadataOf;

import java.math.BigDecimal;
import java.util.UUID;

import org.junit.Test;

public class DefaultJsonEnvelopeBuilderTest {

    @Test
    public void shouldBuildEnvelopeWithMetadata() throws Exception {

        final UUID id = UUID.randomUUID();
        final String name = "someName1234";
        final JsonEnvelope envelope = envelope().with(metadataOf(id, name)).build();
        assertThat(envelope.metadata().id(), is(id));
        assertThat(envelope.metadata().name(), is(name));
    }

    @Test
    public void shouldBuildEnvelopeWithPayloadContainingStringElement() {
        final JsonEnvelope envelope = envelope().withPayloadOf("value1", "name1").build();

        assertThat(envelope.payloadAsJsonObject().getString("name1"), is("value1"));
    }


    @Test
    public void shouldBuildEnvelopeWithPayloadContainingNestedStringElement() {
        final JsonEnvelope envelope = envelope().withPayloadOf("value1", "nameLevel1", "nameLevel2").build();

        assertThat(envelope.payloadAsJsonObject().getJsonObject("nameLevel1").getString("nameLevel2"), is("value1"));
    }

    @Test
    public void shouldBuildEnvelopeWithPayloadContaining2NestedStringElements() {
        final JsonEnvelope envelope = envelope()
                .withPayloadOf("value1", "nameLevel1", "nameLevel2a")
                .withPayloadOf("value2", "nameLevel1", "nameLevel2b")
                .build();

        assertThat(envelope.payloadAsJsonObject().getJsonObject("nameLevel1").getString("nameLevel2a"), is("value1"));
        assertThat(envelope.payloadAsJsonObject().getJsonObject("nameLevel1").getString("nameLevel2b"), is("value2"));
    }

    @Test
    public void shouldBuildEnvelopeWithPayloadContainingUuid() {
        final UUID id = UUID.randomUUID();
        final JsonEnvelope envelope = envelope().withPayloadOf(id, "name1").build();

        assertThat(envelope.payloadAsJsonObject().getString("name1"), is(id.toString()));
    }


    @Test
    public void shouldBuildEnvelopeWithPayloadContainingNumericElement() {
        final JsonEnvelope envelope = envelope().withPayloadOf(BigDecimal.valueOf(123), "name1").build();

        assertThat(envelope.payloadAsJsonObject().getInt("name1"), is(123));
    }

    @Test
    public void shouldBuildEnvelopeWithPayloadContainingNestedNumericElement() {
        final JsonEnvelope envelope = envelope().withPayloadOf(BigDecimal.valueOf(5678), "nameLevel1", "nameLevel2").build();

        assertThat(envelope.payloadAsJsonObject().getJsonObject("nameLevel1").getInt("nameLevel2"), is(5678));
    }

    @Test
    public void shouldBuildEnvelopeWithPayloadContainingBooleanElement() {
        final JsonEnvelope envelope = envelope().withPayloadOf(true, "name1").build();

        assertThat(envelope.payloadAsJsonObject().getBoolean("name1"), is(true));
    }

    @Test
    public void shouldBuildEnvelopeWithPayloadContainingNestedBooleanElement() {
        final JsonEnvelope envelope = envelope().withPayloadOf(true, "nameLevel1", "nameLevel2").build();

        assertThat(envelope.payloadAsJsonObject().getJsonObject("nameLevel1").getBoolean("nameLevel2"), is(true));
    }

    @Test
    public void shouldBuildEnvelopeWithPayloadContainingArray() {
        final JsonEnvelope envelope = envelope().withPayloadOf(new String[]{"arraElem1", "arraElem2", "arraElem3"}, "name1").build();

        assertThat(envelope.payloadAsJsonObject().getJsonArray("name1").getString(0), is("arraElem1"));
        assertThat(envelope.payloadAsJsonObject().getJsonArray("name1").getString(1), is("arraElem2"));
        assertThat(envelope.payloadAsJsonObject().getJsonArray("name1").getString(2), is("arraElem3"));

    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionIfMoreThan2ElementLevels() {
        envelope().withPayloadOf("value1", "nameLevel1", "nameLevel2", "nameLevel3").build();
    }

}
