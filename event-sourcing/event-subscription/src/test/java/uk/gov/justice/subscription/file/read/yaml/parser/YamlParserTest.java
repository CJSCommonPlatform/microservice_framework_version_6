package uk.gov.justice.subscription.file.read.yaml.parser;

import static java.nio.file.Paths.get;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.fail;

import uk.gov.justice.subscription.domain.subscriptiondescriptor.SubscriptionDescriptor;
import uk.gov.justice.subscription.domain.subscriptiondescriptor.SubscriptionDescriptorDef;
import uk.gov.justice.subscription.yaml.parser.YamlParser;
import uk.gov.justice.subscription.yaml.parser.YamlParserException;

import java.io.FileNotFoundException;
import java.nio.file.Path;

import org.junit.Test;

public class YamlParserTest {

    @Test
    public void shouldParsePathsToYaml() {

        final Path path = getFromClasspath("subscription.yaml");

        final YamlParser yamlParser = new YamlParser();

        final SubscriptionDescriptor subscriptionDescriptor = yamlParser.parseYamlFrom(path, SubscriptionDescriptorDef.class).getSubscriptionDescriptor();

        assertThat(subscriptionDescriptor.getService(), is("examplecontext"));
        assertThat(subscriptionDescriptor.getServiceComponent(), is("EVENT_LISTENER"));
        assertThat(subscriptionDescriptor.getSpecVersion(), is("1.0.0"));

        assertThat(subscriptionDescriptor.getSubscriptions().size(), is(2));
    }

    @Test
    public void shouldThrowFileNotFoundException() {

        final Path path = get("this-subscription-does-not-exist.yaml").toAbsolutePath();
        try {
            final YamlParser yamlParser = new YamlParser();
            yamlParser.parseYamlFrom(path, SubscriptionDescriptorDef.class);
            fail();
        } catch (final YamlParserException e) {
            assertThat(e.getCause(), is(instanceOf(FileNotFoundException.class)));
            assertThat(e.getMessage(), containsString("Failed to read YAML file"));
            assertThat(e.getMessage(), containsString("this-subscription-does-not-exist.yaml"));
        }
    }

    @SuppressWarnings("ConstantConditions")
    private Path getFromClasspath(final String name) {
        return get(getClass().getClassLoader().getResource(name).getPath());
    }
}
