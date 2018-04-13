package uk.gov.justice.subscription.file.read;

import static java.nio.file.Paths.get;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.nio.file.Path;

import org.junit.Test;

public class YamlFileToJsonObjectConverterTest {

    @Test
    public void shouldConvertYamlToJsonObject() {
        final YamlFileToJsonObjectConverter converter = new YamlFileToJsonObjectConverter();
        try {
            converter.convert(getFromClasspath("subscription.yaml"));
        } catch (IOException e) {
            fail("Unable to convert to JSON Object");
        }
    }

    @Test
    public void shouldThrowIOExceptionWhenFileDoesNotExist() {

        final YamlFileToJsonObjectConverter converter = new YamlFileToJsonObjectConverter();
        final Path thisPathDoesNotExist = get("no-subscription.yaml");

        try {
            converter.convert(thisPathDoesNotExist);
            fail("Failure, Converted a unavailable file to JSON Object");
        } catch (IOException e) {
            assertThat(e, is(instanceOf(IOException.class)));
            assertThat(e.getLocalizedMessage(), is("no-subscription.yaml"));
        }
    }

    @SuppressWarnings("SameParameterValue")
    private Path getFromClasspath(final String path) {
        return get(getClass().getClassLoader().getResource(path).getPath());
    }
}
