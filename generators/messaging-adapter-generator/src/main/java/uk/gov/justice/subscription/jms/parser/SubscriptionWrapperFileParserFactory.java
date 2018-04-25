package uk.gov.justice.subscription.jms.parser;

import uk.gov.justice.maven.generator.io.files.parser.FileParserFactory;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.justice.subscription.yaml.parser.YamlFileToJsonObjectConverter;
import uk.gov.justice.subscription.yaml.parser.YamlFileValidator;
import uk.gov.justice.subscription.yaml.parser.YamlParser;
import uk.gov.justice.subscription.yaml.parser.YamlSchemaLoader;

import com.fasterxml.jackson.databind.ObjectMapper;

public class SubscriptionWrapperFileParserFactory implements FileParserFactory<SubscriptionWrapper> {

    @Override
    public SubscriptionWrapperFileParser create() {
        final YamlParser yamlParser = new YamlParser();
        final YamlSchemaLoader yamlSchemaLoader = new YamlSchemaLoader();
        final ObjectMapper objectMapper = new ObjectMapperProducer().objectMapper();
        final YamlFileValidator yamlFileValidator = new YamlFileValidator(new YamlFileToJsonObjectConverter(yamlParser, objectMapper), yamlSchemaLoader);

        final EventSourcesFileParser eventSourcesFileParser = new EventSourcesFileParser(yamlParser, yamlFileValidator);
        final SubscriptionDescriptorFileParser subscriptionDescriptorFileParser = new SubscriptionDescriptorFileParser(yamlParser, yamlFileValidator);

        return new SubscriptionWrapperFileParser(eventSourcesFileParser, subscriptionDescriptorFileParser);
    }
}
