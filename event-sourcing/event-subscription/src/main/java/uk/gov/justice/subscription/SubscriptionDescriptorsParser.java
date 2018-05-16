package uk.gov.justice.subscription;

import uk.gov.justice.subscription.domain.subscriptiondescriptor.SubscriptionDescriptor;
import uk.gov.justice.subscription.yaml.parser.YamlFileValidator;
import uk.gov.justice.subscription.yaml.parser.YamlParser;

import java.net.URL;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Stream;

import com.fasterxml.jackson.core.type.TypeReference;

/**
 * Parse YAML URLs into {@link SubscriptionDescriptor}s
 */
public class SubscriptionDescriptorsParser {
    private static final TypeReference<Map<String, SubscriptionDescriptor>> SUBSCRIPTION_DESCRIPTOR_TYPE_REF
            = new TypeReference<Map<String, SubscriptionDescriptor>>() {
    };

    private static final String SUBSCRIPTION_DESCRIPTOR = "subscription_descriptor";

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
        return urls.stream().map(this::parseSubscriptionDescriptorFromYaml);
    }

    private SubscriptionDescriptor parseSubscriptionDescriptorFromYaml(final URL url) {
        yamlFileValidator.validateSubscription(url);
        final Map<String, SubscriptionDescriptor> stringSubscriptionDescriptorMap = yamlParser.parseYamlFrom(url, SUBSCRIPTION_DESCRIPTOR_TYPE_REF);
        return stringSubscriptionDescriptorMap.get(SUBSCRIPTION_DESCRIPTOR);
    }
}
