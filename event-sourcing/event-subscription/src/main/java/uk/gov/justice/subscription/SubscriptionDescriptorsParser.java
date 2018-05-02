package uk.gov.justice.subscription;

import uk.gov.justice.subscription.domain.subscriptiondescriptor.SubscriptionDescriptor;
import uk.gov.justice.subscription.domain.subscriptiondescriptor.SubscriptionDescriptorDef;
import uk.gov.justice.subscription.yaml.parser.YamlFileValidator;
import uk.gov.justice.subscription.yaml.parser.YamlParser;

import java.net.URL;
import java.util.Collection;
import java.util.stream.Stream;

/**
 * Parse YAML URLs into {@link SubscriptionDescriptor}s
 */
public class SubscriptionDescriptorsParser {

    private YamlParser yamlParser;
    private YamlFileValidator yamlFileValidator;

    public SubscriptionDescriptorsParser(final YamlParser yamlParser, final YamlFileValidator yamlFileValidator) {
        this.yamlParser = yamlParser;
        this.yamlFileValidator = yamlFileValidator;
    }

    /**
     * Return a Stream of {@link SubscriptionDescriptor} from a Collection of YAML URLs
     *
     * @param urls the YAML URLs to parse
     * @return Stream of {@link SubscriptionDescriptor}
     */
    public Stream<SubscriptionDescriptor> getSubscriptionDescriptorsFrom(final Collection<URL> urls) {
        return urls.stream()
                .map(this::parseSubscriptionDescriptorFromYaml);
    }

    private SubscriptionDescriptor parseSubscriptionDescriptorFromYaml(final URL url) {
        yamlFileValidator.validateSubscription(url);
        return yamlParser.parseYamlFrom(url, SubscriptionDescriptorDef.class).getSubscriptionDescriptor();
    }
}
