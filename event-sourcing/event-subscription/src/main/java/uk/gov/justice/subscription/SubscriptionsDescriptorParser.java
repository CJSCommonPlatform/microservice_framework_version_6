package uk.gov.justice.subscription;

import uk.gov.justice.subscription.domain.subscriptiondescriptor.SubscriptionsDescriptor;
import uk.gov.justice.subscription.yaml.parser.YamlFileValidator;
import uk.gov.justice.subscription.yaml.parser.YamlParser;

import java.net.URL;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Stream;

import com.fasterxml.jackson.core.type.TypeReference;

/**
 * Parse YAML URLs into {@link SubscriptionsDescriptor}s
 */
public class SubscriptionsDescriptorParser {
    private static final TypeReference<Map<String, SubscriptionsDescriptor>> SUBSCRIPTIONS_DESCRIPTOR_TYPE_REF
            = new TypeReference<Map<String, SubscriptionsDescriptor>>() {
    };

    private static final String SUBSCRIPTIONS_DESCRIPTOR = "subscriptions_descriptor";

    private YamlParser yamlParser;
    private YamlFileValidator yamlFileValidator;


    public SubscriptionsDescriptorParser(final YamlParser yamlParser, final YamlFileValidator yamlFileValidator) {
        this.yamlParser = yamlParser;
        this.yamlFileValidator = yamlFileValidator;
    }

    /**
     * Return a Stream of {@link SubscriptionsDescriptor} from a Collection of YAML URLs
     *
     * @param urls the YAML URLs to parse
     * @return Stream of {@link SubscriptionsDescriptor}
     */
    public Stream<SubscriptionsDescriptor> getSubscriptionDescriptorsFrom(final Collection<URL> urls) {
        return urls.stream().map(this::parseSubscriptionDescriptorFromYaml);
    }

    private SubscriptionsDescriptor parseSubscriptionDescriptorFromYaml(final URL url) {
        yamlFileValidator.validateSubscription(url);
        final Map<String, SubscriptionsDescriptor> stringSubscriptionDescriptorMap = yamlParser.parseYamlFrom(url, SUBSCRIPTIONS_DESCRIPTOR_TYPE_REF);
        return stringSubscriptionDescriptorMap.get(SUBSCRIPTIONS_DESCRIPTOR);
    }
}
