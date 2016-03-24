package uk.gov.justice.services.common.converter;

import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

public class JsonObjectToStringConverterTest {

    private static final String ID = "861c9430-7bc6-4bf0-b549-6534394b8d65";
    private static final String NAME = "test name";
    private static final String NESTED_ID = "c3f7182b-bd20-4678-ba8b-e7e5ea8629c3";
    private static final String NESTED_NAME = "Nested Name";

    @Test
    public void shouldConvertJsonObjectToString() throws Exception {
        JsonObjectToStringConverter jsonObjectToStringConverter = new JsonObjectToStringConverter();

        String testJsonAsString = jsonObjectToStringConverter.convert(jsonAsJsonObject());

        assertThat(testJsonAsString, notNullValue());
        JSONAssert.assertEquals("{id:" + ID + "}", testJsonAsString, false);
        JSONAssert.assertEquals("{name:" + NAME + "}", testJsonAsString, false);
        JSONAssert.assertEquals("{nested:{id:" + NESTED_ID + "}}", testJsonAsString, false);
        JSONAssert.assertEquals("{nested:{name:" + NESTED_NAME + "}}", testJsonAsString, false);
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

}