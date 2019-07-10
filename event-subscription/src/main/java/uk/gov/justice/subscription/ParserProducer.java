package uk.gov.justice.subscription;

import uk.gov.justice.services.yaml.YamlFileValidator;
import uk.gov.justice.services.yaml.YamlParser;
import uk.gov.justice.services.yaml.YamlSchemaLoader;
import uk.gov.justice.services.yaml.YamlToJsonObjectConverter;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import com.fasterxml.jackson.databind.ObjectMapper;

@ApplicationScoped
public class ParserProducer {

    private YamlFileValidator yamlFileValidator;

    @Inject
    private ObjectMapper objectMapper;

    @Inject
    private YamlParser yamlParser;

    @Inject
    private YamlSchemaLoader yamlSchemaLoader;

    @Inject
    private SubscriptionSorter subscriptionSorter;

    @Produces
    public uk.gov.justice.subscription.EventSourcesParser eventSourcesParser() {
        return new uk.gov.justice.subscription.EventSourcesParser(yamlParser, getYamlFileValidator());
    }

    @Produces
    public SubscriptionsDescriptorParser subscriptionDescriptorsParser() {
        return new SubscriptionsDescriptorParser(yamlParser, getYamlFileValidator(), subscriptionSorter);
    }

    private YamlFileValidator getYamlFileValidator() {
        final YamlToJsonObjectConverter yamlToJsonObjectConverter = new YamlToJsonObjectConverter(yamlParser, objectMapper);
        if (yamlFileValidator == null) {
            yamlFileValidator = new YamlFileValidator(yamlToJsonObjectConverter, yamlSchemaLoader);
        }
        return yamlFileValidator;
    }
}
