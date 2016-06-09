package uk.gov.justice.services.messaging;

import static javax.json.JsonValue.NULL;
import static net.trajano.commons.testing.UtilityClassTestUtil.assertUtilityClassWellDefined;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonString;

import com.google.common.collect.ImmutableList;
import org.junit.Test;

/**
 * Unit tests for the {@link JsonObjects} class.
 */
public class JsonObjectsTest {

    private static final String UUID_A = "da45e8f6-d945-4f09-a115-1139a9dbb754";
    private static final String UUID_B = "d04885b4-9652-4c2a-87c6-299bda0a87d4";

    @Test
    public void shouldBeWellDefinedUtilityClass() {
        assertUtilityClassWellDefined(JsonObjects.class);
    }

    @Test
    public void shouldReturnJsonArray() {
        JsonArray array = Json.createArrayBuilder()
                .addNull()
                .build();
        JsonObject object = Json.createObjectBuilder()
                .add("name", array)
                .build();
        Optional<JsonArray> jsonArray = JsonObjects.getJsonArray(object, "name");

        assertThat(jsonArray.isPresent(), is(true));
        assertThat(jsonArray.get(), equalTo(array));
    }

    @Test
    public void shouldReturnJsonObject() {
        JsonObject subObject = Json.createObjectBuilder()
                .add("name2", "cheese")
                .build();
        JsonObject object = Json.createObjectBuilder()
                .add("name", subObject)
                .build();
        Optional<JsonObject> jsonObject = JsonObjects.getJsonObject(object, "name");

        assertThat(jsonObject.isPresent(), is(true));
        assertThat(jsonObject.get(), equalTo(subObject));
    }

    @Test
    public void shouldReturnJsonNumber() {
        JsonObject object = Json.createObjectBuilder()
                .add("name", 99L)
                .build();
        Optional<JsonNumber> jsonNumber = JsonObjects.getJsonNumber(object, "name");

        assertThat(jsonNumber.isPresent(), is(true));
        assertThat(jsonNumber.get().longValue(), equalTo(99L));
    }

    @Test
    public void shouldReturnJsonString() {
        JsonObject object = Json.createObjectBuilder()
                .add("name", "test")
                .build();
        Optional<JsonString> jsonString = JsonObjects.getJsonString(object, "name");

        assertThat(jsonString.isPresent(), is(true));
        assertThat(jsonString.get().getString(), equalTo("test"));
    }

    @Test
    public void shouldReturnJsonStringForNestedField() {
        JsonObject object = Json.createObjectBuilder()
                .add("name", Json.createObjectBuilder()
                        .add("name2", "test")
                        .build())
                .build();
        Optional<JsonString> jsonString = JsonObjects.getJsonString(object, "name", "name2");

        assertThat(jsonString.isPresent(), is(true));
        assertThat(jsonString.get().getString(), equalTo("test"));
    }

    @Test
    public void shouldReturnString() {
        JsonObject object = Json.createObjectBuilder()
                .add("name", "test")
                .build();
        Optional<String> string = JsonObjects.getString(object, "name");

        assertThat(string.isPresent(), is(true));
        assertThat(string.get(), equalTo("test"));
    }

    @Test
    public void shouldReturnBoolean() {
        JsonObject object = Json.createObjectBuilder()
                .add("someBoolean", true)
                .build();
        Optional<Boolean> someBoolean = JsonObjects.getBoolean(object, "someBoolean");

        assertThat(someBoolean.isPresent(), is(true));
        assertThat(someBoolean.get(), is(true));
    }


    @Test
    public void shouldReturnEmptyIfBooleanFieldUnknown() {
        JsonObject object = Json.createObjectBuilder()
                .build();
        Optional<Boolean> someBoolean = JsonObjects.getBoolean(object, "someBoolean");

        assertThat(someBoolean.isPresent(), is(false));
    }

    @Test(expected = IllegalStateException.class)
    public void shouldThrowExceptionForNonBoolean() {
        JsonObject object = Json.createObjectBuilder()
                .add("someBool", 99L)
                .build();
        JsonObjects.getBoolean(object, "someBool");
    }

    @Test(expected = IllegalStateException.class)
    public void shouldThrowExceptionForNonString() {
        JsonObject object = Json.createObjectBuilder()
                .add("name", 99L)
                .build();
        JsonObjects.getString(object, "name");
    }

    @Test
    public void shouldReturnUUID() {
        final String stringValue = "6c84963d-47a1-4d57-a706-09bea3fa84a5";
        JsonObject object = Json.createObjectBuilder()
                .add("name", stringValue)
                .build();
        Optional<UUID> uuid = JsonObjects.getUUID(object, "name");

        assertThat(uuid.isPresent(), is(true));
        assertThat(uuid.get(), equalTo(UUID.fromString(stringValue)));
    }

    @Test(expected = IllegalStateException.class)
    public void shouldThrowExceptionForNonUUID() {
        JsonObject object = Json.createObjectBuilder()
                .add("name", "blah")
                .build();
        JsonObjects.getUUID(object, "name");
    }

    @Test
    public void shouldReturnLong() {
        JsonObject object = Json.createObjectBuilder()
                .add("name", 99L)
                .build();
        Optional<Long> string = JsonObjects.getLong(object, "name");

        assertThat(string.isPresent(), is(true));
        assertThat(string.get(), equalTo(99L));
    }

    @Test(expected = IllegalStateException.class)
    public void shouldThrowExceptionForNonLong() {
        JsonObject object = Json.createObjectBuilder()
                .add("name", "blah")
                .build();
        JsonObjects.getLong(object, "name");
    }

    @Test
    public void shouldReturnListOfJsonStrings() {
        JsonArray array = Json.createArrayBuilder()
                .add("test1")
                .add("test2")
                .build();
        JsonObject object = Json.createObjectBuilder()
                .add("name", array)
                .build();
        Optional<List<JsonString>> jsonStrings = JsonObjects.getList(object, JsonString.class, "name");

        assertThat(jsonStrings.isPresent(), is(true));
        assertThat(jsonStrings.get(), hasSize(2));
        assertThat(jsonStrings.get().get(0).getString(), equalTo("test1"));
        assertThat(jsonStrings.get().get(1).getString(), equalTo("test2"));
    }

    @Test
    public void shouldReturnListOfStrings() {
        JsonArray array = Json.createArrayBuilder()
                .add("test1")
                .add("test2")
                .build();
        JsonObject object = Json.createObjectBuilder()
                .add("name", array)
                .build();
        Optional<List<String>> strings = JsonObjects.getList(object, JsonString.class, JsonString::getString, "name");

        assertThat(strings.isPresent(), is(true));
        assertThat(strings.get(), equalTo(ImmutableList.of("test1", "test2")));
    }

    @Test
    public void shouldReturnListOfUUIDs() {
        JsonArray array = Json.createArrayBuilder()
                .add(UUID_A)
                .add(UUID_B)
                .build();
        JsonObject object = Json.createObjectBuilder()
                .add("name", array)
                .build();
        List<UUID> uuids = JsonObjects.getUUIDs(object, "name");

        assertThat(uuids, equalTo(ImmutableList.of(UUID.fromString(UUID_A), UUID.fromString(UUID_B))));
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionForNullObject() {
        JsonObjects.getString(null, "name");
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionForNullFieldName() {
        JsonObject object = Json.createObjectBuilder()
                .add("name", 99L)
                .build();
        JsonObjects.getString(object, (String) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionForEmptyVarArgs() {
        JsonObject object = Json.createObjectBuilder()
                .add("name", 99L)
                .build();
        JsonObjects.getString(object);
    }

    @Test
    public void shouldReturnEmptyIfFieldIsUnknown() {
        JsonObject object = Json.createObjectBuilder()
                .add("name", "test")
                .build();
        Optional<String> string = JsonObjects.getString(object, "name2");

        assertThat(string.isPresent(), is(false));
    }

    @Test
    public void shouldReturnEmptyIfFieldValueIsNull() {
        JsonObject object = Json.createObjectBuilder()
                .add("name", NULL)
                .build();
        Optional<String> string = JsonObjects.getString(object, "name");

        assertThat(string.isPresent(), is(false));
    }

    @Test
    public void shouldCreateBuilderFromJsonObject() {
        JsonObject source = Json.createObjectBuilder()
                .add("name", "test")
                .build();

        JsonObjectBuilder builder = JsonObjects.createObjectBuilder(source);

        assertThat(builder.build(), equalTo(source));
    }

    @Test
    public void shouldCreateBuilderFromJsonObjectWithFilter() {
        JsonObject source = Json.createObjectBuilder()
                .add("id", "test id")
                .add("ignore1", "ignore this")
                .add("name", "test")
                .add("ignore2", "ignore this as well")
                .build();

        JsonObjectBuilder builder = JsonObjects.createObjectBuilderWithFilter(source, x -> !"ignore1".equals(x) && !"ignore2".equals(x));

        JsonObject actual = builder.build();
        assertThat(actual.size(), equalTo(2));
        assertThat(actual.getString("id"), equalTo(source.getString("id")));
        assertThat(actual.getString("name"), equalTo(source.getString("name")));
    }

}
