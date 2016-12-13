package uk.gov.justice.services.test.utils.core.matchers;

import static org.hamcrest.MatcherAssert.assertThat;
import static uk.gov.justice.services.messaging.DefaultJsonEnvelope.envelope;
import static uk.gov.justice.services.messaging.JsonObjectMetadata.metadataWithRandomUUID;
import static uk.gov.justice.services.test.utils.core.matchers.JsonSchemaValidationMatcher.failsValidationForAnyMissingField;
import static uk.gov.justice.services.test.utils.core.matchers.JsonSchemaValidationMatcher.failsValidationWithMessage;
import static uk.gov.justice.services.test.utils.core.matchers.JsonSchemaValidationMatcher.isNotValidForSchema;
import static uk.gov.justice.services.test.utils.core.matchers.JsonSchemaValidationMatcher.isValidForSchema;

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
        assertThat("raml/json/schema/notification.subscribe.valid.json", isValidForSchema("raml/json/schema/notification.subscribe.json"));
    }

    @Test
    public void shouldFailWhenJsonDoesNotFollowSchema() {
        assertThat("raml/json/schema/notification.subscribe.invalid.value.json", isNotValidForSchema("raml/json/schema/notification.subscribe.json"));
    }

    @Test
    public void shouldValidateFailureMessage() {
        assertThat("raml/json/schema/notification.subscribe.missing.filter.json",
                failsValidationWithMessage("raml/json/schema/notification.subscribe.json",
                        "#: required key [filter] not found"));
    }

    @Test
    public void shouldValidateJsonWhichAreNotOnClasspath() throws Exception {
        assertThat(getTemporaryPathTo("raml/json/schema/notification.subscribe.valid.json"),
                isValidForSchema("raml/json/schema/notification.subscribe.json"));
    }

    @Test
    public void shouldFailWhenOneOfTheFieldIsMissingFromJsonRoot() throws Exception {
        assertThat("raml/json/schema/notification.subscribe.valid.json",
                failsValidationForAnyMissingField("raml/json/schema/notification.subscribe.json"));
    }

    @Test
    public void shouldFailWhenOneOfTheFilterFieldIsMissing() throws Exception {
        assertThat("raml/json/schema/notification.subscribe.valid.json",
                failsValidationForAnyMissingField("raml/json/schema/notification.subscribe.json", "/filter/"));
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

    @Test(expected = IllegalArgumentException.class)
    public void shouldFailToValidateJsonEnvelopeIfSchemaDoesNotExist() throws Exception {
        final JsonEnvelope jsonEnvelope = envelope()
                .with(metadataWithRandomUUID("no.match.action"))
                .withPayloadOf("id", "someId")
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