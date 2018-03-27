package uk.gov.justice.subscription.file.read;

import static com.fasterxml.jackson.annotation.JsonCreator.Mode.PROPERTIES;
import static java.lang.String.format;

import uk.gov.justice.subscription.SubscriptionDescriptorException;
import uk.gov.justice.subscription.domain.SubscriptionDescriptor;
import uk.gov.justice.subscription.domain.SubscriptionDescriptorDef;

import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import org.everit.json.schema.ValidationException;

public class SubscriptionDescriptorParser {

    private final SubscriptionDescriptorFileValidator subscriptionDescriptorFileValidator;

    public SubscriptionDescriptorParser(final SubscriptionDescriptorFileValidator subscriptionDescriptorFileValidator) {
        this.subscriptionDescriptorFileValidator = subscriptionDescriptorFileValidator;
    }

    public SubscriptionDescriptor read(final Path filePath) {
        try {
            subscriptionDescriptorFileValidator.validate(filePath);

            final ObjectMapper mapper = new ObjectMapper(new YAMLFactory())
                    .registerModule(new Jdk8Module())
                    .registerModule(new ParameterNamesModule(PROPERTIES));

            mapper.setPropertyNamingStrategy(PropertyNamingStrategy.CAMEL_CASE_TO_LOWER_CASE_WITH_UNDERSCORES);

            return mapper.readValue(filePath.toFile(), SubscriptionDescriptorDef.class).getSubscriptionDescriptor();
        } catch (final NoSuchFileException e) {
            throw new SubscriptionDescriptorException(format("No such subscriptions YAML file %s ", filePath), e);
        } catch (final ValidationException e) {
            throw new SubscriptionDescriptorException(format("Failed to validate subscriptions yaml file %s ", filePath), e);
        } catch (final IOException e) {
            throw new SubscriptionDescriptorException(format("Failed to read subscriptions yaml file %s ", filePath), e);
        }
    }
}
