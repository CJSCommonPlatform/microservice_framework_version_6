package uk.gov.justice.subscription;

import uk.gov.justice.services.yaml.YamlFileValidator;
import uk.gov.justice.services.yaml.YamlParser;
import uk.gov.justice.subscription.domain.subscriptiondescriptor.SubscriptionsDescriptor;

import java.net.URL;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Stream;

import com.fasterxml.jackson.core.type.TypeReference;

/**
 * Parse YAML URLs into {@link SubscriptionsDescriptor}s
 */
public class SubscriptionsDescriptorParser {

    private static final String SUBSCRIPTION_SCHEMA_PATH = "/json/schema/subscription-schema.json";
    private static final String SUBSCRIPTIONS_DESCRIPTOR = "subscriptions_descriptor";
    private static final TypeReference<Map<String, SubscriptionsDescriptor>> SUBSCRIPTIONS_DESCRIPTOR_TYPE_REF
            = new TypeReference<Map<String, SubscriptionsDescriptor>>() {
    };

    private YamlParser yamlParser;
    private YamlFileValidator yamlFileValidator;
    private SubscriptionSorter subscriptionSorter;

    public SubscriptionsDescriptorParser(final YamlParser yamlParser,
                                         final YamlFileValidator yamlFileValidator,
                                         final SubscriptionSorter subscriptionSorter) {
        this.yamlParser = yamlParser;
        this.yamlFileValidator = yamlFileValidator;
        this.subscriptionSorter = subscriptionSorter;
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
        yamlFileValidator.validate(SUBSCRIPTION_SCHEMA_PATH, url);

        final Map<String, SubscriptionsDescriptor> stringSubscriptionDescriptorMap = yamlParser.parseYamlFrom(url, SUBSCRIPTIONS_DESCRIPTOR_TYPE_REF);
        final SubscriptionsDescriptor subscriptionsDescriptor = stringSubscriptionDescriptorMap.get(SUBSCRIPTIONS_DESCRIPTOR);

        subscriptionSorter.sortSubscriptionsByPrioritisation(subscriptionsDescriptor);
        return subscriptionsDescriptor;
    }
}
