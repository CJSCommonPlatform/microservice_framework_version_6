package uk.gov.justice.services.common.converter;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.nio.charset.Charset;

import javax.json.JsonObject;

import com.google.common.io.Resources;
import org.junit.Test;

public class StringToJsonObjectConverterTest {

    private static final String ID = "861c9430-7bc6-4bf0-b549-6534394b8d65";
    private static final String NAME = "test name";
    private static final String NESTED_ID = "c3f7182b-bd20-4678-ba8b-e7e5ea8629c3";
    private static final String NESTED_NAME = "Nested Name";

    @Test
    public void shouldConvertStringToJsonObject() throws Exception {
        StringToJsonObjectConverter stringToJsonObjectConverter = new StringToJsonObjectConverter();

        JsonObject joTest = stringToJsonObjectConverter.convert(jsonFromFile("test"));

        assertThat(joTest, notNullValue());
        assertThat(joTest.getString("id"), equalTo(ID));
        assertThat(joTest.getString("name"), equalTo(NAME));
        JsonObject joNested = joTest.getJsonObject("nested");

        assertThat(joNested, notNullValue());
        assertThat(joNested.getString("id"), equalTo(NESTED_ID));
        assertThat(joNested.getString("name"), equalTo(NESTED_NAME));
    }

    private String jsonFromFile(final String name) throws IOException {
        return Resources.toString(Resources.getResource(String.format("json/%s.json", name)), Charset.defaultCharset());
    }

}