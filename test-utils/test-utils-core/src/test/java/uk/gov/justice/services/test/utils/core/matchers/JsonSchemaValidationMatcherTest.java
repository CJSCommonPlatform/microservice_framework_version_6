package uk.gov.justice.services.test.utils.core.matchers;

import static org.hamcrest.MatcherAssert.assertThat;
import static uk.gov.justice.services.test.utils.core.matchers.JsonSchemaValidationMatcher.failsValidationForAnyMissingField;
import static uk.gov.justice.services.test.utils.core.matchers.JsonSchemaValidationMatcher.failsValidationWithMessage;
import static uk.gov.justice.services.test.utils.core.matchers.JsonSchemaValidationMatcher.isNotValidForSchema;
import static uk.gov.justice.services.test.utils.core.matchers.JsonSchemaValidationMatcher.isValidForSchema;

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
        assertThat("json/notification.subscribe.valid.json", isValidForSchema("json/schema/notification.subscribe.json"));
    }

    @Test
    public void shouldFailWhenJsonDoesNotFollowSchema() {
        assertThat("json/notification.subscribe.invalid.value.json", isNotValidForSchema("json/schema/notification.subscribe.json"));
    }

    @Test
    public void shouldValidateFailureMessage() {
        assertThat("json/notification.subscribe.missing.filter.json",
                failsValidationWithMessage("json/schema/notification.subscribe.json",
                        "#: required key [filter] not found"));
    }

    @Test
    public void shouldValidateJsonWhichAreNotOnClasspath() throws Exception {
        assertThat(getTemporaryPathTo("json/notification.subscribe.valid.json"),
                isValidForSchema("json/schema/notification.subscribe.json"));
    }

    @Test
    public void shouldFailWhenOneOfTheFieldIsMissingFromJsonRoot() throws Exception {
        assertThat("json/notification.subscribe.valid.json",
                failsValidationForAnyMissingField("json/schema/notification.subscribe.json"));
    }

    @Test
    public void shouldFailWhenOneOfTheFilterFieldIsMissing() throws Exception {
        assertThat("json/notification.subscribe.valid.json",
                failsValidationForAnyMissingField("json/schema/notification.subscribe.json", "/filter/"));
    }

    private String getTemporaryPathTo(final String pathToJsonContent) throws Exception {
        final File tempFileToJson = tempFolder.newFile();
        final BufferedWriter bw = new BufferedWriter(new FileWriter(tempFileToJson));
        bw.write(Resources.toString(Resources.getResource(pathToJsonContent), Charsets.UTF_8));
        bw.close();

        return tempFileToJson.getAbsolutePath();
    }

}