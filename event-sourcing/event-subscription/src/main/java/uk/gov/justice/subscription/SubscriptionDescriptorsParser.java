package uk.gov.justice.subscription;

import uk.gov.justice.subscription.domain.subscriptiondescriptor.SubscriptionDescriptor;
import uk.gov.justice.subscription.domain.subscriptiondescriptor.SubscriptionDescriptorDef;
import uk.gov.justice.subscription.yaml.parser.YamlFileValidator;
import uk.gov.justice.subscription.yaml.parser.YamlParser;

import java.nio.file.Path;
import java.util.Collection;
import java.util.stream.Stream;

public class SubscriptionDescriptorsParser {

    private YamlParser yamlParser;
    private YamlFileValidator yamlFileValidator;

    public SubscriptionDescriptorsParser(final YamlParser yamlParser, final YamlFileValidator yamlFileValidator) {
        this.yamlParser = yamlParser;
        this.yamlFileValidator = yamlFileValidator;
    }

    public Stream<SubscriptionDescriptor> getSubscriptionDescriptorsFrom(final Collection<Path> paths) {
        return paths.stream()
                .map(this::parseSubscriptionDescriptorFromYaml);
    }

    private SubscriptionDescriptor parseSubscriptionDescriptorFromYaml(final Path path) {
        yamlFileValidator.validateSubscription(path);
        return yamlParser.parseYamlFrom(path, SubscriptionDescriptorDef.class).getSubscriptionDescriptor();
    }
}
