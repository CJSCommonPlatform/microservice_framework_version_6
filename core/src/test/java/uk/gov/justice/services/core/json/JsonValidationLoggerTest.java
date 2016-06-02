package uk.gov.justice.services.core.json;

import static com.jayway.jsonassert.JsonAssert.with;
import static com.jayway.jsonassert.impl.matcher.IsCollectionWithSize.hasSize;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath;
import static java.lang.String.format;
import static java.nio.charset.Charset.defaultCharset;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static uk.gov.justice.services.core.json.JsonValidationLogger.toValidationTrace;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.everit.json.schema.Schema;
import org.everit.json.schema.ValidationException;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

public class JsonValidationLoggerTest {

    private String result;
    private ValidationException validationException;
    private final static String SCHEMA_LOCATION_PATTERN = "/json/schema/%s.json";
    private final static String JSON_LOCATION_PATTERN = "/json/%s.json";

    @Before
    public void setup() throws IOException {
        try {
            schema().validate(badObject());
            fail("Test should have resulted in validation errors");
        } catch (ValidationException ex) {
            validationException = ex;
        }
        result = toValidationTrace(validationException);
    }

    @Test
    public void shouldHaveTopLevelMessage() throws IOException {
        with(result)
                .assertEquals("$.message", "#: 2 schema violations found")
                .assertEquals("$.violation", "#");
    }

    @Test
    public void shouldHaveTopLevelTitle() throws IOException {
        with(result).assertEquals("$.violatedSchema", "Title of a schema for testing - ");
    }

    @Test
    public void shouldHaveSubLevelTitle() throws IOException {
        with(result)
                .assertEquals("$.causingExceptions[1].violatedSchema", "Title of a schema for testing - ");
    }

    @Test
    public void shouldHaveDescription() throws IOException {
        with(result)
                .assertEquals("$.causingExceptions[0].violatedSchema",
                        "ingredients - List ingredients and quantities for recipe");
    }

    @Test
    public void shouldSpotMissingQuantity() throws IOException {
        with(result)
                .assertEquals("$.causingExceptions[0].causingExceptions[0].causingExceptions[0].message",
                        "#/ingredients/1: required key [quantity] not found");
    }

    @Test
    public void shouldSpotMissingRequiredKeys() throws IOException {
        with(result)
                .assertEquals("$.causingExceptions[0].causingExceptions[2].causingExceptions[1].message",
                        "#/ingredients/3: required key [name] not found");
    }

    @Test
    public void shouldSpotMissingExtraneousKeys() throws IOException {
        with(result)
                .assertEquals("$.causingExceptions[0].causingExceptions[0].causingExceptions[1].message",
                        "#/ingredients/1: extraneous key [Muntity] is not permitted")
                .assertEquals("$.causingExceptions[1].message",
                        "#: extraneous key [elephant] is not permitted");
    }

    @Test
    public void shouldHaveCorrectNumberOfNodesAtTopLevel() {
        assertThat(result, hasJsonPath("$.causingExceptions", hasSize(2)));
        assertThat(result, hasJsonPath("$.causingExceptions[0].causingExceptions", hasSize(3)));
    }

    @Test
    public void shouldHaveCorrectNumberOfNodesAtSecondLevel() {
        assertThat(result, hasJsonPath("$.causingExceptions[0].causingExceptions", hasSize(3)));
    }

    @Test
    public void shouldSpotIncorrectTypes() throws IOException {
        with(result)
                .assertEquals("$.causingExceptions[0].causingExceptions[1].message",
                        "#/ingredients/2/quantity: expected type: Number, found: String")
                .assertEquals("$.causingExceptions[0].causingExceptions[2].causingExceptions[0].message",
                        "#/ingredients/3/quantity: expected type: Number, found: String");
    }

    @Test
    public void shouldPrintAllRequiredFields() throws IOException {
        with(result)
                .assertEquals("$.causingExceptions[0].causingExceptions[2].causingExceptions[1].message",
                        "#/ingredients/3: required key [name] not found")
                .assertEquals("$.causingExceptions[0].causingExceptions[2].causingExceptions[1].violatedSchema",
                        "ingredient")
                .assertEquals("$.causingExceptions[0].causingExceptions[2].causingExceptions[1].violation",
                        "#/ingredients/3");

    }

    private Schema schema() throws IOException {
        final InputStream inputStream = this.getClass().getResourceAsStream(format(SCHEMA_LOCATION_PATTERN, "fail-schema"));
        final JSONObject schemaJsonObject = new JSONObject(IOUtils.toString(inputStream, defaultCharset().name()));
        return SchemaLoader.load(schemaJsonObject);
    }

    private JSONObject badObject() throws IOException {
        final InputStream inputStream = this.getClass().getResourceAsStream(format(JSON_LOCATION_PATTERN, "fail"));
        final JSONObject schemaJsonObject = new JSONObject(IOUtils.toString(inputStream, defaultCharset().name()));
        return schemaJsonObject;
    }
}