package uk.gov.justice.services.test.utils.core.matchers;

import static com.google.common.base.Charsets.UTF_8;
import static com.google.common.collect.Lists.newArrayList;
import static java.lang.String.format;
import static org.hamcrest.core.IsNot.not;

import uk.gov.justice.services.messaging.JsonEnvelope;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.util.Random;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.io.Files;
import com.google.common.io.Resources;
import org.everit.json.schema.Schema;
import org.everit.json.schema.ValidationException;
import org.everit.json.schema.loader.SchemaLoader;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeDiagnosingMatcher;
import org.hamcrest.TypeSafeMatcher;
import org.json.JSONObject;
import org.json.JSONTokener;


public class JsonSchemaValidationMatcher {

    private static final Random random = new Random();
    private static final String JSON_SCHEMA_TEMPLATE = "json/schema/%s.json";
    private static final String RAML_JSON_SCHEMA_TEMPLATE = "raml/" + JSON_SCHEMA_TEMPLATE;

    /**
     * Matcher to validate json content against a schema
     *
     * @param pathToJsonSchema path to json schema
     * @return matcher
     */
    public static Matcher<String> isValidForSchema(final String pathToJsonSchema) {

        return new TypeSafeMatcher<String>() {
            private String pathToJsonFile;
            private Exception exception = null;

            @Override
            protected boolean matchesSafely(final String pathToJsonFile) {
                this.pathToJsonFile = pathToJsonFile;
                try {
                    getJsonSchemaFor(pathToJsonSchema).validate(getJsonObjectFor(pathToJsonFile));
                } catch (final ValidationException e) {
                    exception = e;
                    return false;
                } catch (final IOException e) {
                    throw new IllegalArgumentException(e);
                }
                return true;
            }

            @Override
            public void describeTo(final Description description) {
                description.appendText("json file ").appendValue(pathToJsonFile)
                        .appendText(" to validate against schema ").appendValue(pathToJsonSchema);
            }

            @Override
            protected void describeMismatchSafely(final String pathToJsonFile, final Description mismatchDescription) {
                mismatchDescription.appendText("validation failed with message ").appendValue(exception.getMessage());
            }
        };
    }

    /**
     * Validates a JsonEnvelope against the correct schema for the action name provided in the
     * metadata. Expects to find the schema on the class path in package
     * 'json/schema/{action.name}.json' or 'raml/json/schema/{action.name}.json'.
     *
     * @return matcher
     */
    public static Matcher<JsonEnvelope> isValidJsonEnvelopeForSchema() {

        return new TypeSafeDiagnosingMatcher<JsonEnvelope>() {
            private ValidationException validationException = null;

            @Override
            protected boolean matchesSafely(final JsonEnvelope jsonEnvelope, final Description description) {

                if (null == validationException) {

                    try {
                        final String pathToJsonSchema = format(JSON_SCHEMA_TEMPLATE, jsonEnvelope.metadata().name());
                        getJsonSchemaFor(pathToJsonSchema).validate(new JSONObject(jsonEnvelope.payload().toString()));
                    } catch (final IllegalArgumentException | IOException e) {
                        try {
                            final String pathToJsonSchema = format(RAML_JSON_SCHEMA_TEMPLATE, jsonEnvelope.metadata().name());
                            getJsonSchemaFor(pathToJsonSchema).validate(new JSONObject(jsonEnvelope.payload().toString()));
                        } catch (final IOException ioe) {
                            throw new IllegalArgumentException(ioe);
                        }
                    } catch (final ValidationException e) {
                        validationException = e;
                        return false;
                    }

                    return true;
                } else {
                    description
                            .appendText("Schema validation failed with message: ")
                            .appendValue(validationException.getMessage());
                    return false;
                }

            }

            @Override
            public void describeTo(final Description description) {
                description.appendText("JsonEnvelope validated against schema found on classpath at 'raml/json/schema/' ");
            }
        };
    }

    /**
     * Matcher to validate json content against a schema
     *
     * @param pathToJsonSchema path to json schema
     * @return matcher
     */
    public static Matcher<String> isNotValidForSchema(final String pathToJsonSchema) {
        return not(isValidForSchema(pathToJsonSchema));
    }

    /**
     * Matcher to validate json content and failure message against a schema
     *
     * @param pathToJsonSchema path to json schema
     * @param errorMessage     expected error message
     * @return matcher
     */
    public static Matcher<String> failsValidationWithMessage(final String pathToJsonSchema, final String errorMessage) {

        return new TypeSafeMatcher<String>() {
            private String pathToJsonFile;
            private Exception exception = null;

            @Override
            protected boolean matchesSafely(final String pathToJsonFile) {
                this.pathToJsonFile = pathToJsonFile;
                try {
                    getJsonSchemaFor(pathToJsonSchema).validate(getJsonObjectFor(pathToJsonFile));
                } catch (final ValidationException e) {
                    exception = e;
                    return e.getMessage().equals(errorMessage);
                } catch (final IOException e) {
                    throw new IllegalArgumentException(e);
                }
                return false;
            }

            @Override
            public void describeTo(final Description description) {
                if (exception == null) {
                    description.appendText("json file ").appendValue(pathToJsonFile)
                            .appendText(" to fail validation against schema ").appendValue(pathToJsonSchema);
                } else {
                    description.appendText("json file ").appendValue(pathToJsonFile)
                            .appendText(" to fail validation against schema ").appendValue(pathToJsonSchema)
                            .appendText(" with message ").appendValue(errorMessage);
                }
            }

            @Override
            protected void describeMismatchSafely(final String pathToJsonFile, final Description mismatchDescription) {
                if (exception == null) {
                    mismatchDescription.appendText("validation passed");
                } else {
                    mismatchDescription.appendText("validation failed with message ").appendValue(exception.getMessage());
                }
            }
        };
    }

    /**
     * Matcher to validate json content after a field is been removed randomly. By default the field
     * would be removed from the root of the json.
     *
     * @param pathToJsonSchema path to json schema
     * @return matcher
     */
    public static Matcher<String> failsValidationForAnyMissingField(final String pathToJsonSchema) {
        return failsValidationForAnyMissingField(pathToJsonSchema, null);
    }

    /**
     * Matcher to validate json content after a field is been removed randomly. XPath to parent
     * field would be used as a reference, from where one of the child field will be removed and
     * validated against the schema.
     * Note: Implementation is limited to removal of json objects
     *
     * @param pathToJsonSchema path to json schema
     * @param xpathToParent    slash separated path to parent field. Null or empty to point to
     *                         root.
     * @return matcher
     */
    public static Matcher<String> failsValidationForAnyMissingField(final String pathToJsonSchema, final String xpathToParent) {

        return new TypeSafeMatcher<String>() {
            private String pathToJsonFile;

            @Override
            protected boolean matchesSafely(final String pathToJsonFile) {
                this.pathToJsonFile = pathToJsonFile;
                try {
                    getJsonSchemaFor(pathToJsonSchema).validate(getJsonObjectWithAMissingField(pathToJsonFile, xpathToParent));
                } catch (final ValidationException e) {
                    return true;
                } catch (final IOException e) {
                    throw new IllegalArgumentException(e);
                }
                return false;
            }

            @Override
            public void describeTo(final Description description) {
                description.appendText("json file ").appendValue(pathToJsonFile)
                        .appendText(" to fail validation against schema ").appendValue(pathToJsonSchema);
            }

            @Override
            protected void describeMismatchSafely(final String pathToJsonFile, final Description mismatchDescription) {
                mismatchDescription.appendText("validation passed");
            }
        };
    }

    private static JSONObject getJsonObjectFor(final String pathToJsonFile) throws IOException {
        return new JSONObject(getJsonContentFrom(pathToJsonFile));
    }

    private static Schema getJsonSchemaFor(final String pathToJsonSchema) throws IOException {
        final String jsonSchema = getJsonContentFrom(pathToJsonSchema);
        final JSONObject rawSchema = new JSONObject(new JSONTokener(jsonSchema));
        return SchemaLoader.load(rawSchema);
    }

    private static String getJsonContentFrom(final String pathToJsonSchema) throws IOException {
        final String jsonContent;
        if (Paths.get(pathToJsonSchema).isAbsolute()) {
            jsonContent = Files.toString(new File(pathToJsonSchema), UTF_8);
        } else {
            jsonContent = Resources.toString(Resources.getResource(pathToJsonSchema), UTF_8);
        }
        return jsonContent;
    }

    private static JSONObject getJsonObjectWithAMissingField(final String pathToJsonFile, final String xpathToParent) throws IOException {
        final JSONObject jsonObject = getJsonObjectFor(pathToJsonFile);

        JSONObject parentJsonObject = jsonObject;
        if (!Strings.isNullOrEmpty(xpathToParent)) {
            for (String key : Splitter.onPattern("/").omitEmptyStrings().split(xpathToParent)) {
                parentJsonObject = (JSONObject) parentJsonObject.get(key);
            }
        }

        final List<String> keysAsArray = newArrayList(parentJsonObject.keySet());
        parentJsonObject.remove(keysAsArray.get(random.nextInt(keysAsArray.size())));
        return jsonObject;
    }
}
