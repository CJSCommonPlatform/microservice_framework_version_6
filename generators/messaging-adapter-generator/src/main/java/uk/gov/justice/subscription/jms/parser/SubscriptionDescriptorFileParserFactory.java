package uk.gov.justice.subscription.jms.parser;


import uk.gov.justice.maven.generator.io.files.parser.FileParserFactory;
import uk.gov.justice.subscription.domain.SubscriptionDescriptor;
import uk.gov.justice.subscription.file.read.SubscriptionDescriptorFileValidator;
import uk.gov.justice.subscription.file.read.SubscriptionDescriptorParser;
import uk.gov.justice.subscription.file.read.YamlFileToJsonObjectConverter;

public class SubscriptionDescriptorFileParserFactory implements FileParserFactory<SubscriptionDescriptor> {

    @Override
    public SubscriptionDescriptorFileParser create() {

        final SubscriptionDescriptorFileValidator subscriptionDescriptorFileValidator = new SubscriptionDescriptorFileValidator(
                new YamlFileToJsonObjectConverter());
        final SubscriptionDescriptorParser subscriptionDescriptorParser = new SubscriptionDescriptorParser(
                subscriptionDescriptorFileValidator);

        return new SubscriptionDescriptorFileParser(subscriptionDescriptorParser);
    }
}
