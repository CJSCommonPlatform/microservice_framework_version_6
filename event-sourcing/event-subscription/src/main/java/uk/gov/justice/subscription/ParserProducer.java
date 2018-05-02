package uk.gov.justice.subscription;

import uk.gov.justice.subscription.yaml.parser.YamlFileValidator;
import uk.gov.justice.subscription.yaml.parser.YamlParser;
import uk.gov.justice.subscription.yaml.parser.YamlSchemaLoader;
import uk.gov.justice.subscription.yaml.parser.YamlToJsonObjectConverter;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import com.fasterxml.jackson.databind.ObjectMapper;

@ApplicationScoped
public class ParserProducer {

    private YamlFileValidator yamlFileValidator;

    @Inject
    ObjectMapper objectMapper;

    @Inject
    YamlParser yamlParser;

    @Inject
    YamlSchemaLoader yamlSchemaLoader;

    @Produces
    public uk.gov.justice.subscription.EventSourcesParser eventSourcesParser() {
        return new uk.gov.justice.subscription.EventSourcesParser(yamlParser, getYamlFileValidator());
    }

    @Produces
    public uk.gov.justice.subscription.SubscriptionDescriptorsParser subscriptionDescriptorsParser() {
        return new uk.gov.justice.subscription.SubscriptionDescriptorsParser(yamlParser, getYamlFileValidator());
    }

    private YamlFileValidator getYamlFileValidator() {
        final YamlToJsonObjectConverter yamlToJsonObjectConverter = new YamlToJsonObjectConverter(yamlParser, objectMapper);
        if (yamlFileValidator == null) {
            yamlFileValidator = new YamlFileValidator(yamlToJsonObjectConverter, yamlSchemaLoader);
        }
        return yamlFileValidator;
    }
}
