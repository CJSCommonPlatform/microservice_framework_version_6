package uk.gov.justice.services.test.utils.core.matchers;

import static com.google.common.base.Charsets.UTF_8;
import static java.lang.String.format;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Optional;

import com.google.common.base.Strings;
import com.google.common.io.Files;
import com.google.common.io.Resources;
import org.everit.json.schema.ArraySchema;
import org.everit.json.schema.ObjectSchema;
import org.everit.json.schema.ReferenceSchema;
import org.everit.json.schema.Schema;
import org.everit.json.schema.loader.SchemaLoader;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.json.JSONObject;
import org.json.JSONTokener;

public class JsonSchemaPropertyMatcher {

    private static final String JSON_PATH_SEPARATOR = "\\.";

    /**
     * Matcher to validate if a given property exists in the given json schema
     *
     * @param jsonpathToRequiredProperty json path of the property that should exist in the schema (i.e. case.offences.offenceId)
     * @return matcher
     */
    public static Matcher<String> hasProperty(final String jsonpathToRequiredProperty) {

        return new TypeSafeMatcher<String>() {
            private String pathToJsonFile = null;

            @Override
            protected boolean matchesSafely(final String pathToJsonFile) {
                this.pathToJsonFile = pathToJsonFile;
                final JsonPath jsonPath = JsonPath.of(jsonpathToRequiredProperty);
                final ObjectSchema parentObjectSchema = getObjectSchemaFromFile(pathToJsonFile);
                return getObjectSchemaWithPath(parentObjectSchema, jsonPath).getPropertySchemas().containsKey(jsonPath.getProperty());
            }

            @Override
            public void describeTo(final Description description) {
                description.appendText("property ").appendValue(jsonpathToRequiredProperty)
                        .appendText(" should exist in JSON Schema ").appendValue(pathToJsonFile);
            }

            @Override
            protected void describeMismatchSafely(final String pathToJsonFile, final Description mismatchDescription) {
                mismatchDescription.appendText("property ").appendValue(jsonpathToRequiredProperty).appendText(" doesn't exist in schema");
            }
        };
    }

    /**
     * Matcher to validate if a given property is a required (non-optional) property in the given json schema
     *
     * @param jsonpathToRequiredProperty json path of the property that should be a required property in the schema (i.e. case.offences.offenceId)
     * @return matcher
     */
    public static Matcher<String> hasRequiredProperty(final String jsonpathToRequiredProperty) {

        return new TypeSafeMatcher<String>() {
            private String pathToJsonFile = null;

            @Override
            protected boolean matchesSafely(final String pathToJsonFile) {
                this.pathToJsonFile = pathToJsonFile;
                final JsonPath jsonPath = JsonPath.of(jsonpathToRequiredProperty);
                final ObjectSchema parentObjectSchema = getObjectSchemaFromFile(pathToJsonFile);
                return getObjectSchemaWithPath(parentObjectSchema, jsonPath).getRequiredProperties().contains(jsonPath.getProperty());
            }

            @Override
            public void describeTo(final Description description) {
                description.appendText("property ").appendValue(jsonpathToRequiredProperty)
                        .appendText(" should be a required property in JSON Schema ").appendValue(pathToJsonFile);
            }

            @Override
            protected void describeMismatchSafely(final String pathToJsonFile, final Description mismatchDescription) {
                mismatchDescription.appendText("property ").appendValue(jsonpathToRequiredProperty).appendText(" is not a required property");
            }
        };
    }

    private static ObjectSchema getObjectSchemaFromFile(final String pathToJsonSchema) {
        final String jsonSchema = getJsonContentFrom(pathToJsonSchema);
        final Schema rawSchema = SchemaLoader.load(new JSONObject(new JSONTokener(jsonSchema)));
        return Optional.of(rawSchema)
                .filter(ObjectSchema.class::isInstance)
                .map(ObjectSchema.class::cast)
                .orElseThrow(() -> new IllegalArgumentException(format("Schema found in file %s is invalid.", pathToJsonSchema)));
    }

    private static String getJsonContentFrom(final String pathToJsonSchema) {
        try {
            if (Paths.get(pathToJsonSchema).isAbsolute()) {
                return Files.toString(new File(pathToJsonSchema), UTF_8);
            } else {
                return Resources.toString(Resources.getResource(pathToJsonSchema), UTF_8);
            }
        } catch (IOException ex) {
            throw new IllegalArgumentException(format("Schema file %s not found.", pathToJsonSchema));
        }
    }

    private static ObjectSchema getObjectSchemaWithPath(final ObjectSchema parentObjectSchema, final JsonPath jsonPath) {
        return jsonPath.getRemainder().map(s -> getObjectSchemaWithPathRecursive(parentObjectSchema, jsonPath)).orElse(parentObjectSchema);
    }

    private static ObjectSchema getObjectSchemaWithPathRecursive(final Schema rawSchema, final JsonPath jsonPath) {
        if (rawSchema == null) {
            throw new IllegalArgumentException("Invalid XPath to property.");
        } else if (rawSchema instanceof ObjectSchema) {
            final ObjectSchema objectSchema = (ObjectSchema) rawSchema;
            return jsonPath.getRemainder().map(remainder -> getObjectSchemaWithPathRecursive(objectSchema.getPropertySchemas().get(jsonPath.getParent()), JsonPath.of(remainder))).orElse(objectSchema);
        } else if (rawSchema instanceof ArraySchema) {
            final ArraySchema arraySchema = (ArraySchema) rawSchema;
            return getObjectSchemaWithPathRecursive(arraySchema.getAllItemSchema(), jsonPath);
        } else if (rawSchema instanceof ReferenceSchema) {
            final ReferenceSchema referenceSchema = (ReferenceSchema) rawSchema;
            return getObjectSchemaWithPathRecursive(referenceSchema.getReferredSchema(), jsonPath);
        }
        throw new IllegalArgumentException("Unsupported property type.");
    }

    private static class JsonPath {
        private final String property;
        private final String parent;
        private final String remainder;

        private static JsonPath of(final String jsonPath) {
            final String[] jsonPathSplit = jsonPath.split(JSON_PATH_SEPARATOR);
            return new JsonPath(
                    jsonPathSplit[jsonPathSplit.length - 1],
                    jsonPathSplit[0],
                    jsonPathSplit.length >= 2 ? Strings.emptyToNull(jsonPath.split(JSON_PATH_SEPARATOR, 2)[1]) : null);
        }

        private JsonPath(final String property, final String parent, final String remainder) {
            this.parent = parent;
            this.remainder = remainder;
            this.property = property;
        }

        public String getProperty() {
            return property;
        }

        public String getParent() {
            return parent;
        }

        public Optional<String> getRemainder() {
            return Optional.ofNullable(remainder);
        }
    }
}
