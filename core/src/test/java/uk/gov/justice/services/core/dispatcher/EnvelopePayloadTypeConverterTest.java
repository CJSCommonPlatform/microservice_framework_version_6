package uk.gov.justice.services.core.dispatcher;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath;
import static javax.json.Json.createObjectBuilder;
import static javax.json.JsonValue.ValueType.OBJECT;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static uk.gov.justice.services.messaging.Envelope.envelopeFrom;

import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.justice.services.messaging.Envelope;
import uk.gov.justice.services.messaging.JsonEnvelope;

import java.io.IOException;
import java.util.UUID;

import javax.json.JsonObject;
import javax.json.JsonValue;

import org.junit.Test;

public class EnvelopePayloadTypeConverterTest {

    @Test
    public void shouldConvertPojoToJsonValue() throws IOException {

        final Envelope<TestObject> inputEnvelope = getEnvelope(new TestObject("test"));
        final Envelope<JsonValue> resultEnvelope = convert(inputEnvelope, JsonValue.class);
        final JsonObject payload = (JsonObject) resultEnvelope.payload();

        assertThat(payload.getValueType(), is(OBJECT));
        assertThat(payload.toString(), hasJsonPath("$.myString", equalTo("test")));
    }

    @Test
    public void shouldHandleNullPojo() throws IOException {

        final TestObject testObject = null;
        final Envelope<TestObject> inputEnvelope = getEnvelope(testObject);
        final Envelope<DifferentTestObject> differentTestObjectEnvelope = convert(inputEnvelope, DifferentTestObject.class);

        assertThat(differentTestObjectEnvelope.payload(), nullValue());
    }

    @Test
    public void shouldConvertJsonValueToPojo() throws IOException {

        final JsonObject payload = createObjectBuilder().add("myString", "newTest").build();
        final Envelope<JsonValue> inputEnvelope = getEnvelope(payload);
        final Envelope<TestObject> resultEnvelope = convert(inputEnvelope, TestObject.class);
        final TestObject testObject = resultEnvelope.payload();

        assertThat(testObject, isA(TestObject.class));
        assertThat(testObject.getMyString(), is("newTest"));
    }

    @Test
    public void shouldConvertJsonEnvelopeToJsonEnvelope() throws IOException {

        final JsonObject payload = createObjectBuilder().add("myString", "newTest").build();
        final JsonEnvelope inputEnvelope = getJsonEnvelope(payload);
        final Envelope<JsonValue> resultEnvelope = convert(inputEnvelope, JsonValue.class);
        final JsonObject returnedPayload = (JsonObject) resultEnvelope.payload();

        assertThat(returnedPayload.getValueType(), is(OBJECT));
        assertThat(returnedPayload.toString(), hasJsonPath("$.myString", equalTo("newTest")));
    }

    @Test
    public void shouldConvertJsonEnvelopeToPojo() throws IOException {

        final JsonObject payload = createObjectBuilder().add("myString", "newTest").build();
        final Envelope<JsonValue> inputEnvelope = getJsonEnvelope(payload);
        final Envelope<TestObject> resultEnvelope = convert(inputEnvelope, TestObject.class);
        final TestObject testObject = resultEnvelope.payload();

        assertThat(testObject, isA(TestObject.class));
        assertThat(testObject.getMyString(), is("newTest"));
    }

    @Test
    public void shouldConvertPojoToPojo() throws IOException {
        final TestObject testObject = new TestObject("testString");
        final Envelope<TestObject> inputEnvelope = getEnvelope(testObject);
        final Envelope<DifferentTestObject> differentTestObjectEnvelope = convert(inputEnvelope, DifferentTestObject.class);

        assertThat(differentTestObjectEnvelope.payload(), isA(DifferentTestObject.class));
    }

    @Test
    public void shouldConvertJsonValueToJsonValue() throws IOException {

        final JsonObject payload = createObjectBuilder().add("myString", "newTest").build();
        final Envelope<JsonValue> inputEnvelope = getEnvelope(payload);
        final Envelope<JsonValue> objectEnvelope = convert(inputEnvelope, JsonValue.class);

        assertThat(objectEnvelope.payload(), isA(JsonValue.class));
        assertThat(objectEnvelope.payload().toString(), hasJsonPath("$.myString", equalTo("newTest")));
    }

    @Test
    public void shouldReturnANull() throws IOException {

        //Asynchronous handlers are void and return null
        final JsonEnvelope nullEnvelope = null;
        final Envelope<JsonValue> objectEnvelope = convert(nullEnvelope, JsonValue.class);
        assertThat(objectEnvelope, nullValue());
    }

    private <T, R> Envelope<T> convert(final Envelope<R> inputEnvelope, final Class<T> clazz) throws IOException {
        final EnvelopePayloadTypeConverter envelopeConverter = new EnvelopePayloadTypeConverter(new ObjectMapperProducer().objectMapper());
        return envelopeConverter.convert(inputEnvelope, clazz);
    }

    private <T> Envelope<T> getEnvelope(final T payload) {
        return envelopeFrom(Envelope
                .metadataBuilder().withName("something")
                .withId(UUID.randomUUID()), payload);
    }

    private JsonEnvelope getJsonEnvelope(final JsonValue payload) {
        return JsonEnvelope.envelopeFrom(JsonEnvelope
                .metadataBuilder().withName("something")
                .withId(UUID.randomUUID()), payload);
    }


    private static class TestObject {

        private String myString;

        public TestObject() {
        }

        public TestObject(final String myString) {
            this.myString = myString;
        }

        public String getMyString() {
            return myString;
        }

        public void setMyString(final String myString) {
            this.myString = myString;
        }
    }

    private static class DifferentTestObject {

        private String myString;

        public DifferentTestObject() {
        }

        public DifferentTestObject(final String myString) {
            this.myString = myString;
        }

        public String getMyString() {
            return myString;
        }

        public void setMyString(final String myString) {
            this.myString = myString;
        }
    }

}
