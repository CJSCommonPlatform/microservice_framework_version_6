package uk.gov.justice.services.test.utils.core.matchers;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;
import static uk.gov.justice.services.test.utils.core.matchers.JsonSchemaValidationMatcher.failsValidationForAnyMissingField;
import static uk.gov.justice.services.test.utils.core.matchers.JsonSchemaValidationMatcher.failsValidationWithMessage;
import static uk.gov.justice.services.test.utils.core.matchers.JsonSchemaValidationMatcher.isNotValidForSchema;
import static uk.gov.justice.services.test.utils.core.matchers.JsonSchemaValidationMatcher.isValidForSchema;
import static uk.gov.justice.services.test.utils.core.messaging.JsonEnvelopeBuilder.envelope;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataWithRandomUUID;

import uk.gov.justice.services.messaging.JsonEnvelope;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class JsonSchemaValidationMatcherTest {

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    @Test
    public void shouldValidateJsonContent() {
        assertThat("json/schema/notification.subscribe.valid.json", isValidForSchema("json/schema/notification.subscribe.json"));
    }

    @Test
    public void shouldFailWhenJsonDoesNotFollowSchema() {
        assertThat("json/schema/notification.subscribe.invalid.value.json", isNotValidForSchema("json/schema/notification.subscribe.json"));
    }

    @Test
    public void shouldValidateFailureMessage() {
        assertThat("json/schema/notification.subscribe.missing.filter.json",
                failsValidationWithMessage("json/schema/notification.subscribe.json",
                        "#: required key [filter] not found"));
    }

    @Test
    public void shouldValidateJsonWhichAreNotOnClasspath() throws Exception {
        assertThat(getTemporaryPathTo("json/schema/notification.subscribe.valid.json"),
                isValidForSchema("json/schema/notification.subscribe.json"));
    }

    @Test
    public void shouldFailWhenOneOfTheFieldIsMissingFromJsonRoot() throws Exception {
        assertThat("json/schema/notification.subscribe.valid.json",
                failsValidationForAnyMissingField("json/schema/notification.subscribe.json"));
    }

    @Test
    public void shouldFailWhenOneOfTheFilterFieldIsMissing() throws Exception {
        assertThat("json/schema/notification.subscribe.valid.json",
                failsValidationForAnyMissingField("json/schema/notification.subscribe.json", "/filter/"));
    }

    @Test
    public void shouldValidateJsonEnvelopeAgainstSchemaForActionNameOfEnvelope() throws Exception {
        final JsonEnvelope jsonEnvelope = envelope()
                .with(metadataWithRandomUUID("event.action"))
                .withPayloadOf("id", "someId")
                .withPayloadOf("some name", "name")
                .build();

        assertThat(jsonEnvelope, JsonSchemaValidationMatcher.isValidJsonEnvelopeForSchema());
    }

    @Test(expected = AssertionError.class)
    public void shouldFailToValidateJsonEnvelopeAgainstSchemaForActionNameOfEnvelope() throws Exception {
        final JsonEnvelope jsonEnvelope = envelope()
                .with(metadataWithRandomUUID("event.action"))
                .withPayloadOf("id", "someId")
                .build();

        assertThat(jsonEnvelope, JsonSchemaValidationMatcher.isValidJsonEnvelopeForSchema());
    }

    @Test
    public void shouldFailToValidateJsonEnvelopeIfSchemaDoesNotExist() throws Exception {
        final JsonEnvelope jsonEnvelope = envelope()
                .with(metadataWithRandomUUID("no.match.action"))
                .withPayloadOf("id", "someId")
                .build();

        try {
            assertThat(jsonEnvelope, JsonSchemaValidationMatcher.isValidJsonEnvelopeForSchema());
            fail();
        } catch (final IllegalArgumentException e) {
            assertThat(e.getMessage(), is("Failed to find schema at any of the following locations : json/schema/no.match.action.json, raml/json/schema/no.match.action.json, yaml/json/schema/no.match.action.json"));
        }
    }

    @Test
    public void shouldFallbackToRamlPathIfNotFoundInJsonPath() throws Exception {
        final JsonEnvelope jsonEnvelope = envelope()
                .with(metadataWithRandomUUID("fallback.action"))
                .withPayloadOf("id", "fallback")
                .build();

        assertThat(jsonEnvelope, JsonSchemaValidationMatcher.isValidJsonEnvelopeForSchema());
    }

    @Test
    public void shouldFallbackToYamlPathIfNotFoundInJsonPathOrRamlPath() throws Exception {
        final JsonEnvelope jsonEnvelope = envelope()
                .with(metadataWithRandomUUID("yaml.fallback.action"))
                .withPayloadOf("id", "yamlfallback")
                .build();

        assertThat(jsonEnvelope, JsonSchemaValidationMatcher.isValidJsonEnvelopeForSchema());
    }

    private String getTemporaryPathTo(final String pathToJsonContent) throws Exception {
        final File tempFileToJson = tempFolder.newFile();
        final BufferedWriter bw = new BufferedWriter(new FileWriter(tempFileToJson));
        bw.write(Resources.toString(Resources.getResource(pathToJsonContent), Charsets.UTF_8));
        bw.close();

        return tempFileToJson.getAbsolutePath();
    }

}