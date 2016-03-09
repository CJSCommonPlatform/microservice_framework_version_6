package uk.gov.justice.services.common.converter;


import com.google.common.io.Resources;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import java.io.IOException;
import java.nio.charset.Charset;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

public class JsonObjectConverterTest {

    private static final String ID = "861c9430-7bc6-4bf0-b549-6534394b8d65";
    private static final String NAME = "test name";
    private static final String NESTED_ID = "c3f7182b-bd20-4678-ba8b-e7e5ea8629c3";
    private static final String NESTED_NAME = "Nested Name";

    @Test
    public void shouldReturnJsonObjectFromString() throws Exception {
        JsonObjectConverter jsonObjectConverter = new JsonObjectConverter();

        JsonObject joTest = jsonObjectConverter.fromString(jsonFromFile("test"));

        assertThat(joTest, notNullValue());
        assertThat(joTest.getString("id"), equalTo(ID));
        assertThat(joTest.getString("name"), equalTo(NAME));
        JsonObject joNested = joTest.getJsonObject("nested");

        assertThat(joNested, notNullValue());
        assertThat(joNested.getString("id"), equalTo(NESTED_ID));
        assertThat(joNested.getString("name"), equalTo(NESTED_NAME));
    }

    @Test
    public void shouldReturnStringFromJsonObject() throws IOException {
        JsonObjectConverter jsonObjectConverter = new JsonObjectConverter();

        String testJsonAsString = jsonObjectConverter.asString(jsonAsJsonObject());

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


    private String jsonFromFile(final String name) throws IOException {
        return Resources.toString(Resources.getResource(String.format("json/%s.json", name)), Charset.defaultCharset());
    }

}