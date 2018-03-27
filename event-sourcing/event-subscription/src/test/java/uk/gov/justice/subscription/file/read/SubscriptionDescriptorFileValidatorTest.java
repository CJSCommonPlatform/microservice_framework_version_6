package uk.gov.justice.subscription.file.read;

import static java.nio.file.Paths.get;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.fail;

import uk.gov.justice.subscription.file.read.SubscriptionDescriptorFileValidator;
import uk.gov.justice.subscription.file.read.YamlFileToJsonObjectConverter;

import java.io.IOException;
import java.nio.file.Path;

import org.everit.json.schema.ValidationException;
import org.junit.Test;

public class SubscriptionDescriptorFileValidatorTest {

    @Test
    public void shouldNotThrowExceptionOnCorrectSubscriptionYaml() throws IOException {
        try {
            final SubscriptionDescriptorFileValidator subscriptionDescriptorFileValidator
                    = new SubscriptionDescriptorFileValidator(new YamlFileToJsonObjectConverter());

            subscriptionDescriptorFileValidator.validate(getFromClasspath("subscription.yaml"));
        } catch (ValidationException e) {
            fail("Unexpected validation exception");
        }

    }

    @Test
    public void shouldThrowExceptionOnInCorrectSubscriptionYaml() {
        try {
            final SubscriptionDescriptorFileValidator subscriptionDescriptorFileValidator
                    = new SubscriptionDescriptorFileValidator(new YamlFileToJsonObjectConverter());

            subscriptionDescriptorFileValidator.validate(getFromClasspath("incorrect-subscription.yaml"));
            fail();
        } catch (ValidationException e) {
            assertThat(e, is(instanceOf(ValidationException.class)));
            assertThat(e.getMessage(), is("#/subscription_descriptor: required key [spec_version] not found"));
        }
    }

    private Path getFromClasspath(final String path) {
        return get(getClass().getClassLoader().getResource(path).getPath());
    }
}
