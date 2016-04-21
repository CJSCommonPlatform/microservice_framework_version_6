package uk.gov.justice.services.common.converter;

import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

public class JsonValueToStringConverterTest {

    private static final String ID = "861c9430-7bc6-4bf0-b549-6534394b8d65";
    private static final String NAME = "test name";
    private static final String NESTED_ID = "c3f7182b-bd20-4678-ba8b-e7e5ea8629c3";
    private static final String NESTED_NAME = "Nested Name";
    private static final String ARRAY_ITEM_1 = "ARRAY ITEM 1";
    private static final String ARRAY_ITEM_2 = "ARRAY ITEM 2";
    private static final String FIELD_NUMBER = "number";

    @Test
    public void shouldConvertJsonObjectToString() throws Exception {
        JsonValueToStringConverter jsonValueToStringConverter = new JsonValueToStringConverter();

        String testJsonAsString = jsonValueToStringConverter.convert(jsonAsJsonObject());

        assertThat(testJsonAsString, notNullValue());
        JSONAssert.assertEquals("{id:" + ID + "}", testJsonAsString, false);
        JSONAssert.assertEquals("{name:" + NAME + "}", testJsonAsString, false);
        JSONAssert.assertEquals("{nested:{id:" + NESTED_ID + "}}", testJsonAsString, false);
        JSONAssert.assertEquals("{nested:{name:" + NESTED_NAME + "}}", testJsonAsString, false);
    }

    @Test
    public void shouldConvertJsonArrayToString() throws Exception {
        JsonValueToStringConverter jsonValueToStringConverter = new JsonValueToStringConverter();

        String testJsonAsString = jsonValueToStringConverter.convert(jsonAsJsonArray());

        assertThat(testJsonAsString, notNullValue());
        JSONAssert.assertEquals("[" + ARRAY_ITEM_1 + "," + ARRAY_ITEM_2 + "]", testJsonAsString, false);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionOnJsonNumber() throws Exception {
        new JsonValueToStringConverter().convert(jsonAsJsonNumber());
    }

    private JsonObject jsonAsJsonObject() {
        JsonObjectBuilder testJsonBuilder = Json.createObjectBuilder();
        testJsonBuilder.add("id", ID);
        testJsonBuilder.add("name", NAME);

        JsonObjectBuilder nestedBuilder = Json.createObjectBuilder();
        nestedBuilder.add("id", NESTED_ID);
        nestedBuilder.add("name", NESTED_NAME);

        testJsonBuilder.add("nested", nestedBuilder.build());

        return testJsonBuilder.build();
    }

    private JsonArray jsonAsJsonArray() {
        JsonArrayBuilder testJsonArrayBuilder = Json.createArrayBuilder();
        testJsonArrayBuilder.add(ARRAY_ITEM_1);
        testJsonArrayBuilder.add(ARRAY_ITEM_2);

        return testJsonArrayBuilder.build();
    }

    private JsonNumber jsonAsJsonNumber() {
        JsonObjectBuilder testJsonBuilder = Json.createObjectBuilder();
        testJsonBuilder.add(FIELD_NUMBER, 100);
        return testJsonBuilder.build().getJsonNumber(FIELD_NUMBER);
    }

}